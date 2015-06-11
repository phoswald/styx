package styx.core.values;

import styx.Decimal;

/**
 * A generic implementation of numeric values of virtually unbounded precision and scale.
 *
 * This class should only be used if the number cannot be represented by a 64-bit float
 * or a 32- or 64-bit signed integer because conversions to these types fail immediately.
 */
final class ConcreteNumberUnbounded extends AbstractNumber {

    private final Decimal decimal;

    ConcreteNumberUnbounded(Decimal decimal) {
        this.decimal = decimal;
    }

    @Override
    public Decimal toDecimal() {
        return decimal;
    }

    @Override
    public String toDecimalString() {
        return decimal.toString();
    }
}
