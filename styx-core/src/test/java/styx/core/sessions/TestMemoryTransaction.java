package styx.core.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import styx.ConcurrentException;
import styx.Reference;
import styx.Session;
import styx.SessionManager;
import styx.StyxException;

@RunWith(Parameterized.class)
public class TestMemoryTransaction extends TestAnyTransaction {

    public TestMemoryTransaction(AbstractSessionFactory sf) {
        super(sf);
    }

    @Parameters
    public static Collection<?> getParameters() {
        return Arrays.<Object[]>asList(
                new Object[] { // test parameter [0]
                        SessionManager.createMemorySessionFactory(true)
                },
                new Object[] { // test parameter [1]
                        FileSessionProvider.createSessionFactory(Paths.get("target", "styx-session", "TestMemorySession.styx"), true)
                });
    }

    @Test
    public void testRaceRW() throws IOException, StyxException {
        try(Session session = sf.createSession(); Session session2 = sf.createSession()) {
            session.write(session.root(), session.complex());

            Reference ref = session.root().child(session.text("foo"));
            session.write(ref, session.text("bar"));
            assertEquals("bar", session.read(ref).asText().toTextString());

            Reference ref2 = session2.root().child(session2.text("foo"));
            assertEquals("bar", session2.read(ref2).asText().toTextString());

            session.beginTransaction();
            session2.beginTransaction();
            session.write(ref, session.text("baz"));
            assertEquals("bar", session2.read(ref2).asText().toTextString());
            session.commitTransaction();

            assertEquals("bar", session2.read(ref2).asText().toTextString());
            session2.commitTransaction();
            assertEquals("baz", session2.read(ref2).asText().toTextString());
        }
    }

    @Test
    public void testRaceWW() throws IOException, StyxException {
        try(Session session = sf.createSession(); Session session2 = sf.createSession()) {
            session.write(session.root(), session.complex());

            Reference ref = session.root().child(session.text("foo"));
            session.write(ref, session.text("bar"));
            assertEquals("bar", session.read(ref).asText().toTextString());

            Reference ref2 = session2.root().child(session2.text("foo"));
            assertEquals("bar", session2.read(ref2).asText().toTextString());

            session.beginTransaction();
            session2.beginTransaction();
            session.write(ref, session.text("baz"));
            session2.write(ref2, session.text("baz2"));
            session.commitTransaction();

            try {
                session2.commitTransaction();
                fail();
            } catch(StyxException e) {
                assertTrue(e instanceof ConcurrentException);
            }

            assertFalse(session2.hasTransaction());
            assertEquals("baz", session2.read(ref2).asText().toTextString());
        }
    }
}
