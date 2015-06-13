package styx.core.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import styx.Pair;
import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Value;
import styx.core.DataProvider;
import styx.core.utils.IdentityWrapper;
import styx.core.utils.SessionUtils;

/**
 * Provides mutable values as a in-memory tree of objects.
 * <p>
 * MemoryData does not support transactions. For transactions, the class SharedMemoryData can be used instead.
 */
public final class MemoryData implements DataProvider {

    /**
     * The root object, never null.
     */
    private final MemoryObject root;

    /**
     * A fast cache to optimize repeated access to a Reference using the same reference instance.
     * <p>
     * When a Reference is accessed with multiple reference instances, there will be cache misses.
     * In this case, MemoryObject still ensures that there is only one object instance for all equal references.
     */
    private final Map<IdentityWrapper<Reference>, MemoryObject> cache;

    /**
     * Constructs a new object tree.
     * @param val the value of the root object, can be null.
     */
    public MemoryData(Value val) {
        root = new MemoryObject(val);
        cache = new HashMap<>();
    }

    /**
     * Reads (gets) the value of the root object.
     * @return the value of the root object, can be null.
     */
    public Value get() {
        return root.read();
    }

    @Override
    public void close() { }

    /**
     * Reads (gets) the value of the object with the given reference.
     * @param ref the reference whose value is to be read, must not be null.
     * @return the value of the object, null if not existing.
     */
    @Override
    public Value read(Session session, Reference ref) {
        return lookup(Objects.requireNonNull(ref)).read();
    }

    /**
     * Writes (sets) the value of the object with the given reference.
     * @param ref the reference whose value is to be written, must not be null.
     * @param val value for the object, null if to be removed.
     * @throws StyxException if the parent is non-existing or non-complex.
     */
    @Override
    public void write(Session session, Reference ref, Value val) throws StyxException {
        lookup(Objects.requireNonNull(ref)).write(val);
    }

    @Override
    public List<Value> browse(Session session, Reference ref, Value after, Value before, Integer maxResults, boolean forward) {
        Value value = lookup(Objects.requireNonNull(ref)).read();
        if(value == null || !value.isComplex()) {
            return null;
        }
        List<Value> result = new ArrayList<Value>();
        for(Pair<Value, Value> child : value.asComplex()) {
            result.add(child.key());
        }
        return SessionUtils.filter(result, after, before, maxResults, forward);
    }

    @Override
    public boolean hasTransaction() {
        return false;
    }

    @Override
    public void beginTransaction(Session session) {
        throw new UnsupportedOperationException("This session does not support transactions.");
    }

    @Override
    public void commitTransaction(Session session) {
        throw new UnsupportedOperationException("This session does not support transactions.");
    }

    @Override
    public void abortTransaction(Session session, boolean retry) {
        throw new UnsupportedOperationException("This session does not support transactions.");
    }

    /**
     * Returns the object identified by the given reference.
     * <p>
     * This method returns the same object instance when called multiple times with the same reference instance
     * or with different reference instances that are equal.
     * @param ref the reference to look up, must not be null.
     * @return the requested object, never null.
     */
    private MemoryObject lookup(Reference ref) {
        int          refl = ref.level();
        Reference cur  = ref;
        int          curl = refl;
        MemoryObject obj  = cache.get(new IdentityWrapper<Reference>(cur));
        while(obj == null && cur.parent() != null) {
            cur = cur.parent();
            curl--;
            obj = cache.get(new IdentityWrapper<Reference>(cur));
        }
        if(obj == null) {
            obj = root;
            cache.put(new IdentityWrapper<Reference>(cur), obj);
        }
        while(curl < refl) {
            curl++;
            cur = ref.parent(curl);
            obj = obj.child(cur.name());
            cache.put(new IdentityWrapper<Reference>(cur), obj);
        }
        return obj;
    }
}
