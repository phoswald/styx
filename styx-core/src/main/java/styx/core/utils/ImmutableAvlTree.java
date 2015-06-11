package styx.core.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import styx.Pair;

public class ImmutableAvlTree<K extends Comparable<K>, V> implements ImmutableSortedMap<K, V> {

    private final K                       key;    // never null
    private final V                       val;    // never null
    private final ImmutableAvlTree<K, V>  left;   // never null, but points to itself for EmptyInstance
    private final ImmutableAvlTree<K, V>  right;  // never null, but points to itself for EmptyInstance
    private final int                     height; // 0 if empty, 1 leaf, ...

    public ImmutableAvlTree() {
        this.key    = null;
        this.val    = null;
        this.left   = this;
        this.right  = this;
        this.height = 0;
    }

    private ImmutableAvlTree(K key, V val, ImmutableAvlTree<K, V> left, ImmutableAvlTree<K, V> right) {
        this.key    = key;
        this.val    = val;
        this.left   = left;
        this.right  = right;
        this.height = 1 + max(left.height, right.height);
    }

    private int balance() {
        return right.height - left.height;
    }

    @Override
    public boolean isEmpty() {
        return height == 0;
    }

    @Override
    public boolean hasSingle() {
        return height == 1;
    }

    @Override
    public boolean hasMany() {
        return height > 1;
    }

    @Override
    public V get(K key) {
        if(key == null)
            throw new IllegalArgumentException();
        return internalGet(this, key);
    }

    @Override
    public ImmutableSortedMap<K, V> put(K key, V val) {
        if(key == null)
            throw new IllegalArgumentException();
        return internalPut(this, key, val);
    }

    @Override
    public Pair<K, V> single() {
        return height == 1 ? new Pair<K, V>(key, val) : null;
    }

    @Override
    public Pair<K, V> find(K key, boolean forward) {
        ImmutableAvlTree<K, V> node = internalFind(this, key, forward);
        if(node.isEmpty())
            return null;
        return new Pair<K, V>(node.key, node.val);
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
    public Iterator<Pair<K, V>> iterator() {
        List<Pair<K, V>> list = new ArrayList<Pair<K, V>>();
        if(!isEmpty())
            internalToList(this, list);
        return list.iterator(); // TODO (optimize): return iterator for tree, not iterator for copied list.
    }

    private static <K extends Comparable<K>, V> V internalGet(ImmutableAvlTree<K, V> node, K key)
    {
        while(!node.isEmpty()) {
            // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
            int cmp = key.compareTo(node.key);
            if(cmp == 0)
                return node.val;
            if(cmp < 0)
                node = node.left;
            else
                node = node.right;
        }
        return null;
    }

    private static <K extends Comparable<K>, V> ImmutableAvlTree<K, V> internalPut(ImmutableAvlTree<K, V> node, K key, V val)
    {
        if(node.isEmpty()) {
            if(val == null)
                return node /* empty */; // remove from empty tree
            else
                return new ImmutableAvlTree<K, V>(key, val, node /* empty */, node /* empty */); // insert into empty tree
        } else {
            int cmp = key.compareTo(node.key);
            if(cmp == 0) {
                if(val == null)
                    return merge(node.left, node.right); // remove
                else
                    return new ImmutableAvlTree<K, V>(key, val, node.left, node.right); // replace
            }
            if(cmp < 0)
                return balance(
                    new ImmutableAvlTree<K, V>(node.key, node.val,
                        internalPut(node.left, key, val),
                        node.right));
            else
                return balance(
                    new ImmutableAvlTree<K, V>(node.key, node.val,
                        node.left,
                        internalPut(node.right, key, val)));
        }
    }

    private static <K extends Comparable<K>, V> ImmutableAvlTree<K, V> internalFind(ImmutableAvlTree<K, V> node, K key, boolean forward)
    {
        if(key == null) { // find first or last
            if(forward) {
                while(!node.left.isEmpty())
                    node = node.left;
            } else {
                while(!node.right.isEmpty())
                    node = node.right;
            }
            return node;
        } else { // find next or previous
            ImmutableAvlTree<K, V> branch = new ImmutableAvlTree<>(); // TODO (optimize): reference to empty node without 'new'
            while(!node.isEmpty()) {
                // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
                int cmp = key.compareTo(node.key);
                if(cmp == 0) {
                    if(forward) {
                        if(node.right.isEmpty())
                            return branch;
                        node = node.right;
                        while(!node.left.isEmpty())
                            node = node.left;
                    } else {
                        if(node.left.isEmpty())
                            return branch;
                        node = node.left;
                        while(!node.right.isEmpty())
                            node = node.right;
                    }
                    return node;
                }
                if(cmp < 0) {
                    if(forward) branch = node;
                    node = node.left;
                } else {
                    if(!forward) branch = node;
                    node = node.right;
                }
            }
            return branch;
        }
    }

    private static <K extends Comparable<K>, V> void internalToList(ImmutableAvlTree<K, V> node, List<Pair<K, V>> list)
    {
        // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
        if(!node.left.isEmpty())
            internalToList(node.left, list);
        list.add(new Pair<K,V>(node.key, node.val));
        if(!node.right.isEmpty())
            internalToList(node.right, list);
    }

    private static <K extends Comparable<K>, V> ImmutableAvlTree<K, V> merge(ImmutableAvlTree<K, V> left, ImmutableAvlTree<K, V> right)
    {
        if(left.isEmpty() && right.isEmpty())
            return left /* empty */; // nothing there to merge
        if(left.isEmpty())
            return right; // trivial merge, only one side
        if(right.isEmpty())
            return left; // trivial merge, only one side
        // Nontrivial merge: pick successor
        // TODO (optimize): pick successor from other subtree more optimal
        ImmutableAvlTree<K, V> succ = right;
        while(!succ.left.isEmpty())
            succ = succ.left;
        return balance(
            new ImmutableAvlTree<K, V>(succ.key, succ.val,
                left,
                internalPut(right, succ.key, null)));
    }

    private static <K extends Comparable<K>, V> ImmutableAvlTree<K, V> balance(ImmutableAvlTree<K, V> node)
    {
        if(node.balance() > 1) {
            if(node.right.balance() <= -1)
                node = rotateLeft(
                    new ImmutableAvlTree<K, V>(node.key, node.val,
                        node.left,
                        rotateRight(node.right)));
            else
                node = rotateLeft(node);
        } else if(node.balance() < -1) {
            if(node.left.balance() >= 1)
                node = rotateRight(
                    new ImmutableAvlTree<K, V>(node.key, node.val,
                        rotateLeft(node.left),
                        node.right));
            else
                node = rotateRight(node);
        }
        // Debug.Assert(node.balance() >= -1 && node.balance() <= 1);
        return node;
    }

    private static <K extends Comparable<K>, V> ImmutableAvlTree<K, V> rotateLeft(ImmutableAvlTree<K, V> node) {
        return new ImmutableAvlTree<K, V>(node.right.key, node.right.val,
            new ImmutableAvlTree<K, V>(node.key, node.val, node.left, node.right.left),
            node.right.right);
    }

    private static <K extends Comparable<K>, V> ImmutableAvlTree<K, V> rotateRight(ImmutableAvlTree<K, V> node) {
        return new ImmutableAvlTree<K, V>(node.left.key, node.left.val,
            node.left.left,
            new ImmutableAvlTree<K, V>(node.key, node.val, node.left.right, node.right));
    }

    private static int max(int a, int b) {
        return a > b ? a : b;
    }
}
