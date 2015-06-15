package styx.core.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import styx.Session;
import styx.SessionManager;
import styx.StyxException;

@RunWith(Parameterized.class)
public class TestMemorySession extends TestAnySession {

    public TestMemorySession(AbstractSessionFactory sf) {
        super(sf);
    }

    @Test
    public void testCtor() throws StyxException {
        try(Session session = sf.createSession()) {
            try(Session session2 = SessionManager.createMemorySessionFactory(false).createSession()) {
                assertNull(session2.read(session.root()));
            }
            try(Session session2 = SessionManager.createMemorySessionFactory(false, session.deserialize("1234")).createSession()) {
                assertEquals("1234", session2.read(session.root()).toString());
            }
            try(Session session2 = SessionManager.createMemorySessionFactory(false, session.deserialize("[a:b,c:d]")).createSession()) {
                assertEquals("[a:b,c:d]", session2.read(session.root()).toString());
            }
        }
    }

    @Parameters
    public static Collection<?> getParameters() {
        return Arrays.<Object[]>asList(
                new Object[] { // test parameter [0]
                        SessionManager.createMemorySessionFactory(false)
                },
                new Object[] { // test parameter [1]
                        SessionManager.createMemorySessionFactory(true)
                },
                new Object[] { // test parameter [2]
                        FileSessionProvider.createSessionFactory(Paths.get("target", "styx-session", "TestMemorySession.styx"), true)
                });
    }
}
