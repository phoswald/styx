package styx.core.values;

import styx.Text;
import styx.Value;

/**
 * Provides some abstract functionality for implementations of value type text (including number, binary, void and bool).
 *
 * Here, a default implementation some methods is provided. Subclasses must
 * implement the remaining methods. Re-implementing selected methods for better
 * performance is also possible.
 */
abstract class AbstractText extends AbstractValue implements Text {

    @Override
    public int compareTo(Value other) {
        if(other.isText()) {
            if(other.isNumber()) {
                return 1;
            } else {
                return compareText(this, other.asText());
            }
        } else if(other.isReference() || other.isComplex() || other.isType() || other.isFunction()) {
            return -1;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isText() {
        return true;
    }

    @Override
    public Text asText() {
        return this;
    }

//    @Override
//    public int charCount() {
//        return toTextString().length();
//    }
//
//    @Override
//    public char charAt(int pos) {
//        return toTextString().charAt(pos);
//    }

    @Override
    public char[] toCharArray() {
        return toTextString().toCharArray();
    }

    public static Text factory(String val) {
        if(val == null || val.length() == 0) {
            return ConcreteText.EMPTY;
        }
        if(val.equals("void")) {
            return ConcreteVoid.VOID;
        }
        if(val.equals("false")) {
            return ConcreteBool.FALSE;
        }
        if(val.equals("true")) {
            return ConcreteBool.TRUE;
        }
        if(val.length() >= 1 && (val.charAt(0) == '-' || (val.charAt(0) >= '0' && val.charAt(0) <= '9'))) {
            Text res;
            if(val.length() >= 2 && val.charAt(0) == '0' && val.charAt(1) == 'x') {
                res = ConcreteBinary.factory(val, 2, val.length() - 2, true);
            } else {
                res = AbstractNumber.factory(val, true);
            }
            if(res != null) {
                return res; // return binary or number if parsed successfully, fall back to text otherwise
            }
        }
        return ConcreteText.factory(val);
    }

    protected static int compareText(Text a, Text b) {
        if(a == b) {
            return 0;
        }
        String as = a.toTextString();
        String bs = b.toTextString();
        int al = as.length();
        int bl = bs.length();
        for(int i = 0; i < al && i < bl; i++) {
            char a2 = as.charAt(i);
            char b2 = bs.charAt(i);
            if(a2 != b2) {
                return a2 < b2 ? -1 : 1;
            }
        }
        if(al != bl) {
            return al < bl ? -1 : 1;
        }
        return 0;
    }
}
