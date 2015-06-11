package styx;

/**
 * Public interface of session factories.
 *
 * Session factories create session instances. They are themselves created by session
 * providers. A session factory contains a session provider (a "driver") and a set of
 * parameters for that provider.
 */
public interface SessionFactory {

    public Session createSession() throws StyxException;
}
