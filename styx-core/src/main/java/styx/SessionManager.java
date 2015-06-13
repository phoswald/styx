package styx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * A static class that can be used to create, lookup, register and unregister session factories.
 *
 * Session factories are created by session providers ("drivers"). Providers are identified by well known names.
 * Providers are automatically discovered using Java's Service provider interface.
 * It is also possible to dynamically register additional providers at run time.
 *
 * Registered session factories are identified by names.
 * The session factory registered under the empty name is called the default session factory.
 * It is possible to dynamically register or unregister session factories at any time.
 */
public final class SessionManager {

    /**
     * Contains all known session providers ("drivers"), indexed by provider name.
     * Providers cannot be removed and their name cannot be null or empty.
     */
    private static final Map<String, SessionProvider> providers = new HashMap<>();

    /**
     * Contains all currently registered session factories, indexed by factory name.
     * Factories can be registered and unregistered dynamically and the name can be empty.
     */
    private static final Map<String, SessionFactory> factories = new HashMap<>();

    static {
        try {
            System.out.println("ServiceLoader: Loading implementations of SessionProvider...");
            Iterator<SessionProvider> iterator = ServiceLoader.load(SessionProvider.class).iterator();
            while(iterator.hasNext()) {
                SessionProvider provider = iterator.next();
                System.out.println("ServiceLoader: Registering '" + provider.getName() + "' -> " + provider.getClass().getName());
                registerProvider(provider);
            }
        } catch (ServiceConfigurationError | StyxException e) {
            System.out.println("ServiceLoader: Failed to load implementations of SessionProvider: " + e);
            throw new StyxRuntimeException("Failed to load implementations of SessionProvider.", e);
        }
    }

    public static List<SessionProvider> getProviders() {
        synchronized (providers) {
            return new ArrayList<>(providers.values());
        }
    }

    public static void registerProvider(SessionProvider provider) throws StyxException {
        Objects.requireNonNull(provider);
        String providerName = provider.getName();
        Objects.requireNonNull(providerName);
        synchronized (providers) {
            if(providers.containsKey(providerName)) {
                throw new StyxException("Provider '" + providerName + "' has already been registered.");
            }
            providers.put(providerName, provider);
        }
    }

    public static SessionFactory createSessionFactory(String providerName, Complex parameters) throws StyxException {
        Objects.requireNonNull(providerName);
        SessionProvider provider;
        synchronized (providers) {
            if(!providers.containsKey(providerName)) {
                throw new StyxException("Provider '" + providerName + "' is unknown.");
            }
            provider = providers.get(providerName);
        }
        return provider.createSessionFactory(parameters);
    }

    /**
     * Registers or unregisters a session factory under a name.
     * @param factoryName the name under which the session factory is to be registered or unregistered,
     *                    can be empty or null to register or unregister the default session factory.
     * @param factory non-null to register or null unregister a session factory.
     */
    public static void registerSessionFactory(String factoryName, SessionFactory factory) {
        if(factoryName == null) {
            factoryName = "";
        }
        synchronized (factories) {
            if(factory != null) {
                factories.put(factoryName, factory);
            } else {
                factories.remove(factoryName);
            }
        }
    }

    /**
     * Returns the session factory registered under the given name.
     * @param factoryName the name under which requested the session factory has been registered,
     *                    can be empty or null to request the default session factory.
     * @return the session factory currently registered under the given name, or null if none.
     */
    public static SessionFactory lookupSessionFactory(String factoryName) {
        if(factoryName == null) {
            factoryName = "";
        }
        synchronized (factories) {
            return factories.get(factoryName);
        }
    }

    public static Session getDetachedSession() {
        try {
            return createSessionFactory("detached", null).createSession();
        } catch (StyxException e) {
            throw new StyxRuntimeException("Failed to create detached session.", e);
        }
    }

    public static SessionFactory createMemorySessionFactory(boolean shared) {
        return createMemorySessionFactory(shared, null);
    }

    public static SessionFactory createMemorySessionFactory(boolean shared, Value value) {
        try {
            Session detached = SessionManager.getDetachedSession();
            return createSessionFactory("memory", detached.complex().
                    put(detached.text("shared"), detached.bool(shared)).
                    put(detached.text("value"), value));
        } catch (StyxException e) {
            throw new StyxRuntimeException("Failed to create memory session factory.", e);
        }
    }
}
