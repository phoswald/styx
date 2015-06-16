package styx.db.mmap;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import styx.Complex;
import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.SessionProvider;
import styx.StyxException;
import styx.core.memory.SharedMemoryData;
import styx.core.sessions.AbstractSessionFactory;
import styx.core.sessions.ConcreteSession;

public class MmapSessionProvider implements SessionProvider {

    @Override
    public String getName() {
        return "mmap";
    }

    @Override
    public SessionFactory createSessionFactory(Complex parameters) throws StyxException {
        Session detached = SessionManager.getDetachedSession();
        return createSessionFactory(
                FileSystems.getDefault().getPath(parameters.get(detached.text("path")).asText().toTextString()));
    }

    public static AbstractSessionFactory createSessionFactory(Path path) throws StyxException {
        MmapDatabase db = MmapDatabase.fromFile(path);
        final MmapSharedValue state = new MmapSharedValue(db);
        return new AbstractSessionFactory() {
            @Override
            public Session createSession() throws StyxException {
                return new ConcreteSession(db.getEmpty(), new SharedMemoryData(state.clone()), type, func, eval, environment);
            }
        };
    }
}
