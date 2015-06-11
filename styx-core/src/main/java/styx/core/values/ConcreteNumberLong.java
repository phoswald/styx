package styx.core.values;

import styx.Decimal;

/**
 * An implementation of numeric values using 64-bit signed integers.
 * This class should only be used if the number cannot be represented by a
 * 32-bit signed integer because conversions to this type fail immediately.
 */
final class ConcreteNumberLong extends AbstractNumber {

    private final long val;

    ConcreteNumberLong(long val) {
        this.val = val;
    }

    @Override
    public boolean isLong() {
        return true;
    }

    @Override
    public boolean isDouble() {
        double dval = val;
        return (long) dval == val; // the long might be represented by a double without loss
    }


    @Override
    public long toLong() {
        return val;
    }

    @Override
    public double toDouble() {
        double dval = val;
        if(((long) dval) == val) {
            return dval; // the long can be represented by a double without loss
        } else {
            throw new ArithmeticException("The number cannot be represented by a 64-bit float.");
        }
    }

    @Override
    public Decimal toDecimal() {
        return Decimal.factory(Long.toString(val));
    }

    @Override
    public String toDecimalString() {
        return Long.toString(val);
    }
}
