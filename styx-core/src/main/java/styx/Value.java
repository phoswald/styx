package styx;


/**
 * The public interface of values of any type.
 *
 * The isXYZ() methods let you determine whether a value can be cast to a subtype.
 * They provide a replacement for Java's "instanceof" operator.
 *
 * The asXYZ() methods let you cast a value to a subtype.
 * They provide a replacement for Java casts, i.e. "(XYZ) val".
 *
 * There is a global sort order for all values (text < reference < complex) and
 * (number < binary < other text, where void and bool are treated as other text).
 */
public interface Value extends Comparable<Value> {

    /**
     * Checks whether the the value can be cast to type text.
     * @return true if the value is of type text (including void, bool and number), false otherwise.
     */
    public boolean isText();

    /**
     * Checks whether the the value can be cast to type void.
     * @return true if the value is of type void (and text), false otherwise.
     */
    public boolean isVoid();

    /**
     * Checks whether the the value can be cast to type bool.
     * @return true if the value is of type bool (and text), false otherwise.
     */
    public boolean isBool();

    /**
     * Checks whether the the value can be cast to type number.
     * @return true if the value is of type number (and text), false otherwise.
     */
    public boolean isNumber();

    /**
     * Checks whether the the value can be cast to type binary.
     * @return true if the value is of type binary (and text), false otherwise.
     */
    public boolean isBinary();

    /**
     * Checks whether the the value can be cast to type reference.
     * @return true if the value is of type reference, false otherwise.
     */
    public boolean isReference();

    /**
     * Checks whether the the value can be cast to type complex.
     * @return true if the value is of type complex, false otherwise.
     */
    public boolean isComplex();

    /**
     * Checks whether the the value can be cast to type type.
     * @return true if the value is of type type, false otherwise.
     */
    public boolean isType();

    /**
     * Checks whether the the value can be cast to type function.
     * @return true if the value is of type function, false otherwise.
     */
    public boolean isFunction();

    /**
     * Casts the value to type text.
     * @return a value of type text, never null.
     * @throws ClassCastException if the value is not of type text.
     */
    public Text asText();

    /**
     * Casts the value to type void.
     * @return a value of type void, never null.
     * @throws ClassCastException if the value is not of type void.
     */
    public Void asVoid();

    /**
     * Casts the value to type bool.
     * @return a value of type bool, never null.
     * @throws ClassCastException if the value is not of type bool.
     */
    public Bool asBool();

    /**
     * Casts the value to type number.
     * @return a value of type number, never null.
     * @throws ClassCastException if the value is not of type number.
     */
    public Numeric asNumber();

    /**
     * Casts the value to type binary.
     * @return a value of type binary, never null.
     * @throws ClassCastException if the value is not of type binary.
     */
    public Binary asBinary();

    /**
     * Casts the value to type reference.
     * @return a value of type reference, never null.
     * @throws ClassCastException if the value is not of type reference.
     */
    public Reference asReference();

    /**
     * Casts the value to type complex.
     * @return a value of type complex, never null.
     * @throws ClassCastException if the value is not of type complex.
     */
    public Complex asComplex();

    /**
     * Casts the value to type type.
     * @return a value of type type, never null.
     * @throws ClassCastException if the value is not of type type.
     */
    public Type asType();

    /**
     * Casts the value to type function.
     * @return a value of type function, never null.
     * @throws ClassCastException if the value is not of type function.
     */
    public Function asFunction();
}
