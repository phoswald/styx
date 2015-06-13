package styx.core.sessions;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import styx.Complex;
import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.SessionProvider;
import styx.StyxException;
import styx.core.memory.SharedMemoryData;
import styx.core.memory.SharedValue;
import styx.core.memory.SharedValueFile;

public final class FileSessionProvider implements SessionProvider {

    private static final Session detached = SessionManager.getDetachedSession();

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public SessionFactory createSessionFactory(Complex parameters) {
        return createSessionFactory(
                parameters.get(detached.text("filename")).asText().toTextString(),
                parameters.get(detached.text("indent")).asBool().toBool());
    }

    public static AbstractSessionFactory createSessionFactory(String file, boolean indent) {
        return createSessionFactory(FileSystems.getDefault().getPath(file), indent);
    }

    public static AbstractSessionFactory createSessionFactory(Path file, boolean indent) {
        final SharedValue state = new SharedValueFile(file, indent);
        return new AbstractSessionFactory() {
            @Override
            public Session createSession() throws StyxException {
                return new ConcreteSession(new SharedMemoryData(state.clone()), type, func, eval, environment);
            }
        };
    }
}
