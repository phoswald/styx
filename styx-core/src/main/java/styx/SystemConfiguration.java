package styx;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class SystemConfiguration {

    private static final Logger LOG = Logger.getLogger(SystemConfiguration.class.toString());

    private static final Session detached = SessionManager.getDetachedSession();

    public static void load(String configFile) throws StyxException {
        SessionManager.registerSessionFactory(null, SessionManager.createMemorySessionFactory(true));
        Path path = FileSystems.getDefault().getPath(configFile);
        if(Files.exists(path)) {
            Complex config  = detached.deserialize(path).asComplex();
            Value   classes = config.get(detached.text("classes"));
            if(classes != null) {
                for(Pair<Value, Value> entry : classes.asComplex()) {
                    String clazz = entry.val().asText().toTextString();
                    try {
                        LOG.info("Loading class " + clazz + ".");
                        Class.forName(clazz);
                    } catch (ClassNotFoundException e) {
                        LOG.severe("Failed to load class " + clazz + ": " + e);
                    }
                }
            }
            Value factories = config.get(detached.text("factories"));
            if(factories != null) {
                for(Pair<Value, Value> entry : factories.asComplex()) {
                    String     factoryName    = entry.key().asText().toTextString();
                    String     providerName   = entry.val().asComplex().get(detached.text("provider")).asText().toTextString();
                    Complex providerParams = entry.val().asComplex().get(detached.text("parameters")).asComplex();

                    LOG.info("Registering session factory '" + (factoryName.length() == 0 ? "<default>" : factoryName) + "' with provider '" + providerName + "' and parameters " + detached.serialize(providerParams, false) + ".");
                    SessionFactory factory = SessionManager.createSessionFactory(providerName, providerParams);
                    SessionManager.registerSessionFactory(factoryName, factory);
                }
            }
        }
    }
}
