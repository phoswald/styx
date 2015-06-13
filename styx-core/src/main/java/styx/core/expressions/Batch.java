package styx.core.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a batch of statements.
 * <p>
 * A batch of statements is similar to a {...} block, with the difference that it evaluates
 * to the list containing the results of all its contained statements.
 */
public final class Batch extends Expression {

    public static final String TAG = Batch.class.getSimpleName();

    private final List<Expression> children;

    public Batch(List<Expression> children) {
        this.children = children != null ? children : new ArrayList<Expression>();
    }

    public Batch(ExprFactory expf, Complex value) throws StyxException {
        this.children = expf.newExpressions(value);
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG), convToComplex(children));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        List<Expression> childrenc = new ArrayList<>();
        int base = scope.enterBlock();
        for(Expression child : children) {
            childrenc.add(child.compile(scope, CompileFlag.BATCH));
        }
        scope.leaveBlock(base);
        return new Batch(childrenc).optimizeConst(scope);
    }

    @Override
    public Determinism effects() {
        return maxEffects(children);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        if(children.size() == 1) {
            return children.get(0).evaluate(stack);
        } else {
            Complex result = stack.session().complex();
            int key = 1;
            for(Expression child : children) {
             // result = result.add(child.evaluate(stack)); // currently, add(null) is a no-op, so keys are not preserved between input and result.
                result = result.put(stack.session().number(key++), child.evaluate(stack));
            }
            return result;
        }
    }
}
