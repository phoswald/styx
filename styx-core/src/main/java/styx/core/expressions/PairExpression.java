package styx.core.expressions;

import styx.Complex;
import styx.StyxException;
import styx.core.values.CompiledComplex;

public final class PairExpression extends CompiledComplex {
    public Expression key;
    public Expression val;

    public PairExpression(Expression arg1, Expression arg2) {
        if(arg2 != null) {
            this.key = arg1;
            this.val = arg2;
        } else {
            this.val = arg1;
        }
    }

    public PairExpression(ExprFactory expf, Complex value) throws StyxException {
        this.key = expf.newExpression(findMember(value, "key", true));
        this.val = expf.newExpression(findMember(value, "val", true));
    }

    @Override
    protected Complex toValue() {
        return complex()
                .put(text("key"), key)
                .put(text("val"), val);
    }
}