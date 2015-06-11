package styx.core.values;

import styx.Reference;
import styx.Value;

/**
 * Provides some abstract functionality for implementations of references.
 */
abstract class AbstractReference extends AbstractValue implements Reference {

    @Override
    public int compareTo(Value other) {
        if(other.isText()) {
            return 1;
        } else if(other.isReference()) {
            return compareReference(this, other.asReference());
        } else if(other.isComplex() || other.isType() || other.isFunction()) {
            return -1;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public Reference asReference() {
        return this;
    }

    @Override
    public int level() {
        if(parent() == null) {
            return 0;
        }
        return parent().level() + 1;
    }

    @Override
    public Reference parent(int level) {
        int curlevel = level();
        if(level < 0 || level > curlevel) {
            throw new IndexOutOfBoundsException();
        }
        Reference cur = this;
        while(level < curlevel) {
            cur = cur.parent();
            curlevel--;
        }
        return cur;
    }

    /**
     * Compares two references.
     * @param a the 1st value to compare.
     * @param b the 2nd value to compare.
     * @return -1 if a < b, +1 if a > b or 0 if a == b
     */
    protected static int compareReference(Reference a, Reference b) {
        if(a == b) {
            return 0;
        }
        int alevel = a.level();
        int blevel = b.level();
        for(int i = 1; i <= alevel && i <= blevel; i++) {
            Reference aparent = a.parent(i);
            Reference bparent = b.parent(i);
            if(aparent == bparent) {
                continue;
            }
            int res = aparent.name().compareTo(bparent.name());
            if(res != 0) {
                return res;
            }
        }
        return compareInteger(alevel, blevel);
    }
}
