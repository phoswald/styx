package styx.db.mmap;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;

import styx.Complex;
import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.SessionProvider;
import styx.StyxException;
import styx.StyxRuntimeException;
import styx.core.DataProvider;
import styx.core.EvalProvider;
import styx.core.FuncProvider;
import styx.core.TypeProvider;
import styx.core.memory.SharedMemoryData;
import styx.core.sessions.AbstractSessionFactory;
import styx.core.sessions.ConcreteSession;

public class MappedFileSessionProvider implements SessionProvider {

    private static final Session detached = SessionManager.getDetachedSession();

    @Override
    public String getName() {
        return "mmap";
    }

    @Override
    public SessionFactory createSessionFactory(Complex parameters) {
        return createSessionFactory(
                parameters.get(detached.text("filename")).asText().toTextString());
    }

    public static AbstractSessionFactory createSessionFactory(String file) {
        return createSessionFactory(FileSystems.getDefault().getPath(file));
    }

    public static AbstractSessionFactory createSessionFactory(Path file) {
        MappedDatabase db;
        try {
            db = MappedDatabase.fromFile(file);
        } catch (IOException e) {
            throw new StyxRuntimeException("Failed to open or map file.", e); // TODO where to open file?
        }
        final MappedSharedValue state = new MappedSharedValue(db);
        return new AbstractSessionFactory() {
            @Override
            public Session createSession() throws StyxException {
                return new ConcreteSessionEx(new SharedMemoryData(state.clone()), type, func, eval, environment, state.empty());
            }
        };
    }

    private static final class ConcreteSessionEx extends ConcreteSession {

        private final Complex empty;

        public ConcreteSessionEx(DataProvider data, TypeProvider type, FuncProvider func, EvalProvider eval, Complex environment, Complex empty) {
            super(data, type, func, eval, environment);
            this.empty = Objects.requireNonNull(empty);
        }

        @Override
        public Complex complex() {
            return empty;
        }
    }
}
