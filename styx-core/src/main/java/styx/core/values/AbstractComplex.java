package styx.core.values;

import java.util.Collection;
import java.util.Iterator;

import styx.Complex;
import styx.Pair;
import styx.Value;

abstract class AbstractComplex extends AbstractValue implements Complex {

    @Override
    public int compareTo(Value other) {
        if(other.isText() || other.isReference()) {
            return 1;
        } else if(other.isComplex()) {
            return compareComplex(this, other.asComplex());
        } else if(other.isType() || other.isFunction()) {
            return -1;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    @Override
    public Complex asComplex() {
        return this;
    }

    @Override
    public Complex putAll(Collection<Pair<Value,Value>> pairs) {
        Complex result = this;
        for(Pair<Value,Value> pair : pairs) {
            result = result.put(pair.key(), pair.val()); // TODO (optimize) putAll()
        }
        return result;
    }

    @Override
    public Complex addAll(Collection<Value> vals) {
        Complex result = this;
        for(Value val : vals) {
            result = result.add(val); // TODO (optimize) putAll()
        }
        return result;
    }

    /**
     * Compares two complex values.
     * @param a the 1st value to compare.
     * @param b the 2nd value to compare.
     * @return -1 if a < b, +1 if a > b or 0 if a == b
     */
    protected static int compareComplex(Complex a, Complex b) {
        if(a == b) {
            return 0;
        }
        Iterator<Pair<Value,Value>> ita = a.iterator();
        Iterator<Pair<Value,Value>> itb = b.iterator();
        while(true) {
            if(!ita.hasNext() && !itb.hasNext()) {
                return 0;
            }
            if(!ita.hasNext()) {
                return -1;
            }
            if(!itb.hasNext()) {
                return 1;
            }
            Pair<Value,Value> ac = ita.next();
            Pair<Value,Value> bc = itb.next();
            int res = ac.key().compareTo(bc.key());
            if(res != 0) {
                return res;
            }
            res = ac.val().compareTo(bc.val());
            if(res != 0) {
                return res;
            }
        }
    }
}
