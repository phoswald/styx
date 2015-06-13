package styx.core.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import styx.Complex;
import styx.Numeric;
import styx.Pair;
import styx.StyxException;
import styx.Text;
import styx.Type;
import styx.Value;

public final class Simple extends AbstractType {

    public static final String TAG = Simple.class.getSimpleName();

    private final boolean   nonnull;

//  private final boolean   isVoid;
//  private final boolean   isBool;
    private final boolean   isNumber;
    private final boolean   isNumberInt32;
    private final boolean   isNumberInt64;
    private final boolean   isNumberFloat64;
    private final boolean   isBinary;

    private final Integer   textMaxLength;
    private final String    textCharEnum;
    private final String[]  textEnum;

//  private final Integer   numberMaxPrecision;

    private final Integer   binaryMaxLength;
    private final byte[]    binaryByteEnum;
    private final byte[][]  binaryEnum;

    public Simple(Complex value) throws StyxException {
        nonnull          = convToBoolean(findMember(value, "non_null",          false), false);
        isNumber         = convToBoolean(findMember(value, "is_number",         false), false);
        isNumberInt32    = convToBoolean(findMember(value, "is_number_int32",   false), false);
        isNumberInt64    = convToBoolean(findMember(value, "is_number_int64",   false), false);
        isNumberFloat64  = convToBoolean(findMember(value, "is_number_float64", false), false);
        isBinary         = convToBoolean(findMember(value, "is_binary",         false), false);
        textMaxLength    = convToInteger(findMember(value, "text_max_length",   false), null);
        textCharEnum     = convToString (findMember(value, "text_char_enum",    false));
        textEnum         = convToStringA(findMember(value, "text_enum",         false));
        binaryMaxLength  = convToInteger(findMember(value, "binary_max_length", false), null);
        binaryByteEnum   = convToByteA  (findMember(value, "binary_byte_enum",  false));
        binaryEnum       = convToByteAA (findMember(value, "binary_enum",       false));
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG), complex()
                .put(text("non_null"),          convToBoolIf (nonnull,         false))
                .put(text("is_number"),         convToBoolIf (isNumber,        false))
                .put(text("is_number_int32"),   convToBoolIf (isNumberInt32,   false))
                .put(text("is_number_int64"),   convToBoolIf (isNumberInt64,   false))
                .put(text("is_number_float64"), convToBoolIf (isNumberFloat64, false))
                .put(text("is_binary"),         convToBoolIf (isBinary,        false))
                .put(text("text_max_length"),   convToNumber (textMaxLength))
                .put(text("text_char_enum"),    convToText   (textCharEnum))
                .put(text("text_enum"),         convToComplex(textEnum))
                .put(text("binary_max_length"), convToNumber (binaryMaxLength))
                .put(text("binary_byte_enum"),  convToBinary (binaryByteEnum))
                .put(text("binary_enum"),       convToComplex(binaryEnum)));
    }

    @Override
    public boolean assignable(Type type) {
        if(type == null || type.definition() instanceof Simple == false) {
            return false;
        }
        Simple typet = (Simple) type.definition();
        if(nonnull && !typet.nonnull) {
            return false;
        }

        if(textMaxLength != null && (typet.textMaxLength == null || textMaxLength < typet.textMaxLength)) {
            return false;
        }
        if(textCharEnum != null && (typet.textCharEnum == null || !containsAll(textCharEnum, typet.textCharEnum))) {
            return false;
        }
        if(textEnum != null && (typet.textEnum == null || !containsAll(textEnum, typet.textEnum))) {
            return false;
        }

        if(isNumber) {
            if(!typet.isNumber) {
                return false;
            }
            if(isNumberInt32 && !typet.isNumberInt32) {
                return false;
            }
            if(isNumberInt64 && !typet.isNumberInt32) {
                return false;
            }
            if(isNumberFloat64 && !typet.isNumberInt32) {
                return false;
            }
        }

        if(isBinary) {
            if(!typet.isBinary) {
                return false;
            }
            if(binaryMaxLength != null && (typet.binaryMaxLength == null || binaryMaxLength < typet.binaryMaxLength)) {
                return false;
            }
            if(binaryByteEnum != null && (typet.binaryByteEnum == null || !containsAll(binaryByteEnum, typet.binaryByteEnum))) {
                return false;
            }
            if(binaryEnum != null && (typet.binaryEnum == null || !containsAll(binaryEnum, typet.binaryEnum))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean validate(Value val) {
        if(val == null) {
            return !nonnull;
        }
        if(!val.isText()) {
            return false;
        }
        Text valt = val.asText();

        String vals = valt.toTextString();
        if(textMaxLength != null && textMaxLength < vals.length()) {
            return false;
        }
        if(textCharEnum != null && !containsAll(textCharEnum, vals)) {
            return false;
        }
        if(textEnum != null && !contains(textEnum, vals)) {
            return false;
        }

        if(isNumber) {
            if(!valt.isNumber()) {
                return false;
            }
            Numeric valn = valt.asNumber();
            if(isNumberInt32 && !valn.isInteger()) {
                return false;
            }
            if(isNumberInt64 && !valn.isLong()) {
                return false;
            }
            if(isNumberFloat64 && !valn.isDouble()) {
                return false;
            }
        }

        if(isBinary) {
            if(!valt.isBinary()) {
                return false;
            }
            byte[] valb = valt.asBinary().toByteArray();
            if(binaryMaxLength != null && binaryMaxLength < valb.length) {
                return false;
            }
            if(binaryByteEnum != null && !containsAll(binaryByteEnum, valb)) {
                return false;
            }
            if(binaryEnum != null && !contains(binaryEnum, valb)) {
                return false;
            }
        }

        return true;
    }

    private static String[] convToStringA(Value value) {
        if(value == null) {
            return null;
        }
        List<String> res = new ArrayList<>();
        for(Pair<Value,Value> child : value.asComplex()) {
            res.add(child.val().asText().toTextString());
        }
        return res.toArray(new String[res.size()]);
    }

    private static byte[] convToByteA(Value value) {
        return value != null ? value.asBinary().toByteArray() : null;
    }

    private static byte[][] convToByteAA(Value value) {
        if(value == null) {
            return null;
        }
        List<byte[]> res = new ArrayList<>();
        for(Pair<Value,Value> child : value.asComplex()) {
            res.add(child.val().asText().asBinary().toByteArray());
        }
        return res.toArray(new byte[res.size()][]);
    }

    private static Complex convToComplex(String[] vals) {
        if(vals == null) {
            return null;
        }
        Complex ary = complex();
        for(String val : vals) {
            ary = ary.add(text(val));
        }
        return ary;
    }

    private static Complex convToComplex(byte[][] vals) {
        if(vals == null) {
            return null;
        }
        Complex ary = complex();
        for(byte[] val : vals) {
            ary = ary.add(binary(val));
        }
        return ary;
    }

    private static <T> boolean contains(T[] ary, T elem) {
        return Arrays.binarySearch(ary, elem) != -1;
    }

    private static <T> boolean containsAll(T[] ary, T[] elems) {
        for(T elem : elems) {
            if(Arrays.binarySearch(ary, elem) == -1) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsAll(byte[] ary, byte[] elems) {
        for(byte elem : elems) {
            if(Arrays.binarySearch(ary, elem) == -1) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsAll(String ary, String elems) {
        char[] ary2 = ary.toCharArray();
        for(int i = 0; i < elems.length(); i++) {
            if(Arrays.binarySearch(ary2, elems.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }
}
