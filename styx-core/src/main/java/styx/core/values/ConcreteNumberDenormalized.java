package styx.core.values;

import styx.Decimal;
import styx.Numeric;

final class ConcreteNumberDenormalized extends AbstractNumber {

    private final AbstractNumber number;
    private final String         str;

    ConcreteNumberDenormalized(AbstractNumber number, String str) {
        this.number = number;
        this.str    = str;
    }

    @Override
    public boolean isInteger() {
        return number.isInteger();
    }

    @Override
    public boolean isLong() {
        return number.isLong();
    }

    @Override
    public boolean isDouble() {
        return number.isDouble();
    }

    @Override
    public String toTextString() {
        return str;
    }

    @Override
    public int toInteger() {
        return number.toInteger();
    }

    @Override
    public long toLong() {
        return number.toLong();
    }

    @Override
    public double toDouble() {
        return number.toDouble();
    }

    @Override
    public Decimal toDecimal() {
        return number.toDecimal();
    }

    @Override
    public String toDecimalString() {
        return number.toDecimalString();
    }

    @Override
    public boolean normalized() {
        return false;
    }

    @Override
    public Numeric normalize() {
        return number;
    }
}
