package styx.core.values;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import styx.Binary;
import styx.Bool;
import styx.Complex;
import styx.StyxException;
import styx.Numeric;
import styx.Pair;
import styx.Text;
import styx.Value;

public abstract class CompiledComplex extends AbstractComplex {

    private Complex complex;

    // TODO (implement): Always call ctor with complex, then remove this ctor
    protected CompiledComplex() {
        this.complex = null;
    }

    protected CompiledComplex(Complex complex) {
        this.complex = complex;
    }

    @Override
    public Iterator<Pair<Value, Value>> iterator() {
        return assureReady().iterator();
    }

    @Override
    public boolean isEmpty() {
        return assureReady().isEmpty();
    }

    @Override
    public boolean hasSingle() {
        return assureReady().hasSingle();
    }

    @Override
    public boolean hasMany() {
        return assureReady().hasMany();
    }

    @Override
    public Value get(Value key) {
        return assureReady().get(key);
    }

    @Override
    public Complex put(Value key, Value val) {
        return assureReady().put(key, val);
    }

    @Override
    public Complex add(Value val) {
        return assureReady().add(val);
    }

    @Override
    public Pair<Value,Value> single() {
        return assureReady().single();
    }

    protected abstract Complex toValue();

    // *** helpers

    protected static Value findMember(Complex value, String member, boolean nonnull) throws StyxException {
        Value child = value.get(text(member));
        if(child != null) {
            return child;
        }
        if(nonnull) {
            throw new StyxException("Member '" + member + "' not found.");
        }
        return null;
    }

    protected static String convToString(Value value) {
        return convToString(value, null);
    }

    protected static String convToString(Value value, String dflt) {
        return value != null ? value.asText().toTextString() : dflt;
    }

    protected static Boolean convToBoolean(Value value, Boolean dflt) {
        return value != null ? (Boolean) value.asBool().toBool() : dflt;
    }

    protected static Integer convToInteger(Value value, Integer dflt) {
        return value != null ? (Integer) value.asNumber().toInteger() : dflt;
    }

    protected static Text convToText(String val) {
        return val == null ? null : text(val);
    }

    protected static Bool convToBoolIf(boolean value, boolean dflt) {
        return value != dflt ? bool(value) : null;
    }

    protected static Numeric convToNumber(Integer val) {
        return val == null ? null : number(val);
    }

    protected static Binary convToBinary(byte[] val) {
        return val == null ? null : binary(val);
    }

    protected static Complex convToComplex(List<? extends Value> list) {
        if(list == null) {
            return null;
        }
        Complex res = complex();
         for(Value elem : list) {
            res = res.add(elem);
        }
         return res;
    }

    // ***

    private Complex assureReady() {
        synchronized(this) {
            if(complex == null) {
                complex = Objects.requireNonNull(toValue(), "The complex value cannot be null.");
            }
            return complex;
        }
    }
}
