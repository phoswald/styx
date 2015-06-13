package styx.core.types;

import styx.Type;
import styx.Value;
import styx.core.values.CompiledComplex;

public abstract class AbstractType extends CompiledComplex {

    protected final Type type;

    protected AbstractType() {
        super(null); // TODO ?
        this.type = new ConcreteType(this);
    }

    public final Type type() {
        return type;
    }

    public abstract boolean assignable(Type type);

    public abstract boolean validate(Value val);
}
