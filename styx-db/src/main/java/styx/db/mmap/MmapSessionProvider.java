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

public class MmapSessionProvider implements SessionProvider {

    @Override
    public String getName() {
        return "mmap";
    }

    @Override
    public SessionFactory createSessionFactory(Complex parameters) {
        Session detached = SessionManager.getDetachedSession();
        return createSessionFactory(
                parameters.get(detached.text("path")).asText().toTextString());
    }

    public static AbstractSessionFactory createSessionFactory(String path) {
        return createSessionFactory(FileSystems.getDefault().getPath(path));
    }

    public static AbstractSessionFactory createSessionFactory(Path path) {
        MmapDatabase db;
        try {
            db = MmapDatabase.fromFile(path);
        } catch (IOException e) {
            throw new StyxRuntimeException("Failed to open or map file.", e); // TODO where to open file?
        }
        final MmapSharedValue state = new MmapSharedValue(db);
        return new AbstractSessionFactory() {
            @Override
            public Session createSession() throws StyxException {
                return new ConcreteSessionEx(new SharedMemoryData(state.clone()), type, func, eval, environment, db.makeComplex(0));
            }
        };
    }

    private static final class ConcreteSessionEx extends ConcreteSession { // TODO: add complex to base class, then remove this class

        private final Complex complex;

        public ConcreteSessionEx(DataProvider data, TypeProvider type, FuncProvider func, EvalProvider eval, Complex environment, Complex complex) {
            super(data, type, func, eval, environment);
            this.complex = Objects.requireNonNull(complex);
        }

        @Override
        public Complex complex() {
            return complex;
        }
    }
}
