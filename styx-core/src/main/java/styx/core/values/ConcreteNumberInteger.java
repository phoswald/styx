package styx.core.values;

import styx.Decimal;

/**
 * An implementation of numeric values using 32-bit signed integers.
 */
final class ConcreteNumberInteger extends AbstractNumber {

    private final int val;

    ConcreteNumberInteger(int val) {
        this.val = val;
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public boolean isLong() {
        return true;
    }

    @Override
    public boolean isDouble() {
        return true; // every int can be represented by a double without loss
    }

    @Override
    public int toInteger() {
        return val;
    }

    @Override
    public long toLong() {
        return val;
    }

    @Override
    public double toDouble() {
        return val; // every int can be represented by a double without loss
    }

    @Override
    public Decimal toDecimal() {
        return Decimal.factory(Integer.toString(val));
    }

    @Override
    public String toDecimalString() {
        return Integer.toString(val);
    }
}
