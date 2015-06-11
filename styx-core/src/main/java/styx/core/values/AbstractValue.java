package styx.core.values;

import java.io.StringWriter;

import styx.Binary;
import styx.Bool;
import styx.Complex;
import styx.Function;
import styx.Number;
import styx.Reference;
import styx.StyxException;
import styx.Text;
import styx.Type;
import styx.Value;
import styx.Void;
import styx.core.utils.LimitingWriter;
import styx.core.utils.Serializer;

/**
 * Provides some abstract functionality for implementations of value types.
 *
 * Here, a default implementation some methods is provided. Subclasses must
 * implement the remaining methods. Re-implementing selected methods for better
 * performance is also possible.
 *
 * From the asXYZ() and toXYZ() methods, subtypes must re-implement the methods
 * for the respective type.
 *
 * There are various generic comparison methods. They all return -1, 0 or +1.
 */
public abstract class AbstractValue implements Value {

    @Override
    public boolean equals(Object other) {
        if(other instanceof Value) {
            return compareTo((Value) other) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        StringWriter stm = new StringWriter();
        try {
            Serializer.serialize(this, new LimitingWriter(stm, 1000), false);
        } catch (StyxException e) {
            // this should only be reached if the limit was exceeded.
        }
        return stm.toString();
    }

    @Override
    public boolean isText() {
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isBool() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public boolean isComplex() {
        return false;
    }

    @Override
    public boolean isType() {
        return false;
    }

    @Override
    public boolean isFunction() {
        return false;
    }

    @Override
    public Text asText() {
        throw new ClassCastException("The value is not a text.");
    }

    @Override
    public Void asVoid() {
        throw new ClassCastException("The value is not a void.");
    }

    @Override
    public Bool asBool() {
        throw new ClassCastException("The value is not a bool.");
    }

    @Override
    public Number asNumber() {
        throw new ClassCastException("The value is not a number.");
    }

    @Override
    public Binary asBinary() {
        throw new ClassCastException("The value is not a binary.");
    }

    @Override
    public Reference asReference() {
        throw new ClassCastException("The value is not a reference.");
    }

    @Override
    public Complex asComplex() {
        throw new ClassCastException("The value is not a complex value.");
    }

    @Override
    public Type asType() {
        throw new ClassCastException("The value is not a type.");
    }

    @Override
    public Function asFunction() {
        throw new ClassCastException("The value is not a function.");
    }

    public static Text text(String val) {
        return AbstractText.factory(val);
    }

    public static Bool bool(boolean val) {
        return val ? ConcreteBool.TRUE : ConcreteBool.FALSE;
    }

    public static Void empty() {
        return ConcreteVoid.VOID;
    }

    public static Number number(int val) {
        return AbstractNumber.factory(val);
    }

    public static Number number(long val) {
        return AbstractNumber.factory(val);
    }

    public static Number number(double val) {
        return AbstractNumber.factory(val);
    }

    public static Number number(String val) {
        return AbstractNumber.factory(val);
    }

    public static Binary binary(byte[] val) {
        return ConcreteBinary.factory(val);
    }

    public static Binary binary(String val) {
        return ConcreteBinary.factory(val);
    }

    public static Reference root() {
        return ConcreteReference.ROOT;
    }

    public static Complex complex() {
        return ConcreteComplex.EMPTY;
    }

    /**
     * Compares two possibly null Values.
     * @param a the 1st value to compare.
     * @param b the 2nd value to compare.
     * @return -1 if a < b, +1 if a > b or 0 if a == b, where null is treated smaller than any other value.
     */
    protected static int compare(Value a, Value b) {
        if(a != null && b != null) {
            return a.compareTo(b);
        } else {
            return compareInteger(a == null ? 0 : 1, b == null ? 0 : 1);
        }
    }

    /**
     * Compares two signed 32-bit integer numbers.
     * @param a the 1st value to compare.
     * @param b the 2nd value to compare.
     * @return -1 if a < b, +1 if a > b or 0 if a == b
     */
    protected static int compareInteger(int a, int b) {
        if(a != b) {
            return a < b ? -1 : 1;
        }
        return 0;
    }
}
