package styx.core.values;

import styx.Bool;

/**
 * A simple implementation of boolean values.
 */
final class ConcreteBool extends AbstractText implements Bool {

    public static final Bool FALSE = new ConcreteBool(false);
    public static final Bool TRUE  = new ConcreteBool(true);

    /**
     * The immutable boolean value, never null.
     */
    private final boolean val;

    private ConcreteBool(boolean val) {
        this.val = val;
    }

    @Override
    public boolean isBool() {
        return true;
    }

    @Override
    public Bool asBool() {
        return this;
    }

    @Override
    public String toTextString() {
        return val ? "true" : "false";
    }

    @Override
    public boolean toBool() {
        return val;
    }
}
