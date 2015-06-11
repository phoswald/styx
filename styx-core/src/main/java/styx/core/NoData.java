package styx.core;

import java.util.List;

import styx.Reference;
import styx.Session;
import styx.Value;

public final class NoData implements DataProvider {

    @Override
    public void close() { }

    @Override
    public Value read(Session session, Reference ref) {
        throw new UnsupportedOperationException("This session does not support mutable data.");
    }

    @Override
    public void write(Session session, Reference ref, Value val) {
        throw new UnsupportedOperationException("This session does not support mutable data.");
    }

    @Override
    public List<Value> browse(Session session, Reference ref, Value after, Value before, Integer maxResults, boolean forward) {
        throw new UnsupportedOperationException("This session does not support mutable data.");
    }

    @Override
    public boolean hasTransaction() {
        return false;
    }

    @Override
    public void beginTransaction(Session session) {
        throw new UnsupportedOperationException("This session does not support mutable data.");
    }

    @Override
    public void commitTransaction(Session session) {
        throw new UnsupportedOperationException("This session does not support mutable data.");
    }

    @Override
    public void abortTransaction(Session session, boolean retry) {
        throw new UnsupportedOperationException("This session does not support mutable data.");
    }
}
