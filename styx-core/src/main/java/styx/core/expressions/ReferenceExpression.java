package styx.core.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a reference initializer expression of the form "/part/part".
 */
public final class ReferenceExpression extends Expression {

    public static final String TAG = "Reference";

    private final List<Expression> parts;

    public ReferenceExpression(List<Expression> parts) {
        this.parts = parts != null ? parts : new ArrayList<Expression>();
    }

    public ReferenceExpression(ExprFactory expf, Complex value) throws StyxException {
        this.parts = expf.newExpressions(value);
    }

    public Expression propagateConst(Session session) {
        Reference result = session.root();
        for(Expression part : parts) {
            if(part instanceof Constant == false) {
                return this;
            }
            result = result.child(Constant.unwrap(part));
        }
        return new Constant(result);
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG), convToComplex(parts));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        List<Expression> partsc = new ArrayList<>();
        for(Expression part : parts) {
            partsc.add(part.compile(scope, CompileFlag.EXPRESSION));
        }
        return new ReferenceExpression(partsc).optimizeConst(scope);
    }

    @Override
    public Determinism effects() {
        return maxEffects(parts);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        Reference ret = stack.session().root();
        for(Expression part : parts) {
            ret = ret.child(part.evaluate(stack));
        }
        return ret;
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        for(Expression part : parts) {
            part.evaluate(stack);
        }
        return null;
    }
}
