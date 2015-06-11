package styx.core.values;

import java.util.Arrays;

import styx.Binary;

/**
 * A simple implementation of binary values.
 */
final class ConcreteBinary extends AbstractText implements Binary {

    public static final Binary EMPTY = new ConcreteBinary(new byte[0]);

    /**
     * The immutable binary value, never null.
     */
    private final byte[] val;

    private ConcreteBinary(byte[] val) {
        this.val = val;
    }

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public Binary asBinary() {
        return this;
    }

    @Override
    public String toTextString() {
        return encodeHex(val, true);
    }

    @Override
    public int byteCount() {
        return val.length;
    }

    @Override
    public byte byteAt(int pos) {
        return val[pos];
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(val, val.length);
    }

    @Override
    public String toHexString() {
        return encodeHex(val, false);
    }

    public static Binary factory(byte[] val) {
        if(val == null || val.length == 0) {
            return EMPTY;
        }
        return new ConcreteBinary(Arrays.copyOf(val, val.length));
    }

    public static Binary factory(String val) {
        return factory(val, 0, val != null ? val.length() : 0, false);
    }

    public static Binary factory(String val, int pos, int len, boolean nothrow) {
        if(len == 0) {
            return EMPTY;
        }
        byte[] res = decodeHex(val, pos, len, nothrow);
        if(res == null) {
            return null;
        }
        return new ConcreteBinary(res);
    }

    private static String encodeHex(byte[] val, boolean prefix) {
        char[] res = new char[val.length * 2 + (prefix ? 2 : 0)];
        int    pos = 0;
        if(prefix) {
            res[0] = '0';
            res[1] = 'x';
            pos += 2;
        }
        for(int i = 0; i < val.length; i++, pos += 2) {
            res[pos  ] = encodeHex((val[i] >> 4) & 0x0f);
            res[pos+1] = encodeHex( val[i]       & 0x0f);
        }
        return new String(res);
    }

    private static byte[] decodeHex(String val, int pos, int len, boolean nothrow) {
        if(len % 2 != 0) {
            if(nothrow) {
                return null;
            } else {
                throw new IllegalArgumentException("The hex string has an odd length.");
            }
        }
        byte[] res = new byte[val != null ? len / 2 : 0];
        for(int i = 0; i < res.length; i++) {
            int hiBits = decodeHex(val.charAt(pos + i*2    ));
            int loBits = decodeHex(val.charAt(pos + i*2 + 1));
            if(hiBits == -1 || loBits == -1) {
                if(nothrow) {
                    return null;
                } else {
                    throw new IllegalArgumentException("The hex string contains an illegal character.");
                }
            }
            res[i] = (byte) ((hiBits << 4) + loBits);
        }
        return res;
    }

    private static char encodeHex(int val) {
        if(val <= 9) {
            return (char) ('0' + val);
        } else {
            return (char) ('A' + val - 10);
        }
    }

    private static int decodeHex(char val) {
        if(val >= '0' && val <= '9') {
            return val - '0';
        }
        if(val >= 'A' && val <= 'F') {
            return val - 'A' + 10;
        }
        return -1;
    }
}
