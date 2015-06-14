package styx;

import java.util.Collection;

/**
 * The public interface of complex values.
 *
 * A complex value is an ordered set of key/value pairs, where the key and the value
 * of each pair can again be a values of any type (simple, reference or complex).
 */
public interface Complex extends Value, Iterable<Pair<Value,Value>> {

    /**
     * Checks whether the complex value has no children.
     * @return true if the complex value has no children, false otherwise.
     */
    public boolean isEmpty();

    /**
     * Checks whether the complex value has exactly one child.
     * @return true if the complex value has exactly one child, false otherwise.
     */
    public boolean hasSingle();

    /**
     * Checks whether the complex value has more than one child.
     * @return true if the complex value has more than one child, false otherwise.
     */
    public boolean hasMany();

    /**
     * Gets the value for the given key.
     * @param key the key for the value, must not be null.
     * @return the value or null if there is no such value.
     */
    public Value get(Value key);

    /**
     * Sets the value for the given key (insert, replace or remove).
     * @param key the key to be set, must not be null.
     * @param val the value to be set, if null the value is removed.
     * @return the modified complex value, never null.
     */
    public Complex put(Value key, Value val);

    public Complex putAll(Collection<Pair<Value,Value>> pairs);

    public Complex add(Value val);

    public Complex addAll(Collection<Value> vals);

    /**
     * Returns the single child of the complex value.
     * @return the child if the complex value has exactly one child, null otherwise.
     */
    public Pair<Value,Value> single();
}
