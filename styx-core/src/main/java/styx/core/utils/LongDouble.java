package styx.core.utils;

//import styx.StyxRuntimeException;

/*
public final class LongDouble {

    int  sign     = 0;
    int  exponent = 0;
    long mantissa = 0;

    public LongDouble(long val) {
        if(val == Long.MIN_VALUE) {
            sign     = -1;
            exponent = 63;
            mantissa = 1L << 63;
        } else if(val != 0) {
            sign     = val < 0 ? -1 : 1;
            exponent = 63;
            mantissa = val > 0 ? val : -val;
            if((mantissa & 0xFFFFFFFF00000000L) == 0) {
                mantissa <<= 32;
                exponent  -= 32;
            }
            if((mantissa & 0xFFFF000000000000L) == 0) {
                mantissa <<= 16;
                exponent  -= 16;
            }
            if((mantissa & 0xFF00000000000000L) == 0) {
                mantissa <<= 8;
                exponent  -= 8;
            }
            if((mantissa & 0xF000000000000000L) == 0) {
                mantissa <<= 4;
                exponent  -= 4;
            }
            if((mantissa & 0xC000000000000000L) == 0) {
                mantissa <<= 2;
                exponent  -= 2;
            }
            if((mantissa & 0x8000000000000000L) == 0) {
                mantissa <<= 1;
                exponent  -= 1;
            }
        }
    }

    public LongDouble(double val) {
        if(Double.isInfinite(val) || Double.isNaN(val)) {
            throw new StyxRuntimeException("The value is Infinity or NaN.");
        }
        if(val != 0.0) {
            mantissa = Double.doubleToLongBits(val);
            sign     =        (mantissa & 0x8000000000000000L) != 0 ? -1 : 1;
            exponent = (int) ((mantissa & 0x7ff0000000000000L) >> 52L) - 1023;
            mantissa =        (mantissa & 0x000fffffffffffffL
                                        | 0X0010000000000000L) << 11;
        }
    }

    @Override
    public String toString() {
        String bits = Long.toBinaryString(mantissa);
        return sign + " * " + bits.substring(0, 1) + "." + bits.substring(1) + " * 2^" + exponent;
    }

    public static int compare(LongDouble a, LongDouble b) {
        if(a.sign != b.sign) {
            return a.sign < b.sign ? -1 : 1;
        }
        if(a.exponent != b.exponent) {
            return (a.exponent < b.exponent ? -1 : 1) * a.sign;
        }
        if(a.mantissa != b.mantissa) {
            return (unsignedLess(a.mantissa, b.mantissa) ? -1 : 1) * a.sign;
        }
        return 0;
    }

    private static boolean unsignedLess(long a, long b) {
        return (a < b) ^ ((a < 0) != (b < 0));
    }
}
*/