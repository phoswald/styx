package styx;

/**
 * The public interface of references.
 *
 * A reference is a path (or address) of a mutable value, where each part of the
 * path can again be a value of any type (simple, complex or reference).
 */
public interface Reference extends Value {

    /**
     * Gets the level of this reference.
     * @return 0 for root, 1 for child of root, etc.
     */
    public int level();

    /**
     * Gets the parent of this reference.
     * @return never null except for root.
     */
    public Reference parent();

    /**
     * Gets the ancestor with the given level.
     * @param level selects the ancestor, 0 for root, 1 for child of root.
     * @return never null except for root, possibly the reference itself.
     * @throws IndexOutOfBoundsException if level is negative or greater than the level of this reference.
     */
    public Reference parent(int level);

    /**
     * Gets the local name of this reference.
     * @return never null except for root.
     */
    public Value name();

    /**
     * Gets the child reference with the given local name.
     * @param name the local name, must not be null.
     * @return never null.
     * @throws NullPointerException if the given local name was null.
     */
    public Reference child(Value name);
}
