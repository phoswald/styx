package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a "if(...) ... else ..." statement.
 */
public final class IfElse extends Expression {

    public static final String TAG = IfElse.class.getSimpleName();

    private final Expression cond;
    private final Expression exprif;
    private final Expression exprelse;

    public IfElse(Expression cond, Expression exprif, Expression exprelse) {
        this.cond     = cond;
        this.exprif   = exprif;
        this.exprelse = exprelse;
    }

    public IfElse(ExprFactory expf, Complex value) throws StyxException {
        this.cond     = expf.newExpression(findMember(value, "cond",     true));
        this.exprif   = expf.newExpression(findMember(value, "exprif",   true));
        this.exprelse = expf.newExpression(findMember(value, "exprelse", false));
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG),
                complex().
                put(text("cond"),     cond).
                put(text("exprif"),   exprif).
                put(text("exprelse"), exprelse));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        return new IfElse(
                cond.compile(scope, CompileFlag.EXPRESSION),
                exprif.compile(scope, flags),
                exprelse != null ? exprelse.compile(scope, flags) : null).optimizeConst(scope);
    }

    @Override
    public Determinism effects() {
        return maxEffects(cond, exprif, exprelse);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        if(cond.evaluate(stack).asBool().toBool()) {
            return exprif.evaluate(stack);
        } else if(exprelse != null) {
            return exprelse.evaluate(stack);
        } else {
            return null;
        }
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        if(cond.evaluate(stack).asBool().toBool()) {
            return exprif.execute(stack);
        } else if(exprelse != null) {
            return exprelse.execute(stack);
        } else {
            return null;
        }
    }
}
