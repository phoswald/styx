package styx;

/**
 * Public interface of session providers.
 *
 * Session providers implement "drivers" for specific backends (database, file system, memory, etc).
 * They have a unique name and create session factories, which contain a set of parameters for that provider.
 *
 * Session providers are automatically discovered using Java's Service provider interface, which uses the
 * resource directory META-INF/services and can be accessed through the class java.util.ServiceLoader.
 *
 * Session providers can be obtained from the static class SessionManager.
 */
public interface SessionProvider {

    public String getName();

    public SessionFactory createSessionFactory(Complex parameters) throws StyxException;
}
