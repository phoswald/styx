package styx.core.values;

import styx.Void;

/**
 * A simple implementation of empty values.
 */
final class ConcreteVoid extends AbstractText implements Void {

    public static final Void VOID = new ConcreteVoid();

    private ConcreteVoid() { }

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public Void asVoid() {
        return this;
    }

    @Override
    public String toTextString() {
        return "void";
    }
}
