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

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public SessionFactory createSessionFactory(Complex parameters) {
        Session detached = SessionManager.getDetachedSession();
        return createSessionFactory(
                FileSystems.getDefault().getPath(parameters.get(detached.text("path")).asText().toTextString()),
                parameters.get(detached.text("indent")).asBool().toBool());
    }

    public static AbstractSessionFactory createSessionFactory(Path path, boolean indent) {
        final SharedValue state = new SharedValueFile(path, indent);
        return new AbstractSessionFactory() {
            @Override
            public Session createSession() throws StyxException {
                return new ConcreteSession(new SharedMemoryData(state.clone()), type, func, eval, environment);
            }
        };
    }
}
