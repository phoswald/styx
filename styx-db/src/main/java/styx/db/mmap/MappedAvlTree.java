package styx.db.mmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import styx.Pair;
import styx.Value;
import styx.core.utils.ImmutableSortedMap;

public class MappedAvlTree implements ImmutableSortedMap<Value, Value> {

    private final MappedDatabase db;

    private long    address;
    private boolean loaded;

    private Value       key;    // never null
    private Value       val;    // never null
    private MappedAvlTree  left;   // never null, but points to itself for EmptyInstance
    private MappedAvlTree  right;  // never null, but points to itself for EmptyInstance
    private int            height; // 0 if empty, 1 leaf, ...

    public MappedAvlTree(MappedDatabase db, long address) {
        this.db      = Objects.requireNonNull(db);
        this.address = address;
    }

    private MappedAvlTree(MappedDatabase db, Value key, Value val, MappedAvlTree left, MappedAvlTree right) {
        this.db      = Objects.requireNonNull(db);
        this.address = 0;
        this.loaded  = true;
        this.key     = key;
        this.val     = val;
        this.left    = left;
        this.right   = right;
        this.height  = 1 + max(left.height, right.height);
    }

    private MappedAvlTree load() {
        if(!loaded) {
//          int flags   = db.getInt(address);
            int flags2  = db.getInt(address +  4);
            int keySize = db.getInt(address +  8);
            int valSize = db.getInt(address + 12);

            key    = deserialize(address + 32,           keySize);
            val    = deserialize(address + 32 + keySize, valSize);
            left   = new MappedAvlTree(db, db.getLong(address + 16));
            right  = new MappedAvlTree(db, db.getLong(address + 24));
            height = flags2;
            loaded = true;
        }
        return this;
    }

    public long store() {
        if(address == 0) {
            int flags  = 0;
            int flags2 = height;
            byte[] keyData = serialize(key);
            byte[] valData = serialize(val);

            address = db.alloc(32 + keyData.length + valData.length);
            db.putInt(address,      flags);
            db.putInt(address +  4, flags2);
            db.putInt(address +  8, keyData.length);
            db.putInt(address + 12, valData.length);
            db.putLong(address + 16, left.store());
            db.putLong(address + 24, right.store());
            db.putArray(address + 32,                  keyData);
            db.putArray(address + 32 + keyData.length, valData);
        }
        return address;
    }

    private Value deserialize(long address, int size) {
        byte[] data = new byte[size];
        db.getArray(address, data);
        return null;
    }

    private byte[] serialize(Value obj) {
        return null;
    }

    private Value key() {
        return load().key;
    }

    private Value val() {
        return load().val;
    }

    private MappedAvlTree left() {
        return load().left;
    }

    private MappedAvlTree right() {
        return load().right;
    }

    private int height() {
        return 0;
    }

    private int balance() {
        return right().height() - left().height();
    }

    @Override
    public boolean isEmpty() {
        return height() == 0;
    }

    @Override
    public boolean hasSingle() {
        return height() == 1;
    }

    @Override
    public boolean hasMany() {
        return height() > 1;
    }

    @Override
    public Value get(Value key) {
        if(key == null)
            throw new IllegalArgumentException();
        return internalGet(this, key);
    }

    @Override
    public ImmutableSortedMap<Value, Value> put(Value key, Value val) {
        if(key == null)
            throw new IllegalArgumentException();
        return internalPut(db, this, key, val);
    }

    @Override
    public Pair<Value, Value> single() {
        return height() == 1 ? new Pair<Value, Value>(key(), val()) : null;
    }

    @Override
    public Pair<Value, Value> find(Value key, boolean forward) {
        MappedAvlTree node = internalFind(db, this, key, forward);
        if(node.isEmpty())
            return null;
        return new Pair<Value, Value>(node.key(), node.val());
    }

//    @Override
//    public IEnumerator<KeyValuePair<KEY, VAL>> getEnumerator(boolean forward) {
//        return new Enumerator(this, forward);
//    }

//    @Override
//    public List<KeyValuePair<KEY, VAL>> toList() {
//        List<KeyValuePair<KEY, VAL>> list = new ArrayList<KeyValuePair<KEY, VAL>>();
//        if(!isEmpty())
//            internalToList(this, list);
//        return list;
//    }

    @Override
    public Iterator<Pair<Value, Value>> iterator() {
        List<Pair<Value, Value>> list = new ArrayList<Pair<Value, Value>>();
        if(!isEmpty())
            internalToList(this, list);
        return list.iterator(); // TODO (optimize): return iterator for tree, not iterator for copied list.
    }

    private static Value internalGet(MappedAvlTree node, Value key)
    {
        while(!node.isEmpty()) {
            // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
            int cmp = key.compareTo(node.key());
            if(cmp == 0)
                return node.val();
            if(cmp < 0)
                node = node.left();
            else
                node = node.right();
        }
        return null;
    }

    private static MappedAvlTree internalPut(MappedDatabase db, MappedAvlTree node, Value key, Value val)
    {
        if(node.isEmpty()) {
            if(val == null)
                return node /* empty */; // remove from empty tree
            else
                return new MappedAvlTree(db, key, val, node /* empty */, node /* empty */); // insert into empty tree
        } else {
            int cmp = key.compareTo(node.key());
            if(cmp == 0) {
                if(val == null)
                    return merge(db, node.left(), node.right()); // remove
                else
                    return new MappedAvlTree(db, key, val, node.left(), node.right()); // replace
            }
            if(cmp < 0)
                return balance(db,
                    new MappedAvlTree(db, node.key(), node.val(),
                        internalPut(db, node.left(), key, val),
                        node.right()));
            else
                return balance(db,
                    new MappedAvlTree(db, node.key(), node.val(),
                        node.left(),
                        internalPut(db, node.right(), key, val)));
        }
    }

    private static MappedAvlTree internalFind(MappedDatabase db, MappedAvlTree node, Value key, boolean forward)
    {
        if(key == null) { // find first or last
            if(forward) {
                while(!node.left().isEmpty())
                    node = node.left();
            } else {
                while(!node.right().isEmpty())
                    node = node.right();
            }
            return node;
        } else { // find next or previous
            MappedAvlTree branch = new MappedAvlTree(db, 0);
            while(!node.isEmpty()) {
                // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
                int cmp = key.compareTo(node.key());
                if(cmp == 0) {
                    if(forward) {
                        if(node.right().isEmpty())
                            return branch;
                        node = node.right();
                        while(!node.left().isEmpty())
                            node = node.left();
                    } else {
                        if(node.left().isEmpty())
                            return branch;
                        node = node.left();
                        while(!node.right().isEmpty())
                            node = node.right();
                    }
                    return node;
                }
                if(cmp < 0) {
                    if(forward) branch = node;
                    node = node.left();
                } else {
                    if(!forward) branch = node;
                    node = node.right();
                }
            }
            return branch;
        }
    }

    private static void internalToList(MappedAvlTree node, List<Pair<Value, Value>> list)
    {
        // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
        if(!node.left().isEmpty())
            internalToList(node.left(), list);
        list.add(new Pair<Value,Value>(node.key(), node.val()));
        if(!node.right().isEmpty())
            internalToList(node.right(), list);
    }

    private static MappedAvlTree merge(MappedDatabase db, MappedAvlTree left, MappedAvlTree right)
    {
        if(left.isEmpty() && right.isEmpty())
            return left /* empty */; // nothing there to merge
        if(left.isEmpty())
            return right; // trivial merge, only one side
        if(right.isEmpty())
            return left; // trivial merge, only one side
        // Nontrivial merge: pick successor
        // TODO (optimize): pick successor from other subtree more optimal
        MappedAvlTree succ = right;
        while(!succ.left().isEmpty())
            succ = succ.left();
        return balance(db,
            new MappedAvlTree(db, succ.key(), succ.val(),
                left,
                internalPut(db, right, succ.key(), null)));
    }

    private static MappedAvlTree balance(MappedDatabase db, MappedAvlTree node)
    {
        if(node.balance() > 1) {
            if(node.right().balance() <= -1)
                node = rotateLeft(db,
                    new MappedAvlTree(db, node.key(), node.val(),
                        node.left(),
                        rotateRight(db, node.right())));
            else
                node = rotateLeft(db, node);
        } else if(node.balance() < -1) {
            if(node.left().balance() >= 1)
                node = rotateRight(db,
                    new MappedAvlTree(db, node.key(), node.val(),
                        rotateLeft(db, node.left()),
                        node.right()));
            else
                node = rotateRight(db, node);
        }
        // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
        return node;
    }

    private static MappedAvlTree rotateLeft(MappedDatabase db, MappedAvlTree node) {
        return new MappedAvlTree(db, node.right().key(), node.right().val(),
            new MappedAvlTree(db, node.key(), node.val(), node.left(), node.right().left()),
            node.right().right());
    }

    private static MappedAvlTree rotateRight(MappedDatabase db, MappedAvlTree node) {
        return new MappedAvlTree(db, node.left().key(), node.left().val(),
            node.left().left(),
            new MappedAvlTree(db, node.key(), node.val(), node.left().right(), node.right()));
    }

    private static int max(int a, int b) {
        return a > b ? a : b;
    }
}
