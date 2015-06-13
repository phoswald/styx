package styx.core.memory;

import java.util.List;

import styx.ConcurrentException;
import styx.StyxException;
import styx.Reference;
import styx.Session;
import styx.Value;
import styx.core.DataProvider;

/**
 * Provides mutable values as a in-memory tree of objects.
 * <p>
 * The in-memory tree of mutable objects is provided by the class MemoryData.
 * This class adds support for transactions and for storing the shared (root) value in an external location.
 */
public final class SharedMemoryData implements DataProvider {

    private SharedValue state;
    private MemoryData  txn;
    private boolean     dirty;

    public SharedMemoryData(SharedValue state) {
         this.state = state;
    }

    @Override
    public void close() { }

    @Override
    public Value read(Session session, Reference ref) throws StyxException {
        if(txn != null) {
            return txn.read(session, ref);
        } else {
            try(MemoryData temp = new MemoryData(state.get(session))) {
                return temp.read(session, ref);
            }
        }
    }

    @Override
    public void write(Session session, Reference ref, Value val) throws StyxException {
        if(txn != null) {
            dirty = true;
            txn.write(session, ref, val);
        } else {
            if(ref.level() == 0) {
                state.set(session, val);
            } else {
                Value rootval;
                do {
                    try(MemoryData temp = new MemoryData(state.get(session))) {
                        temp.write(session, ref, val);
                        rootval = temp.get();
                    }
                } while(!state.testset(session, rootval));
            }
        }
    }

    @Override
    public List<Value> browse(Session session, Reference ref, Value after, Value before, Integer maxResults, boolean forward) throws StyxException {
        if(txn != null) {
            return txn.browse(session, ref, after, before, maxResults, forward);
        } else {
            try(MemoryData temp = new MemoryData(state.get(session))) {
                return temp.browse(session, ref, after, before, maxResults, forward);
            }
        }
    }

    @Override
    public boolean hasTransaction() {
        return txn != null;
    }

    @Override
    public void beginTransaction(Session session) throws StyxException {
        // Note: multiple calls are silently ignored.
        if(txn == null) {
            txn = new MemoryData(state.get(session));
        }
    }

    @Override
    public void commitTransaction(Session session) throws StyxException {
        // Note: multiple calls are silently ignored.
        if(txn != null) {
            MemoryData t = txn;
            txn = null; // no need to close, it's a no-op.
            if(dirty) {
                dirty = false;
                if(!state.testset(session, t.get())) {
                    throw new ConcurrentException("The data has been changed by another transaction.");
                }
            }
        }
    }

    @Override
    public void abortTransaction(Session session, boolean retry) {
        // Note: multiple calls are silently ignored.
        if(txn != null) {
            txn = null; // no need to close, it's a no-op.
            dirty = false;
            if(retry) {
                state.monitor(session);
            }
        }
    }
}
