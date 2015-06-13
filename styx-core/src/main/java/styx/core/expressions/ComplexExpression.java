package styx.core.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.Pair;
import styx.Session;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements complex initializer expression of the form "[val, val]" or "[key:val, key:val]".
 */
public final class ComplexExpression extends Expression {

    public static final String TAG = "Complex";

    private final List<PairExpression> children;

    public ComplexExpression(List<PairExpression> children) {
        this.children = children != null ? children : new ArrayList<PairExpression>();

        for(int i = 0; i < this.children.size(); i++) {
            PairExpression child = this.children.get(i);
            if(child.key == null) {
                child.key = new Constant(convToNumber(i + 1));
            }
        }
    }

    public ComplexExpression(Expression key, Expression val) {
        this.children = Arrays.asList(new PairExpression(key, val));
    }

    public ComplexExpression(ExprFactory expf, Complex value) throws StyxException {
        this.children = convToPairList(expf, value);
    }

    public Expression propagateConst(Session session) {
        Complex result = session.complex();
        for(PairExpression child : children) {
            if(child.key instanceof Constant == false || child.val instanceof Constant == false) {
                return this;
            }
            result = result.put(Constant.unwrap(child.key), Constant.unwrap(child.val));
        }
        return new Constant(result);
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG), convToComplex(children));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        List<PairExpression> childrenc = new ArrayList<>();
        int base = scope.enterBlock();
        for(PairExpression child : children) {
            childrenc.add(new PairExpression(
                    child.key.compile(scope, CompileFlag.EXPRESSION),
                    child.val.compile(scope, CompileFlag.EXPRESSION)));
        }
        scope.leaveBlock(base);
        return new ComplexExpression(childrenc).optimizeConst(scope);
    }

    @Override
    public Determinism effects() {
        Determinism det = Determinism.CONSTANT;
        for(PairExpression child : children) {
            det = maxEffects(det, maxEffects(child.key, child.val));
        }
        return det;
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        Complex result = stack.session().complex();
        for(PairExpression child : children) {
            Value keyret = child.key.evaluate(stack);
            Value valret = child.val.evaluate(stack);
            result = result.put(keyret, valret);
        }
        return result;
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        for(PairExpression child : children) {
            child.key.execute(stack);
            child.val.execute(stack);
        }
        return null;
    }

    private static List<PairExpression> convToPairList(ExprFactory expf, Complex value) throws StyxException {
        List<PairExpression> res = new ArrayList<>();
        for(Pair<Value,Value> child : value.asComplex()) {
            res.add(new PairExpression(expf, child.val().asComplex()));
        }
        return res;
    }
}
