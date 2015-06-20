package styx.db.mmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import styx.Pair;
import styx.StyxException;
import styx.Value;
import styx.core.utils.ImmutableSortedMap;

public class MmapAvlTree implements ImmutableSortedMap<Value, Value> {

    private final MmapDatabase db;

    private long address; // (-1 if not stored)

    private Value       key;    // never null (null if not loaded or for empty instance)
    private Value       val;    // never null (null if not loaded or for empty instance)
    private MmapAvlTree left;   // never null, but points to itself for empty instance (null if not loaded)
    private MmapAvlTree right;  // never null, but points to itself for empty instance (null if not loaded)
    private int         height; // 0 for empty instance, 1 leaf, ... (-1 if not loaded)

    public MmapAvlTree(MmapDatabase db, long address) {
        this.db      = Objects.requireNonNull(db);
        this.address = address;
        this.height  = -1;

        if(address == 0) {
            this.left   = this;
            this.right  = this;
            this.height = 0;
        }
    }

    private MmapAvlTree(MmapDatabase db, Value key, Value val, MmapAvlTree left, MmapAvlTree right) {
        this.db      = Objects.requireNonNull(db);
        this.address = -1;
        this.key     = Objects.requireNonNull(key);
        this.val     = Objects.requireNonNull(val);
        this.left    = Objects.requireNonNull(left);
        this.right   = Objects.requireNonNull(right);
        this.height  = 1 + max(left.height(), right.height());
    }

    public long store() throws StyxException {
        if(address == -1) {
            address = db.alloc(40);
//          db.putInt (address,      0 /* not used */);
            db.putInt (address +  4, height);
            db.putLong(address +  8, db.storeValue(key));
            db.putLong(address + 16, db.storeValue(val));
            db.putLong(address + 24, left.store());
            db.putLong(address + 32, right.store());
        }
        return address;
    }

    private Value key() {
        if(key == null && height != 0) {
            key = Objects.requireNonNull(db.loadValue(db.getLong(address +  8)));
        }
        return key;
    }

    private Value val() {
        if(val == null && height != 0) {
            val = Objects.requireNonNull(db.loadValue(db.getLong(address + 16)));
        }
        return val;
    }

    private MmapAvlTree left() {
        if(left == null) {
            left = Objects.requireNonNull(db.getProxy(db.getLong(address + 24)));
        }
        return left;
    }

    private MmapAvlTree right() {
        if(right == null) {
            right = Objects.requireNonNull(db.getProxy(db.getLong(address + 32)));
        }
        return right;
    }

    private int height() {
        if(height == -1) {
            height = db.getInt(address + 4);
        }
        return height;
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
        MmapAvlTree node = internalFind(db, this, key, forward);
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

    private static Value internalGet(MmapAvlTree node, Value key)
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

    private static MmapAvlTree internalPut(MmapDatabase db, MmapAvlTree node, Value key, Value val)
    {
        if(node.isEmpty()) {
            if(val == null)
                return node /* empty */; // remove from empty tree
            else
                return new MmapAvlTree(db, key, val, node /* empty */, node /* empty */); // insert into empty tree
        } else {
            int cmp = key.compareTo(node.key());
            if(cmp == 0) {
                if(val == null)
                    return merge(db, node.left(), node.right()); // remove
                else
                    return new MmapAvlTree(db, key, val, node.left(), node.right()); // replace
            }
            if(cmp < 0)
                return balance(db,
                    new MmapAvlTree(db, node.key(), node.val(),
                        internalPut(db, node.left(), key, val),
                        node.right()));
            else
                return balance(db,
                    new MmapAvlTree(db, node.key(), node.val(),
                        node.left(),
                        internalPut(db, node.right(), key, val)));
        }
    }

    private static MmapAvlTree internalFind(MmapDatabase db, MmapAvlTree node, Value key, boolean forward)
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
            MmapAvlTree branch = db.getProxy(0);
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

    private static void internalToList(MmapAvlTree node, List<Pair<Value, Value>> list)
    {
        // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
        if(!node.left().isEmpty())
            internalToList(node.left(), list);
        list.add(new Pair<Value,Value>(node.key(), node.val()));
        if(!node.right().isEmpty())
            internalToList(node.right(), list);
    }

    private static MmapAvlTree merge(MmapDatabase db, MmapAvlTree left, MmapAvlTree right)
    {
        if(left.isEmpty() && right.isEmpty())
            return left /* empty */; // nothing there to merge
        if(left.isEmpty())
            return right; // trivial merge, only one side
        if(right.isEmpty())
            return left; // trivial merge, only one side
        // Nontrivial merge: pick successor
        // TODO (optimize): pick successor from other subtree more optimal
        MmapAvlTree succ = right;
        while(!succ.left().isEmpty())
            succ = succ.left();
        return balance(db,
            new MmapAvlTree(db, succ.key(), succ.val(),
                left,
                internalPut(db, right, succ.key(), null)));
    }

    private static MmapAvlTree balance(MmapDatabase db, MmapAvlTree node)
    {
        if(node.balance() > 1) {
            if(node.right().balance() <= -1)
                node = rotateLeft(db,
                    new MmapAvlTree(db, node.key(), node.val(),
                        node.left(),
                        rotateRight(db, node.right())));
            else
                node = rotateLeft(db, node);
        } else if(node.balance() < -1) {
            if(node.left().balance() >= 1)
                node = rotateRight(db,
                    new MmapAvlTree(db, node.key(), node.val(),
                        rotateLeft(db, node.left()),
                        node.right()));
            else
                node = rotateRight(db, node);
        }
        // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
        return node;
    }

    private static MmapAvlTree rotateLeft(MmapDatabase db, MmapAvlTree node) {
        return new MmapAvlTree(db, node.right().key(), node.right().val(),
            new MmapAvlTree(db, node.key(), node.val(), node.left(), node.right().left()),
            node.right().right());
    }

    private static MmapAvlTree rotateRight(MmapDatabase db, MmapAvlTree node) {
        return new MmapAvlTree(db, node.left().key(), node.left().val(),
            node.left().left(),
            new MmapAvlTree(db, node.key(), node.val(), node.left().right(), node.right()));
    }

    private static int max(int a, int b) {
        return a > b ? a : b;
    }
}
