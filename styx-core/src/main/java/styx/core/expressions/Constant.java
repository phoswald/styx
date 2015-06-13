package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.StyxRuntimeException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a constant literal value or the null value.
 */
public final class Constant extends Expression {

    public static final String TAG1 = Constant.class.getSimpleName();
    public static final String TAG2 = Constant.class.getSimpleName() + "Null";

    private final Value value;

    public Constant(Value value) {
        this.value = value;
    }

    public Constant(String tag, Value value) {
        this.value = tag.equals(TAG1) ? value : null;
    }

    public static Value unwrap(Expression expr) {
        if(expr instanceof Constant) {
            return ((Constant) expr).value;
        } else {
            throw new StyxRuntimeException("The expression is not constant.");
        }
    }

    @Override
    protected Complex toValue() {
        return value != null ? complex(text(TAG1), value) :
                               complex(text(TAG2), complex());
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        return this;
    }

    @Override
    public Determinism effects() {
        return Determinism.CONSTANT;
    }

    @Override
    public Value evaluate(Stack stack) {
        return value;
    }
}
