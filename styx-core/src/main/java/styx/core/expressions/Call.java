package styx.core.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import styx.Complex;
import styx.Determinism;
import styx.Function;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements function invocation of the form "...(...)".
 */
public final class Call extends Expression {

    public static final String TAG = Call.class.getSimpleName();

    private final Expression       func;
    private final List<Expression> args;
    private final Determinism   det;

    public Call(Expression func, List<Expression> args) {
        this.func = func;
        this.args = args != null ? args : new ArrayList<Expression>();
        this.det  = null;
    }

    public Call(ExprFactory expf, Complex value) throws StyxException {
        this.func = expf.newExpression (findMember(value, "func",  true));
        this.args = expf.newExpressions(findMember(value, "args",  true));
        this.det  = null;
    }

    private Call(Expression func, List<Expression> args, Scope scope) {
        this.func = func;
        this.args = args;
        this.det  = calcDeterminism(scope);
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG),
                complex().
                put(text("func"), func).
                put(text("args"), convToComplex(args)));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        List<Expression> argsc = new ArrayList<>();
        for(Expression arg : args) {
            argsc.add(arg.compile(scope, CompileFlag.EXPRESSION));
        }
        // TODO (implement) check argumentCount() if call is statically linked
        return new Call(func.compile(scope, CompileFlag.EXPRESSION), argsc, scope).optimizeConst(scope);
    }

    @Override
    public Determinism effects() {
        return det;
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        Function target = Objects.requireNonNull(func.evaluate(stack), "The target of the function call is null.").asFunction();
        // Push argument onto stack before moving current stack frame. This is necessary
        // because arguments can be local variables, which rely on the current stack frame.
        int frameBase = stack.prepareFrame();
        for(int i = 0; i < args.size(); i++) {
            stack.push(args.get(i).evaluate(stack));
        }
        stack.enterFrame(frameBase);
        Value result;
        if(target instanceof ConcreteFunction) {
            // fast path: pass arguments on stack
            result = ((ConcreteFunction) target).definition().invoke(stack);
        } else {
            // slow path: pass arguments on array
            result = target.invoke(stack.session(), stack.getFrameAsArray());
        }
        // Move the current stack frame back to the to old position. This returns not only the arguments
        // passed to the function but also all local variables created by the target function. In the case
        // of an exception, the catch location is responsible to restore the correct frame position.
        // This is independent of whether the exception occurred in the argument expressions or in the target function.
        stack.leaveFrame(frameBase);
        return result;
    }

    private Determinism calcDeterminism(Scope scope) {
        // Note:
        // - argsdet is CONSTANT if all arguments are constant.
        // - funcdet is CONSTANT if the call is statically linked.
        // - targdet is CONSTANT if the call is statically linked and the target function is pure (or even constant).
        // - targdet is NON_DETERMINISTIC if the target function cannot be resolved. In this case we have to assume
        //   the worst and optimization is not possible. Such an expression cannot event be used in an atomic block.
        Determinism argsdet = maxEffects(args);
        Determinism funcdet = func.effects();
        Determinism targdet;
        if(funcdet == Determinism.CONSTANT) {
            try {
                targdet = func.evaluateConst(scope).asFunction().determinism();
                if(targdet == Determinism.PURE) {
                    targdet = Determinism.CONSTANT;
                }
            } catch(RuntimeException | StyxException e) {
                // optimization possible, but evaluation leads to an exception (this is not optimized).
                targdet = Determinism.NON_DETERMINISTIC;
            }
        } else {
            targdet = Determinism.NON_DETERMINISTIC; // optimization not possible.
        }
        return maxEffects(argsdet, targdet);
    }
}
