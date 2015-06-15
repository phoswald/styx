package styx.core.intrinsics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;

public class TestSessionIntrinsics extends Base {

    private static SessionFactory sf = SessionManager.createMemorySessionFactory(false);

    @Test
    public void testBrowse() throws StyxException {
        try(Session session = sf.createSession()) {
            session.evaluate("[/][*] = [ A: B, X: Y ]");
            assertEquals("[A,X]", evaluate(session, "session.browse([/])").toString());
        }
    }

    @Test
    public void testSerialize() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals("\"[A:B,C:D]\"", evaluate(session, " session.serialize([C:D,A:B], false) ").toString());
            assertEquals("[A:B,C:D]", evaluate(session, " session.deserialize(\"[C:D,A:B]\") ").toString());
        }
    }

    @Test
    public void testEvaluate() throws StyxException {
        try(Session session = sf.createSession()) {
            session.evaluate("[/][*] = [ 10, 20, 30, 40 ]");
            assertEquals(session.parse("1 + 2").toString(), evaluate(session, " session.parse(\"1 + 2\") ").toString());
            assertEquals("3", evaluate(session, " session.evaluate(\"1 + 2\") ").toString());
            assertEquals("100", evaluate(session, " session.evaluate(\"[/1][*] + [/2][*] + [/][3][*] + [/][4][*]\") ").toString());
        }
    }
}
