package styx.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import styx.StyxException;
import styx.Pair;
import styx.Reference;
import styx.Session;
import styx.Value;
import styx.core.DataProvider;
import styx.core.memory.MemoryObject;
import styx.core.utils.SessionUtils;

public class RowDatabaseData implements DataProvider {

    private final RowDatabase db;

    public RowDatabaseData(RowDatabase db) {
        this.db  = db;
    }

    @Override
    public void close() throws StyxException {
        db.close();
    }

    @Override
    public Value read(Session session, Reference ref) throws StyxException {
        Objects.requireNonNull(ref);
        if(db.hasTransaction()) {
            return read2(session, ref);
        } else {
            try(Transaction trans = new Transaction()) {
                Value result = read2(session, ref);
                trans.success();
                return result;
            }
        }
    }

    @Override
    public void write(Session session, Reference ref, Value val) throws StyxException {
        Objects.requireNonNull(ref);
        if(db.hasTransaction()) {
            write2(session, ref, val);
        } else {
            try(Transaction trans = new Transaction()) {
                write2(session, ref, val);
                trans.success();
            }
        }
    }

    @Override
    public List<Value> browse(Session session, Reference ref, Value after, Value before, Integer maxResults, boolean forward) throws StyxException {
        Objects.requireNonNull(ref);
        if(db.hasTransaction()) {
            return browse2(session, ref, after, before, maxResults, forward);
        } else {
            try(Transaction trans = new Transaction()) {
                List<Value> result = browse2(session, ref, after, before, maxResults, forward);
                trans.success();
                return result;
            }
        }
    }

    @Override
    public boolean hasTransaction() {
        return db.hasTransaction();
    }

    @Override
    public void beginTransaction(Session session) throws StyxException {
        db.beginTransaction();
    }

    @Override
    public void commitTransaction(Session session) throws StyxException {
        db.commitTransaction();
    }

    @Override
    public void abortTransaction(Session session, boolean retry) throws StyxException {
        db.abortTransaction(retry);
    }

    private Value read2(Session session, Reference ref) throws StyxException {
        Row      row = lookup(session, ref, false);
        Value val = null;
        if(row != null) {
            val = session.deserialize(row.value);
            if(val.isComplex()) {
                int          topLvl = ref.level(); // same as level(row.parent)
                MemoryObject topObj = new MemoryObject(val);
                Map<String, Value> names = new HashMap<>();
                for(Row descendant : db.selectDescendants(row.key())) {
                    names.put(descendant.key(), session.deserialize(descendant.name));
                    int          descLvl = descendant.level();
                    MemoryObject descObj = topObj;
                    for(int i = topLvl + 1; i <= descLvl; i++) {
                        descObj = descObj.child(names.get(descendant.prefix(i)));
                    }
                    descObj.write(session.deserialize(descendant.value));
                }
                val = topObj.read();
            }
        }
        return val;
    }

    private void write2(Session session, Reference ref, Value val) throws StyxException {
        // Delete old value (and ensure parent exists and is complex)
        Row row = lookup(session, ref, true);
        if(row != null) {
            db.deleteSingle(row.parent, row.name);
            db.deleteDescendants(row.key());
        }
        // Insert new value (if not null)
        if(val != null) {
            insert(session, row, val);
        }
    }

    private List<Value> browse2(Session session, Reference ref, Value after, Value before, Integer maxResults, boolean forward) throws StyxException {
        Row row = lookup(session, ref, false);
        if(row == null || row.value.equals("[]") == false) {
            return null;
        }
        List<Value> result = new ArrayList<>();
        for(Row child : db.selectChildren(row.key())) {
            result.add(session.deserialize(child.name));
        }
        Collections.sort(result);
        return SessionUtils.filter(result, after, before, maxResults, forward);
    }

    private void insert(Session session, Row row, Value val) throws StyxException {
        if(val.isComplex()) {
            db.insert(row.parent, row.name, row.suffix, "[]");
            int suffix = 1;
            for(Pair<Value,Value> child : val.asComplex()) {
                insert(session, new Row(row.key(), session.serialize(child.key(), false), suffix, null), child.val());
                suffix++;
            }
        } else {
            db.insert(row.parent, row.name, row.suffix, session.serialize(val, false));
        }
    }

    private Row lookup(Session session, Reference ref, boolean create) throws StyxException {
        // TODO (optimize): could be cached when transacted
        String parent = "";
        String name   = "";
        if(ref.parent() != null) {
            Row prow = lookup(session, ref.parent(), false);
            if(create) {
                if(prow == null) {
                    throw new StyxException("Attempt to write a child of a non-existing value.");
                } else if(!prow.value.equals("[]")) {
                    throw new StyxException("Attempt to write a child of a non-complex value.");
                }
            } else if(prow == null) {
                return null;
            }
            parent = prow.key();
            name   = session.serialize(ref.name(), false);
        }
        Row row = db.selectSingle(parent, name);
        if(row == null && create) {
            int suffix = 0;
            if(parent.length() > 0) {
                suffix = db.selectMaxSuffix(parent) + 1;
            }
            row = new Row(parent, name, suffix, null);
            db.insert(row.parent, row.name, row.suffix, row.value); // TODO (optimize-): will be deleted immediately in write2()
        }
        return row;
    }

    private final class Transaction implements AutoCloseable {
        private boolean success;

        public Transaction() throws StyxException {
            db.beginTransaction();
        }

        public void success() {
            success = true;
        }

        @Override
        public void close() throws StyxException {
            if(success) {
                db.commitTransaction();
            } else {
                db.abortTransaction(false);
            }
        }
    }
}
