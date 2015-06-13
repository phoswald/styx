package styx.core.values;

import styx.Decimal;
import styx.Numeric;
import styx.StyxRuntimeException;
import styx.Value;

/**
 * Abstract base class for numeric values.
 *
 * There are concrete subclasses for representations of numbers as int, long, double or with
 * unbounded precision and scale. When creating instances, it must be ensured that the smallest
 * possible representiation is selected (int < long < double < unbounded). This is not only an
 * optimization for later calculations but strictly required for correct behavior because
 * conversions between some representations (i.e. double to int) immediately fail.
 *
 */
abstract class AbstractNumber extends AbstractText implements Numeric {

    private static final int  LONG_LIMIT_EXP = Long.toString(Long.MAX_VALUE).length() - 1; // the largest exponent that can possibly fit.
    private static final long LONG_LIMIT_ADD = Long.MIN_VALUE;      // the largest value to which we can add 10 without overflow (but negative)
    private static final long LONG_LIMIT_MUL = Long.MIN_VALUE / 10; // the largest value we can multiple with 10 without overflow (but negative)

    @Override
    public int compareTo(Value other) {
        if(other.isText()) {
            if(other.isNumber()) {
                return compareNumber(this, other.asNumber());
            } else {
                return -1;
            }
        } else if(other.isReference() || other.isComplex() || other.isType() || other.isFunction()) {
            return -1;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public Numeric asNumber() {
        return this;
    }

    @Override
    public String toTextString() {
        return toDecimalString(); // overridden by ConcreteNumberDenormalized
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public boolean isLong() {
        return false;
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public int toInteger() {
        throw new ArithmeticException("The number cannot be represented by a 32-bit signed integer.");    // overridden by some subclasses
    }

    @Override
    public long toLong() {
        throw new ArithmeticException("The number cannot be represented by a 64-bit signed integer.");    // overridden by some subclasses
    }

    @Override
    public double toDouble() {
        throw new ArithmeticException("The number cannot be represented by a 64-bit float."); // overridden by some subclasses
    }

    @Override
    public boolean normalized() {
        return true; // overridden by ConcreteNumberDenormalized
    }

    @Override
    public Numeric normalize() {
        return this; // overridden by ConcreteNumberDenormalized
    }

    public static AbstractNumber factory(int val) {
        return new ConcreteNumberInteger(val);
    }

    public static AbstractNumber factory(long val) {
        if(val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
            return new ConcreteNumberInteger((int) val); // the long can be represented by an int without loss
        } else {
            return new ConcreteNumberLong(val);
        }
    }

    public static AbstractNumber factory(double val) {
        if(Double.isInfinite(val) || Double.isNaN(val)) {
            throw new ArithmeticException("The value is Infinity or NaN.");
        }
        long lval = (long) val;
        if((lval) == val) {
            return factory(lval); // the double can be represented by a long without loss
        } else {
            return new ConcreteNumberDouble(val);
        }
    }

    /**
     * Constructs a new number instance from the given decimal string representation.
     * @param str a string of the following format: ['-'] digits [ '.' digits ] [ 'E' ['-'] digits ],
     *            where digits is one or more decimal digits ('0'..'9').
     * @throws NumberFormatException if the given string does not have the appropriate format.
     */
    public static AbstractNumber factory(String str) {
        return factory(str, false);
    }

    /**
     * Constructs a new number instance from the given decimal string representation.
     * @param str a string of the following format: ['-'] digits [ '.' digits ] [ 'E' ['-'] digits ],
     *             where digits is one or more decimal digits ('0'..'9').
     * @param nothrow indicates whether null shall be returned instead of throwing an exception
     *                if the given string does not have the appropriate format.
     * @throws NumberFormatException if the given string does not have the appropriate format.
     */
    public static AbstractNumber factory(String str, boolean nothrow) {
        Decimal decimal = Decimal.factory(str, nothrow);
        if(decimal  == null) {
            return null;
        }

        AbstractNumber number = null;
        if(decimal.sign() == 0) {
            // no digits in mantissa, must be kind of zero
            number = factory(0);
        } else {
            int scale = decimal.scale();
            if(scale <= 0 && decimal.exponent() <= LONG_LIMIT_EXP) {
                // Integral, try whether representable by long (or int)
                // Note: We try to construct a negative long because a positive long cannot contain MIN_VALUE
                int  sgn = decimal.sign();
                int  len = decimal.precision();
                long val = 0;
                int  pos = 0;
                int  scl = scale;
                while(pos < len) {
                    int digit = decimal.digitAt(pos);
                    if(val < LONG_LIMIT_MUL) {
                        break;
                    }
                    val *= 10;
                    if((sgn < 0 && val < LONG_LIMIT_ADD     + digit) ||
                       (sgn > 0 && val < LONG_LIMIT_ADD + 1 + digit)) {
                        break;
                    }
                    val -= digit;
                    pos++;
                }
                while(scl < 0) {
                    if(val < LONG_LIMIT_MUL) {
                        break;
                    }
                    val *= 10;
                    scl++;
                }
                if(pos == len && scl == 0) {
                    number = factory(sgn < 0 ? val : -val);
                }
            }
            if(number == null) {
                // Not a long or int, try whether representable by double
                // Note: Double.parseDouble() does not throw NumberFormatException if the input is syntactically valid.
                //       The operation succeeds even if the resulting double cannot represent the given string exactly.
                double val = Double.parseDouble(str);
                if(!Double.isInfinite(val)) {
                    Decimal decimal2 = Decimal.factory(Double.toString(val));
                    if(Decimal.compare(decimal, decimal2) == 0) {
                        number = factory(val);
                    }
                }
            }
            if(number == null) {
                number = new ConcreteNumberUnbounded(decimal);
            }
        }
        if(decimal.normalized()) {
            // TODO (optimize--): remove sanity check when done
            String str2 = number.toDecimalString();
            if(!str2.equals(str)) {
                throw new StyxRuntimeException("Sanity check failed (input: '"+str+"', output: '"+str2+"', "+number.getClass()+").");
            }
            return number;
        } else {
            return new ConcreteNumberDenormalized(number, str);
        }
    }

    /**
     * Compares two numeric values (first numerically, then by text if not normalized).
     * @param a the 1st value to compare.
     * @param b the 2nd value to compare.
     * @return -1 if a < b, +1 if a > b or 0 if a == b
     */
    private static int compareNumber(Numeric a, Numeric b) {
        if(a == b) {
            return 0;
        }
        int res = compareNumeric(a, b);
        if(res != 0) {
            return res;
        }
        if(a.normalized() && b.normalized()) {
            return 0;
        } else if(a.normalized()) {
            return -1;
        } else if(b.normalized()) {
            return 1;
        } else {
            return compareText(a, b);
        }
    }

    /**
     * Compares two numeric values (only numerically, not normalized values are considered equal).
     * @param a the 1st value to compare.
     * @param b the 2nd value to compare.
     * @return -1 if a < b, +1 if a > b or 0 if a == b
     */
    private static int compareNumeric(Numeric a, Numeric b) {
        // detect a few past paths
        if(a.isInteger()) {
            if(/*b.isInteger() || */ b.isLong()) {
                return compareLong(a.toLong(), b.toLong());
            } else if(b.isDouble()) {
                return compareDouble(a.toDouble(), b.toDouble());
            }
        } else if(a.isLong()) {
            if(/*b.isInteger() || */ b.isLong()) {
                return compareLong(a.toLong(), b.toLong());
//            } else if(b.isDouble()) {
//                return LongDouble.compare(new LongDouble(a.toLong()), new LongDouble(b.toDouble()));
            }
        } else if(a.isDouble()) {
            if(b.isInteger()) {
                return compareDouble(a.toDouble(), b.toDouble());
//            } else if(b.isLong()) {
//                return LongDouble.compare(new LongDouble(a.toDouble()), new LongDouble(b.toLong()));
            } else if(b.isDouble()) {
                return compareDouble(a.toDouble(), b.toDouble());
            }
        }
        // slow path
        return Decimal.compare(a.toDecimal(), b.toDecimal());
    }

    /**
     * Compares two signed 64-bit integer numbers.
     * @param a the 1st value to compare.
     * @param b the 2nd value to compare.
     * @return -1 if a < b, +1 if a > b or 0 if a == b
     */
    private static int compareLong(long a, long b) {
        if(a != b) {
            return a < b ? -1 : 1;
        }
        return 0;
    }

    /**
     * Compares two 64-bit floating point numbers.
     * @param a the 1st value to compare.
     * @param b the 2nd value to compare.
     * @return -1 if a < b, +1 if a > b or 0 if a == b
     */
    private static int compareDouble(double a, double b) {
        if(a != b) {
            return a < b ? -1 : 1;
        }
        return 0;
    }
}
