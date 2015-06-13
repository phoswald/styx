package styx;

/**
 * The public interface of numeric values.
 * <p>
 * A numeric value is a rational number.
 */
public interface Numeric extends Text {

    public boolean isInteger();

    public boolean isLong();

    public boolean isDouble();

    /**
     * Converts the number to a 32-bit signed integer value.
     * @return an int that is equal to the number.
     * @throws ArithmeticException if the number cannot be represented exactly by an int.
     */
    public int toInteger();

    /**
     * Converts the number to a 64-bit signed integer value.
     * @return a long that is equal to the number.
     * @throws ArithmeticException if the number cannot be represented exactly by a long.
     */
    public long toLong();

    /**
     * Converts the number to a 64-bit floating point value.
     * @return a double that is equal to the number.
     * @throws ArithmeticException if the number cannot be represented exactly by a double.
     */
    public double toDouble();

    /**
     * Converts the number to a decimal value.
     * @return a decimal value that is equal to the number.
     */
    public Decimal toDecimal();

    /**
     * Converts the number into a normalized decimal string representation.
     * <p>
     * The returned string is guaranteed to have the following properties:
     * <ul>
     * <li> The returned string has the following format: ['-'] digits [ '.' digits ] [ 'E' ['-'] digits ],
     *      where digits is one or more decimal digits ('0'..'9').
     * <li> If the number can be represented exactly by an int, Integer.parseInt() will succeed on the returned string.
     * <li> If the number can be represented exactly by a long, Long.parseLong() will succeed on the returned string.
     * <li> If the number can be represented exactly by a double, Double.parseDouble() will succeed on the returned string.
     * </ul>
     * @return a decimal string representation of the number.
     */
    public String toDecimalString();

    /**
     * Checks whether the number is normalized.
     * <p>
     * A number is not normalized if:
     * <ul>
     * <li> it has leading zeros or is negative zero
     * <li> it has a trailing zeros in the fractional part
     * <li> it has an exponent between -4 and 19
     * <li> its absolute value is equal to or smaller than 10^-5 or equal to or larger than 10^20 and does not have an exponent
     * <li> its exponent has leading zeros or is negative zero.
     * </ul>
     * @return true if the number is normalized, false if not.
     */
    public boolean normalized();

    /**
     * Converts the number to a normalized number.
     * @return a normalized number.
     */
    public Numeric normalize();
}
