package styx.db.mmap;

import java.util.Objects;

import styx.Session;
import styx.StyxException;
import styx.Value;
import styx.core.memory.SharedValue;

public final class MmapSharedValue implements SharedValue {

    private final MmapDatabase db;

    /**
     * The address of the last value read or written, used to detect changes in testset() and monitor().
     */
    private long base;

    public MmapSharedValue(MmapDatabase db) {
        this.db = Objects.requireNonNull(db);
    }

    @Override
    public SharedValue clone() {
        return new MmapSharedValue(db);
    }

    @Override
    public Value get(Session session) throws StyxException {
        base = db.getRoot();
        return db.loadValue(base);
    }

    @Override
    public void set(Session session, Value value) throws StyxException {
        base = db.storeValue(value);
        db.setRoot(base);
    }

    @Override
    public boolean testset(Session session, Value value) throws StyxException {
        long address = db.storeValue(value);
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
}
