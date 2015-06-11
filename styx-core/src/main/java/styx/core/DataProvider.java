package styx.core;

import java.util.List;

import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Value;

/**
 * The interface a STYX session uses for reading and writing mutable values.
 * <p>
 * Each mutable value is identified by a reference.
 * As references are hierarchical, the resulting values have the form of a tree.
 * <p>
 * Each data provider instance is used by only one session.
 */
public interface DataProvider extends AutoCloseable {

    /**
     * Releases all resources held by this data provider.
     * <p>
     * Calling this method a object that is already closed is a no-op.
     * @throws StyxException if an error occurred.
     */
    @Override
    public void close() throws StyxException;

    /**
     * Reads the value of the given reference.
     * @param session the session this data provider belongs to.
     * @param ref the reference, whose value is to be read, must not be null.
     * @return the current value of the given reference, or null if the given reference currently has no value.
     * @throws StyxException if a storage access occurs, or if the given reference does not exist.
     */
    public Value read(Session session, Reference ref) throws StyxException;

    /**
     * Writes the value of the given reference
     * @param session the session this data provider belongs to.
     * @param ref the reference, whose value is to be written, must not be null.
     * @param val the new value for the given reference, can be null if the current value is to be removed.
     * @throws StyxException if a storage access occurs, or if the given reference does not exist.
     */
    public void write(Session session, Reference ref, Value val) throws StyxException;

    /**
     * Browses the keys of the children of the given reference.
     * @param session the session this data provider belongs to.
     * @param ref the reference, whose children are to be browsed, must not be null.
     * @param after optionally excludes all children up to (and including) this value from the result.
     * @param before optionally excludes all children starting from (and including) this value from the result.
     * @param maxResults optionally restricts the number of results to be returned.
     * @param forward true for normal order (from first to last), false for reverse order (from last to first)
     * @return the sorted and optionally restricted list of the given reference's children's keys.
     * @throws StyxException if a storage access occurs, or if the given reference does not exist.
     */
    public List<Value> browse(Session session, Reference ref, Value after, Value before, Integer maxResults, boolean forward) throws StyxException;

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
     * @param session the session this data provider belongs to.
     * @throws StyxException if a new transaction could not be opened.
     */
    public void beginTransaction(Session session) throws StyxException;

    /**
     * Commits the current transaction.
     * <p>
     * After this method returns (either normally or with an exception),
     * it is guaranteed that there is no open transaction.
     * @param session the session this data provider belongs to.
     * @throws StyxException if the transaction failed to commit, or if there was no open transaction.
     */
    public void commitTransaction(Session session) throws StyxException;

    /**
     * Aborts the current transaction.
     * <p>
     * After this method returns (either normally or with an exception),
     * it is guaranteed that there is no open transaction.
     * <p>
     * The timeout, if any, is chosen by the implementation. It is not guaranteed that the data
     * actually has been changed upon return of this method and implementations are free to implement
     * polling or fixed, unconditional delays if the storage location cannot be monitored.
     * @param session the session this data provider belongs to.
     * @param retry normally false. If true, this method waits until the data read by the current
     *              transaction has been changed by another session or until a timeout has expired.
     * @throws StyxException if the transaction failed to abort, or if there was no open transaction.
     */
    public void abortTransaction(Session session, boolean retry) throws StyxException;
}
