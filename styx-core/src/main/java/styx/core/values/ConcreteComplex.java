package styx.core.values;

import java.util.Iterator;
import java.util.Objects;

import styx.Complex;
import styx.Pair;
import styx.Value;
import styx.core.utils.ImmutableAvlTree;
import styx.core.utils.ImmutableSortedMap;

/**
 * A simple implementation of complex values.
 */
public final class ConcreteComplex extends AbstractComplex {

    /**
     * The instance that represents the complex value with no children.
     */
    public static final ConcreteComplex EMPTY = new ConcreteComplex(new ImmutableAvlTree<Value, Value>());

    /**
     * The complex part of this value, never null and never empty.
     */
    private final ImmutableSortedMap<Value, Value> children;

    public ConcreteComplex(ImmutableSortedMap<Value, Value> children) {
        this.children = Objects.requireNonNull(children);
    }

    public ImmutableSortedMap<Value, Value> children() {
        return children;
    }

    @Override
    public Iterator<Pair<Value, Value>> iterator() {
        return children.iterator();
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public boolean hasSingle() {
        return children.hasSingle();
    }

    @Override
    public boolean hasMany() {
        return children.hasMany();
    }

    @Override
    public Value get(Value key) {
        return children.get(Objects.requireNonNull(key));
    }

    @Override
    public Complex put(Value key, Value val) {
        return new ConcreteComplex(children.put(Objects.requireNonNull(key), val));
    }

    @Override
    public Complex add(Value val) {
        Pair<Value,Value> last = children.find(null, false);
        int next;
        if(last == null) {
            next = 1;
        } else {
            next = last.key().asNumber().toInteger() + 1;
        }
        return new ConcreteComplex(children.put(AbstractNumber.factory(next), val));
    }

    @Override
    public Pair<Value,Value> single() {
        return children.single();
    }
}
