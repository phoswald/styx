package styx.core.types;

import styx.Complex;
import styx.Type;
import styx.Value;
import styx.core.values.AbstractValue;

public final class ConcreteType extends AbstractValue implements Type {

    private final AbstractType def;

    public ConcreteType(AbstractType def) {
        this.def = def;
    }

    @Override
    public int compareTo(Value other) {
        if(other.isText() || other.isReference() || other.isComplex()) {
            return 1;
        } else if(other.isType()) {
            return definition().compareTo(other.asType().definition());
        } else if(other.isFunction()) {
            return -1;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isType() {
        return true;
    }

    @Override
    public Type asType() {
        return this;
    }

    @Override
    public Complex definition() {
        return def;
    }

    @Override
    public boolean assignable(Type type) {
        return def.assignable(type);
    }

    @Override
    public boolean validate(Value val) {
        return def.validate(val);
    }
}
