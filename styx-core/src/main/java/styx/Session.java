package styx;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;

/**
 * Public interface of sessions.
 *
 * Sessions are the starting point for working with STYX. A session typically encapsulates a unit of
 * work, which is often a transaction. Session are created by session factories, which are themselves
 * created by session providers.
 *
 * Session instances can hold external resources such as file handles or database connections and
 * therefore have to be closed after usage. In contrast, values created by sessions are lightweight
 * and never have to be closed explicitly. Such values, however, may not be usable after the session
 * that created them has been closed.
 */
public interface Session extends AutoCloseable {

    /**
     * Releases all resources held by this session.
     * <p>
     * Calling this method a object that is already closed is a no-op.
     * @throws StyxException if an error occurred.
     */
    @Override
    public void close() throws StyxException;

    // *** Factories

    /**
     * Returns a textual value for the given unicode string.
     * @param val a string.
     * @return a textual value, never null.
     */
    public Text text(String val);

    /**
     * Returns a void value.
     * @return a value of type void, never null .
     */
    public Void empty();

    /**
     * Returns a boolean value.
     * @return a value of type bool, never null .
     */
    public Bool bool(boolean val);

    /**
     * Returns a numeric value for the given 32-bit signed integer value.
     * @param val an int value.
     * @return a numeric value that exactly represents the given int value, never null.
     */
    public Number number(int val);

    /**
     * Returns a numeric value for the given 64-bit signed integer value.
     * @param val a long value.
     * @return a numeric value that exactly represents the given long value, never null.
     */
    public Number number(long val);

    /**
     * Returns a numeric value for the given 64-bit floating point value.
     * @param val a double value.
     * @return a numeric value that exactly represents the given double value, never null.
     * @throws ArithmeticException if the given double value was INF or NAN.
     */
    public Number number(double val);

    /**
     * Returns a numeric value for the given decimal string.
     * @param val a string of the following format: ['-'] digits [ '.' digits ] [ 'E' ['-'] digits ]
     * @return a numeric value that exactly represents the given string, never null.
     * @throws NumberFormatException if the given string does not have the appropriate format.
     */
    public Number number(String val);

    /**
     * Returns a binary value for the given byte array.
     * @param val a byte array.
     * @return a binary value, never null.
     */
    public Binary binary(byte[] val);

    /**
     * Returns a binary value for the given hex string.
     * @param val a string with an even number of upper case hex digits (0..9, A..F, possibly empty).
     * @return a binary value, never null.
     * @throws IllegalArgumentException if the given string does not have the appropriate format.
     */
    public Binary binary(String val);

    /**
     * Returns a reference to the root, on which child() can be called to obtain other references.
     * @return a reference value with level() == 0, never null .
     */
    public Reference root();

    /**
     * Returns an empty complex value, on which put() can be called to construct other complex values.
     * @return an empty complex value, never null.
     */
    public Complex complex();

    /**
     * Returns a type instance for the given type definition.
     * @param definition the type definition, must not be null.
     * @return a type instance, never null.
     * @throws StyxException if the given definition is not valid.
     * @throws NullPointerException if the given definition is null.
     */
    public Type type(Value definition) throws StyxException;

    /**
     * Returns a function instance for the given function definition.
     * @param definition the function definition, must not be null.
     * @return a function instance, never null.
     * @throws StyxException if the given definition is not valid.
     * @throws NullPointerException if the given definition is null.
     */
    public Function function(Value definition) throws StyxException;

    // *** References

    /**
     * Reads the value of the given reference.
     * @param ref the reference, whose value is to be read, must not be null.
     * @return the current value of the given reference, or null if the given reference currently has no value.
     * @throws StyxException if a storage access occurs, or if the given reference does not exist.
     * @throws NullPointerException if the given reference is null.
     */
    public Value read(Reference ref) throws StyxException;

    /**
     * Writes the value of the given reference
     * @param ref the reference, whose value is to be written, must not be null.
     * @param val the new value for the given reference, can be null if the current value is to be removed.
     * @throws StyxException if a storage access occurs, or if the given reference does not exist.
     * @throws NullPointerException if the given reference is null.
     */
    public void write(Reference ref, Value val) throws StyxException;

    /**
     * Browses the keys of the children of the given reference.
     * @param ref the reference, whose children are to be browsed, must not be null.
     * @return the sorted list of the given reference's children's keys.
     * @throws StyxException if a storage access occurs, or if the given reference does not exist.
     * @throws NullPointerException if the given reference is null.
     */
    public List<Value> browse(Reference ref) throws StyxException;

    /**
     * Browses the keys of the children of the given reference.
     * @param ref the reference, whose children are to be browsed, must not be null.
     * @param after optionally excludes all children up to (and including) this value from the result.
     * @param before optionally excludes all children starting from (and including) this value from the result.
     * @param maxResults optionally restricts the number of results to be returned.
     * @param forward true for normal order (from first to last), false for reverse order (from last to first)
     * @return the sorted and optionally restricted list of the given reference's children's keys.
     * @throws StyxException if a storage access occurs, or if the given reference does not exist.
     * @throws NullPointerException if the given reference is null.
     */
    public List<Value> browse(Reference ref, Value after, Value before, Integer maxResults, boolean forward) throws StyxException;

    // *** Transactions

    /**
     * Determines whether there currently is an open transaction.
     * <p>
     * Transactions must be opened and closed explicitly.
     * A transaction is opened by a successful call to beginTransaction()
     * and closed by a call to commitTransaction() or abortTransaction().
     * <p>
     * If an error occurs during reading or writing, an open transaction remains open (but it may fail to commit later).
     * @return true iff a transaction has been opened and not yet closed.
     */
    public boolean hasTransaction();

    /**
     * Opens a new transaction.
     * <p>
     * Transactions cannot be nested.
     * If no exception is thrown, it is guaranteed that a new transaction has been opened.
     * If an exception is thrown, it is guaranteed that no transaction has been opened.
     * @throws StyxException if a new transaction could not be opened.
     */
    public void beginTransaction() throws StyxException;

    /**
     * Commits the current transaction.
     * <p>
     * After this method returns (either normally or with an exception),
     * it is guaranteed that there is no open transaction.
     * @throws StyxException if the transaction failed to commit, or if there was no open transaction.
     */
    public void commitTransaction() throws StyxException;

    /**
     * Aborts the current transaction.
     * <p>
     * After this method returns (either normally or with an exception),
     * it is guaranteed that there is no open transaction.
     * <p>
     * The timeout, if any, is chosen by the implementation. It is not guaranteed that the data
     * actually has been changed upon return of this method and implementations are free to implement
     * polling or fixed, unconditional delays if the storage location cannot be monitored.
     * @param retry normally false. If true, this method waits until the data read by the current
     *              transaction has been changed by another session or until a timeout has expired.
     * @throws StyxException if the transaction failed to abort, or if there was no open transaction.
     */
    public void abortTransaction(boolean retry) throws StyxException;

    // *** Streams

    public String serialize(Value val, boolean indent) throws StyxException;

    public void serialize(Value val, Path file, boolean indent) throws StyxException;

    public void serialize(Value val, OutputStream stm, boolean indent) throws StyxException;

    public void serialize(Value val, Writer stm, boolean indent) throws StyxException;

    public Value deserialize(String str) throws StyxException;

    public Value deserialize(Path file) throws StyxException;

    public Value deserialize(InputStream stm) throws StyxException;

    public Value deserialize(Reader stm) throws StyxException;

    // *** Interpreter

    public Function parse(String script) throws StyxException;

    public Function parse(String script, boolean compile) throws StyxException;

    public Value evaluate(String script) throws StyxException;
}
