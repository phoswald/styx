package styx.db.mmap;

import java.util.Objects;

import styx.Complex;
import styx.Session;
import styx.StyxException;
import styx.Value;
import styx.core.memory.SharedValue;
import styx.core.values.ConcreteComplex;

public final class MappedSharedValue implements SharedValue {

    private final MappedDatabase db;

    /**
     * The address of the last value read or written, used to detect changes in testset() and monitor().
     */
    private long base;

    public MappedSharedValue(MappedDatabase db) {
        this.db = Objects.requireNonNull(db);
    }

    public Complex empty() {
        return new ConcreteComplex(new MappedAvlTree(db, 0));
    }

    @Override
    public SharedValue clone() {
        return new MappedSharedValue(db);
    }

    @Override
    public Value get(Session session) throws StyxException {
        base = db.getRoot();
        return new ConcreteComplex(new MappedAvlTree(db, base));
    }

    @Override
    public void set(Session session, Value value) throws StyxException {
        base = unwrap(value).store();
        db.setRoot(base);
    }

    @Override
    public boolean testset(Session session, Value value) throws StyxException {
        long address = unwrap(value).store();
        if(!db.testAndSetRoot(address, base)) {
            return false;
        }
        base = address;
        return true;
    }

    @Override
    public void monitor(Session session) {
        db.monitorRoot(base);
    }

    private MappedAvlTree unwrap(Value value) throws StyxException {
        if(value == null) {
            return new MappedAvlTree(db, 0);
        }
        if(value.isComplex()) {
            throw new StyxException("Not a complex value.");
        }
        if(value instanceof ConcreteComplex == false) {
            throw new StyxException("Complex value has unsupported implementation of Complex.");
        }
        ConcreteComplex impl = (ConcreteComplex) value;
        if(impl.children() instanceof MappedAvlTree == false) {
            throw new StyxException("Complex value has unsupported implementation of ImmutableSortedMap.");
        }
        return (MappedAvlTree) impl.children();
    }
}
