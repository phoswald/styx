package styx.core.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import styx.Complex;
import styx.StyxException;
import styx.StyxRuntimeException;
import styx.Value;

/**
 * A mutable object, part of a tree of such objects and identified by a Reference.
 */
public final class MemoryObject {

    /**
     * The value must be queried from the parent (lazy initialization, top down).
     * <p>
     * This flag is never set for the root object.
     */
    private final static int FLAG_PARENT = 0x01;

    /**
     * The value must be updated by replacing children which have FLAG_MODIFIED set (lazy initialization, bottom up).
     * <p>
     * This flag can occur together with FLAG_PARENT.
     * In this case, the parent has to be queried first, then children have to be replaced.
     */
    private final static int FLAG_CHILD = 0x02;

    /**
     * Used when a parent has FLAG_CHILD set to mark modified children.
     * <p>
     * The flag is set when the parent's onModifiedChild() is called and is cleared when the parent is lazily initialized.
     * This flag is never set for the root object.
     */
    private final static int FLAG_MODIFIED = 0x04;

    /**
     * The parent object, never null except for root.
     */
    private final MemoryObject parent;

    /**
     * The local name, never null except for root.
     */
    private final Value name;

    /**
     * The value of the object, null if there is no value or if FLAG_PARENT is set.
     * Never null for root.
     */
    private Value val;

    /**
     * The current state of the object's value.
     */
    private int flags;

    /**
     * The set of child references, null if none.
     * Can be non-empty for non-complex values (but child references never have values in this case).
     */
    private Map<Value, MemoryObject> children;

    /**
     * Constructs a new root object.
     * @param val the value of the object, can be null.
     */
    public MemoryObject(Value val) {
        this.parent = null;
        this.name   = null;
        this.val    = val;
        this.flags  = 0;
    }

    /**
     * Constructs a new non-root object.
     * @param parent the parent object, must not be null.
     * @param name the local name, must not be null.
     */
    private MemoryObject(MemoryObject parent, Value name) {
        this.parent = parent;
        this.name   = name;
        this.val    = null;
        this.flags  = FLAG_PARENT;
    }

    /**
     * Returns (gets or creates) the child object with the given name.
     * @param name the name of the child object (the last part of its reference), must not be null.
     * @return the requested child object, never null.
     */
    public MemoryObject child(Value name) {
        Objects.requireNonNull(name);
        if(children == null) {
            children = new HashMap<Value, MemoryObject>();
        }
        MemoryObject child = children.get(name);
        if(child == null) {
            child = new MemoryObject(this, name);
            children.put(name, child);
        }
        return child;
    }

    /**
     * Reads (gets) the value of the object.
     * @return the value of the object, null if not existing.
     */
    public Value read() {
        // If FLAG_PARENT is set, query the value from the parent and clear the flag.
        if((flags & FLAG_PARENT) != 0) {
            val = parent.getChild(name);
            flags &= ~FLAG_PARENT;
        }
        // If FLAG_CHILD is set, replace children which have FLAG_MODIFIED set and
        // clear FLAG_MODIFIED from children and FLAG_CHILD from this object.
        if((flags & FLAG_CHILD) != 0) {
            if(val == null || !val.isComplex()) {
                throw new StyxRuntimeException(); // invalid: previous write to non-existing child.
            }
            Complex valc = val.asComplex(); // never called for non-complex values.
            for(MemoryObject child : children.values()) {
                if((child.flags & FLAG_MODIFIED) != 0) {
                    valc = valc.put(child.name, child.read());
                    child.flags &= ~FLAG_MODIFIED;
                }
            }
            val = valc;
            flags &= ~FLAG_CHILD;
        }
        return val;
    }

    /**
     * Writes (sets) the value of the object.
     * @param val value for the object, null if to be removed.
     * @throws StyxException if the parent is non-existing or non-complex.
     */
    public void write(Value val) throws StyxException {
        // Writing is valid only if the parent is complex.
        if(/*val != null &&*/ parent != null) {
            parent.assureComplex();
        }
        // Propagate changes up (if not root).
        // This will set FLAG_CHILD on parents and FLAG_MODIFIED on this object and on parents (recursively).
        if(parent != null) {
            parent.onModifiedChild(this);
            flags |= FLAG_MODIFIED;
        }
        // Propagate changes down (if any children).
        // This will set FLAG_PARENT and clear FLAG_CHILD or FLAG_MODIFIED on all children (recursively).
        if(children != null) {
            for(MemoryObject child : children.values()) {
                child.onModifiedParent();
            }
        }
        this.val = val;
        flags &= ~(FLAG_PARENT | FLAG_CHILD); // clear both flags
    }

    /**
     * Throws an exception if the object is not existing or not complex.
     */
    private void assureComplex() throws StyxException {
        // If FLAG_PARENT is set, query the value from the parent and clear the flag.
        if((flags & FLAG_PARENT) != 0) {
            val = parent.getChild(name);
            flags &= ~FLAG_PARENT;
        }
        if(val == null) {
            throw new StyxException("Attempt to write a child of a non-existing value.");
        } else if(!val.isComplex()) {
            throw new StyxException("Attempt to write a child of a non-complex value.");
        }
    }

    /**
     * Returns the value of the child with the given local name.
     * @param name the local name of the child, never null.
     * @return the value of the child, can be null.
     */
    private Value getChild(Value name) {
        // If FLAG_PARENT is set, query the value from the parent and clear the flag.
        if((flags & FLAG_PARENT) != 0) {
            val = parent.getChild(this.name);
            flags &= ~FLAG_PARENT;
        }
        if(val == null || !val.isComplex()) {
            return null;
        } else {
            return val.asComplex().get(name);
        }
    }

    /**
     * Indicates that a value has been written to an ancestor reference.
     */
    private void onModifiedParent() {
        // If FLAG_PARENT is not set, propagate down and set it.
        // If FLAG_CHILD or FLAG_MODIFIED are set, they become obsolete and are cleared.
        if((flags & FLAG_PARENT) == 0) {
            if(children != null) {
                for(MemoryObject child : children.values()) {
                    child.onModifiedParent();
                }
            }
            val = null;
            flags = FLAG_PARENT; // clear FLAG_CHILD and FLAG_MODIFIED
        }
    }

    /**
     * Indicates that a value has been written to a descendant reference.
     * @param child never null.
     */
    private void onModifiedChild(MemoryObject child) {
        // If FLAG_CHILD is not set, propagate up and set it (recursively).
        // If FLAG_PARENT is set, it still applies and is left unchanged.
        if((flags & FLAG_CHILD) == 0) {
            if(parent != null) {
                parent.onModifiedChild(this);
                flags |= FLAG_MODIFIED;
            }
            flags |= FLAG_CHILD;
        }
    }
}
