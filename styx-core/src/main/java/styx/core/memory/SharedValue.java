package styx.core.memory;

import styx.StyxException;
import styx.Session;
import styx.Value;

/**
 * The interface used by SharedMemoryData for atomically reading, writing, updating and watching a shared value.
 * <p>
 * Values may be stored in-memory or in an external location.
 * <p>
 * Each instance of this interface is used by only one session, but multiple instances may refer to the same value.
 */
public interface SharedValue extends Cloneable {

    /**
     * Creates a new instance that refers to the same shared location.
     * @return never null.
     */
    public SharedValue clone();

    /**
     * Reads the value from the shared location.
     * @param session the session this instance belongs to.
     * @return the current value, can be null.
     * @throws StyxException if a storage access occurs
     */
    public Value get(Session session) throws StyxException;

    /**
     * Writes the value to the shared location.
     * @param session the session this instance belongs to.
     * @param value the value to be written, can be null.
     * @throws StyxException if a storage access occurs
     */
    public void set(Session session, Value value) throws StyxException;

    /**
     * Writes the value to the shared location if the value has not been changed by another session.
     * <p>
     * The basis for the detection of changes is the last read or write on the same instance.
     * @param session the session this instance belongs to.
     * @param value the value to be written, can be null.
     * @return true if the value has been written, false if the value has been changed by another session.
     * @throws StyxException if a storage access occurs
     */
    public boolean testset(Session session, Value value) throws StyxException;

    /**
     * Waits until the value has been changed by another session or until a timeout has expired.
     * <p>
     * The basis for the detection of changes is the last read or write on the same instance.
     * <p>
     * The timeout, if any, is chosen by the implementation. It is not guaranteed that the value
     * actually has been changed upon return of this method and implementations are free to implement
     * polling or fixed, unconditional delays if the storage location cannot be monitored.
     * @param session the session this instance belongs to.
     */
    public void monitor(Session session);
}
