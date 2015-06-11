package styx;

/**
 * A decimal number with arbitrary precision and scale.
 * <p>
 * A decimal number consists of a sign, a mantissa of decimal digits and a positive or negative exponent of base 10.
 * <p>
 * number = sign  * digit ['.' digits] * 10^exponent
 * <p>
 * exponent = precision - scale - 1 ('magnitude of the first digit') <br/>
 * precision = scale + exponent + 1 ('total number of decimal digits') <br/>
 * scale = precision - exponent - 1 ('number of decimal digits after the decimal point') <br/>
 */
public final class Decimal {

    private static final byte[] NO_BYTES = new byte[0];

    private final int     sign;
    private final int     exponent;
    private final byte[]  digits;
    private final boolean normalized;

    private Decimal(int sign, int exponent, byte[] digits, boolean normalized) {
//        if(sign < -1 || sign > 1 || digits == null) {
//            throw new StyxRuntimeException();
//        }
//        if(sign == 0 && digits.length != 0 || sign != 0 && digits.length == 0) {
//            throw new StyxRuntimeException();
//        }
        this.sign       = sign;
        this.exponent   = exponent;
        this.digits     = digits;
        this.normalized = normalized;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(sign == 0) {
            sb.append('0');
        } else {
            if(sign == -1) {
                sb.append('-');
            }
            if(exponent > -5 && exponent < 20) {
                for(int i = exponent; i < 0; i++) {
                    if(i == exponent + 1) {
                        sb.append('.');
                    }
                    sb.append('0');
                }
                for(int i = 0; i < digits.length; i++) {
                    if(i == exponent + 1) {
                        sb.append('.');
                    }
                    sb.append((char) ('0' + digits[i]));
                }
                for(int i = digits.length; i <= exponent; i++) {
                    sb.append('0');
                }
            } else {
                for(int i = 0; i < digits.length; i++) {
                    if(i == 1) {
                        sb.append('.');
                    }
                    sb.append((char) ('0' + digits[i]));
                }
                sb.append('E');
                sb.append(exponent);
            }
        }
        return sb.toString();
    }

    /**
     * Returns the sign of the number.
     * @return -1 if negative, 0 if zero, +1 if positive
     */
    public int sign() {
        return sign;
    }

    /**
     * Returns the exponent of the number.
     * Examples:
     * <ul>
     * <li> The number 123 has an exponent of 2 (1.23 * 10^2)
     * <li> The number 1.23 has an exponent of 0 (1.23 * 10^0)
     * <li> The number 0.0123 has an exponent of -2 (1.23 * 10^-2)
     * </ul>
     * @return a positive or negative number: the order of magnitude of the first significant digit.
     */
    public int exponent() {
        return exponent;
    }

    /**
     * Returns the total number of decimal digits.
     * @return a non-negative number: 0 for zero, > 0 for positive or negative numbers.
     */
    public int precision() {
        return digits.length;
    }

    /**
     * Returns the number of decimal digits after the decimal point.
     * Examples:
     * <ul>
     * <li> The number 123 has a scale of 0
     * <li> The number 123.45 has a scale of +2
     * <li> The number 12300 has a scale of -2
     * </ul>
     * @return a positive or negative number: > 0 for fractional numbers, <= 0 for integral numbers.
     */
    public int scale() {
        return digits.length - exponent - 1;
    }

    /**
     * Returns the decimal digit at the given position.
     * @param pos between 0 and precision() - 1.
     * @return a value in the range 0..9
     */
    public int digitAt(int pos) {
        return digits[pos];
    }

    /**
     * Checks whether the number is normalized.
     * @return true if the number is normalized, false if not.
     */
    public boolean normalized() {
        return normalized;
    }

    /**
     * Constructs a new decimal instance from the given decimal string representation.
     * @param str     a string of the following format: ['-'] digits [ '.' digits ] [ 'E' ['-'] digits ],
     *                where digits is one or more decimal digits ('0'..'9').
     * @throws NumberFormatException if the given string does not have the appropriate format.
     */
    public static Decimal factory(String str) {
        return factory(str, false);
    }

    /**
     * Constructs a new decimal instance from the given decimal string representation.
     * @param str     a string of the following format: ['-'] digits [ '.' digits ] [ 'E' ['-'] digits ],
     *                where digits is one or more decimal digits ('0'..'9').
     * @param nothrow indicates whether null shall be returned instead of throwing an exception
     *                if the given string does not have the appropriate format.
     * @throws NumberFormatException if the given string does not have the appropriate format.
     */
    public static Decimal factory(String str, boolean nothrow) {
        if(str == null || str.length() == 0) {
            return fail("The number is null or empty.", nothrow);
        }

        int     len = str.length();
        int     pos = 0; // current offset
        int     posInt;  // offset where the integral part starts
        int     posFrc;  // offset where the fractional part starts
        int     posExp;  // offset where the exponent part starts
        int     numInt;  // number of digits of the integral part
        int     numFrc;  // number of digits of the fractional part
        int     numExp;  // number of digits of the exponential part
        boolean negInt = false; // indicates whether the mantissa is negative
        boolean negExp = false; // indicates whether the exponent is negative
        boolean normal = true;
        int     exponent;

        if(pos < len && str.charAt(pos) == '-') {
            negInt = true;
            pos++;
        }
        posInt = pos;
        while(pos < len && isDigit(str.charAt(pos))) {
            pos++;
        }
        if(pos == posInt) {
            return fail("The number has an illegal format.", nothrow);
        }
        while(posInt < pos && str.charAt(posInt) == '0') {
            posInt++; // skip leading zeros
            if(posInt < pos) {
                normal = false; // allow exactly one
            }
        }
        numInt = pos - posInt;
        exponent = numInt - 1;
        if(pos < len && str.charAt(pos) == '.') {
            pos++;
            posFrc = pos;
            while(pos < len && isDigit(str.charAt(pos))) {
                pos++;
            }
            if(pos == posInt) {
                return fail("The number has an illegal format.", nothrow);
            }
            while(numInt == 0 && posFrc < pos && str.charAt(posFrc) == '0') {
                posFrc++; exponent--; // skip leading zeros in fractional part
            }
            numFrc = pos - posFrc;
            while(numFrc > 0 && str.charAt(posFrc + numFrc - 1) == '0') {
                numFrc--; normal = false; // skip trailing zeros in fractional part
            }
        } else {
            posFrc = -1;
            numFrc = 0;
        }
        while(numFrc == 0 && numInt > 0 && str.charAt(posInt + numInt - 1) == '0') {
            numInt--; // skip trailing zeros in integral part
        }
        if(pos < len && str.charAt(pos) == 'E') {
            pos++;
            if(pos < len && str.charAt(pos) == '-') {
                negExp = true;
                pos++;
            }
            posExp = pos;
            while(pos < len && isDigit(str.charAt(pos))) {
                pos++;
            }
            if(pos == posExp) {
                return fail("The number has an illegal format.", nothrow);
            }
            numExp = pos - posExp;

            int exponent2 = Integer.parseInt(str.substring(posExp, posExp + numExp)) * (negExp ? -1 : 1);
            if((exponent2 == 0 && negExp) || (numInt != 1) || (exponent2 > -5 && exponent2 < 20)) {
                normal = false;
            }
            exponent += exponent2;
        } else {
            if(exponent <= -5 || exponent >= 20) {
                normal = false;
            }
        }
        if(pos < len) {
            return fail("The number has an illegal format.", nothrow);
        }

        byte[] digits;
        if(numInt + numFrc > 0) {
            digits = new byte[numInt + numFrc];
            for(int i = 0; i < numInt; i++) {
                digits[i] = (byte) (str.charAt(posInt + i) - '0');
            }
            for(int i = 0; i < numFrc; i++) {
                digits[numInt + i] = (byte) (str.charAt(posFrc + i) - '0');
            }
        } else {
            digits = NO_BYTES;
            normal = str.equals("0");
        }

        return new Decimal(digits.length == 0 ? 0 : negInt ? -1 : 1, exponent, digits, normal);
    }

    /**
     * Compares two decimal numbers.
     * @param a the 1st value to compare.
     * @param b the 2nd value to compare.
     * @return -1 if a < b, +1 if a > b or 0 if a == b
     * @throws NullPointerException if a or b is null
     */
    public static int compare(Decimal a, Decimal b) {
        int asig = a.sign();
        int bsig = b.sign();
        if(asig != bsig) {
            return asig < bsig ? -1 : 1;
        } else if(asig == 0) {
            return 0;
        }
        int aexp = a.exponent();
        int bexp = b.exponent();
        if(aexp != bexp) {
            return (aexp < bexp ? -1 : 1) * asig;
        }
        int al = a.precision();
        int bl = b.precision();
        for(int i = 0; i < al && i < bl; i++) {
            int a2 = a.digitAt(i);
            int b2 = b.digitAt(i);
            if(a2 != b2) {
                return (a2 < b2 ? -1 : 1) * asig;
            }
        }
        if(al != bl) {
            return (al < bl ? -1 : 1) * asig;
        }
        return 0;
    }

    private static Decimal fail(String message, boolean nothrow) {
        if(nothrow) {
            return null;
        } else {
            throw new NumberFormatException(message);
        }
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}
