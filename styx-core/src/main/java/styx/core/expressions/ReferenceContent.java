package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a reference's mutable value, either on the left or right hand side.
 */
public final class ReferenceContent extends AssignableExpression {

    public static final String TAG = ReferenceContent.class.getSimpleName();

    private final Expression expr;

    public ReferenceContent(Expression expr) {
        this.expr = expr;
    }

    public ReferenceContent(ExprFactory expf, Value value) throws StyxException {
        this.expr = expf.newExpression(value);
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG), expr);
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        return new ReferenceContent(expr.compile(scope, CompileFlag.EXPRESSION)).optimizeConst(scope);
    }

    @Override
    public Determinism effects() {
        return maxEffects(Determinism.QUERY, expr);
    }

    @Override
    public Determinism assignEffects() {
        return maxEffects(Determinism.COMMAND, expr);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        return stack.session().read(expr.evaluate(stack).asReference());
    }

    @Override
    public void assign(Stack stack, Value val) throws StyxException {
        stack.session().write(expr.evaluate(stack).asReference(), val);
    }
}
