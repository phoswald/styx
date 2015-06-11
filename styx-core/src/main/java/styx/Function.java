package styx;

/**
 * The public interface of functions.
 *
 * A function is something that can be invoked.
 */
public interface Function extends Value {

    /**
     * Returns the function definition for this function.
     * @return a function definition, never null.
     */
    public Value definition();

    public Determinism determinism();

    public int argumentCount();

    public Value invoke(Session session, Value[] args) throws StyxException;
}
