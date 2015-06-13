package styx.core.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;

import styx.Session;
import styx.StyxException;
import styx.Value;

public abstract class TestBase {

    @BeforeClass
    public static void createFolder() throws IOException {
        Files.createDirectories(Paths.get("target", "styx-session"));
    }

    protected Value evaluate(Session session, String script) throws StyxException {
        try {
            Value parsed   = session.parse(script, true);
            String   serial   = session.serialize(parsed, true);
            Value reparsed = session.deserialize(serial);
            String   reserial = session.serialize(reparsed, true);
            assertEquals(parsed.getClass(), reparsed.getClass());
            assertEquals(serial, reserial);
        } catch (StyxException e) {
            fail(e.toString());
        }
        return session.evaluate(script);
    }

    protected Value evaluate(Session session, String script, int idx) throws StyxException {
        return evaluate(session, script).asComplex().get(session.number(idx));
    }

    protected static void deleteRecursive(Path path) throws IOException {
        if(Files.isDirectory(path)) {
            try(DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for(Path entry : entries) {
                    deleteRecursive(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    protected static final class SyncState {
        private String state;

        public SyncState(String state) {
            this.state = state;
        }

        public synchronized void set(String state) {
            System.out.println("SyncState: setting " + state);
            this.state = state;
            notifyAll();
        }

        public synchronized void wait(String state) throws InterruptedException {
            while(!state.equals(this.state)) {
                System.out.println("SyncState: waiting for " + state + "...");
                wait(5000);
            }
            System.out.println("SyncState: reached " + state);
        }
    }
}
