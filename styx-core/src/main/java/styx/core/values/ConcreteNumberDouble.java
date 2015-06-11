package styx.core.values;

import styx.Decimal;

/**
 * An implementation of numeric values using 64-bit floats.
 * This class should only be used if the number cannot be represented by a
 * 32- or 64-bit signed integer because conversions to these types fail immediately.
 */
final class ConcreteNumberDouble extends AbstractNumber {

    private final double val;

    ConcreteNumberDouble(double val) {
        this.val = val;
    }

    @Override
    public boolean isDouble() {
        return true;
    }

    @Override
    public double toDouble() {
        return val;
    }

    @Override
    public Decimal toDecimal() {
        return Decimal.factory(Double.toString(val));
    }

    @Override
    public String toDecimalString() {
        return Decimal.factory(Double.toString(val)).toString();
    }
}
