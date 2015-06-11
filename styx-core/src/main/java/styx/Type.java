package styx;

/**
 * The public interface of types.
 *
 * A type defines the structure of its instances.
 */
public interface Type extends Value {

    /**
     * Returns the type definition for this type.
     * @return a type definition, never null.
     */
    public Value definition();

    /**
     * Checks whether a value of the given type can be assigned to a variable of this type.
     * @param type the type to be validated, must not be null.
     * @return true if assignment is possible, otherwise false.
     */
    public boolean assignable(Type type);

    /**
     * Checks whether the given value can be assigned to a variable of this type.
     * @param val the value to be validated, can be null.
     * @return true if assignment is possible, otherwise false.
     */
    public boolean validate(Value val);
}
