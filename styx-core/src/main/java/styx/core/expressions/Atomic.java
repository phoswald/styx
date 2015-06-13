package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.ConcurrentException;
import styx.Determinism;
import styx.RetryException;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a "atomic ..." statement.
 */
public final class Atomic extends Expression {

    public static final String TAG = Atomic.class.getSimpleName();

    private final Expression expr;

    public Atomic(Expression expr) {
        this.expr = expr;
    }

    public Atomic(ExprFactory expf, Value value) throws StyxException {
        this.expr = expf.newExpression(value);
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG), expr);
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        Expression exprc = expr.compile(scope, flags);
        if(exprc.effects() == Determinism.NON_DETERMINISTIC) {
            throw new StyxException("An 'atomic' statement cannot contain non-deterministic expressions.");
        }
        return new Atomic(exprc);
    }

    @Override
    public Determinism effects() {
        return expr.effects();
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException { // TODO can we refactor this?
        Flow flow = execute(stack);
        /* if(flow == Flow.Return || flow == Flow.Yield) {
            return stack.getResult(); // the block is left using a 'return' or 'yield'. The result is on the stack.
        } else */ if(flow != null) {
            throw new UnsupportedOperationException("Invalid control flow statement in 'atomic' expression.");
        } else {
            return null;
        }
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        if(stack.session().hasTransaction()) {
            // A transaction is already active. Transactions can be nested, but committing a nested
            // transaction has no effect and retrying always restart the outermost transaction.
            // Therefore, nothing has to be done before or after executing the atomic block.
            return expr.execute(stack);
        }
        while(true) {
            int frameBase = stack.getFrameBase();
            int frameSize = stack.getFrameSize();
            try {
                // Start a transaction, execute the atomic block and commit it.
                stack.session().beginTransaction();
                Flow flow = expr.execute(stack);
                stack.session().commitTransaction();
                return flow;
            } catch(ConcurrentException e) {
                // Transactions experiencing concurrent modification start over immediately
                stack.setFrame(frameBase, frameSize);
                continue;
            } catch(RetryException e) {
                // Retrying aborts the transaction, waits for a change and then continues to start over.
                stack.setFrame(frameBase, frameSize);
                stack.session().abortTransaction(true);
                continue;
            } finally {
                // If an exception has thrown, abort the transaction, but don't catch the exception.
                // If the transaction committed normally or if it is retried, there's nothing to do.
                if(stack.session().hasTransaction()) {
                    stack.session().abortTransaction(false);
                }
            }
        }
    }
}
