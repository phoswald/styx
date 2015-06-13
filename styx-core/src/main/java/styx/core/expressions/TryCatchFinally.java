package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a "try ... catch(...) ... finally ..." statement.
 */
public final class TryCatchFinally extends Expression {

    public static final String TAG = TryCatchFinally.class.getSimpleName();

    private final Expression exprtry;
    private final Expression exprcatch;
    private final Expression exprfinally;

    public TryCatchFinally(Expression exprtry, Expression exprcatch, Expression exprfinally) {
        this.exprtry     = exprtry;
        this.exprcatch   = exprcatch;
        this.exprfinally = exprfinally;
    }

    public TryCatchFinally(ExprFactory expf, Complex value) throws StyxException {
        this.exprtry     = expf.newExpression(findMember(value, "exprtry",     true));
        this.exprcatch   = expf.newExpression(findMember(value, "exprcatch",   false));
        this.exprfinally = expf.newExpression(findMember(value, "exprfinally", false));
    }

    @Override
    protected Complex toValue() {
        return complex(text(TryCatchFinally.class.getSimpleName()),
                complex().
                put(text("exprtry"),     exprtry).
                put(text("exprcatch"),   exprcatch).
                put(text("exprfinally"), exprfinally));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        return new TryCatchFinally(
                exprtry     != null ? exprtry.    compile(scope, flags) : null,
                exprcatch   != null ? exprcatch.  compile(scope, flags) : null,
                exprfinally != null ? exprfinally.compile(scope, flags) : null);
    }

    @Override
    public Determinism effects() {
        return maxEffects(exprtry, exprcatch, exprfinally);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException { // TODO can we refactor this?
        Flow flow = execute(stack);
        /* if(flow == Flow.Return || flow == Flow.Yield) {
            return stack.getResult(); // the block is left using a 'return' or 'yield'. The result is on the stack.
        } else */ if(flow != null) {
            throw new UnsupportedOperationException("Invalid control flow statement in 'try-catch-finally' expression.");
        } else {
            return null;
        }
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        int frameBase = stack.getFrameBase();
        int frameSize = stack.getFrameSize();
        try {
            if(exprtry != null) {
                Flow flow = exprtry.execute(stack);
                if(flow != null) {
                    return flow;
                }
            }
        } catch(StyxException e) {
            stack.setFrame(frameBase, frameSize);
            if(exprcatch != null) {
                Flow flow = exprcatch.execute(stack);
                if(flow != null) {
                    return flow;
                }
            } else {
                throw e;
            }
        } finally {
            stack.setFrame(frameBase, frameSize);
            if(exprfinally != null) {
                Flow flow = exprfinally.execute(stack);
                if(flow != null) {
                    return flow;
                }
            }
        }
        return null;
    }
}
