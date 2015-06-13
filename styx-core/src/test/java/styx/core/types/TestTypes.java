package styx.core.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;
import styx.Type;
import styx.Value;

public class TestTypes {

	private static SessionFactory sf = SessionManager.createMemorySessionFactory(false);

	@Test
	public void test() throws StyxException {
		try(Session session = sf.createSession()) {
			Type type1 = makeType(session, ":: @Simple [ ]");
			Type type2 = makeType(session, ":: @Simple [ non_null: \"true\" ]");

			assertNotNull(type1);
			assertTrue(type1.validate(null));
			assertTrue(type1.validate(session.text("foo")));
			assertTrue(type1.assignable(type1));

			assertFalse(type2.validate(null));
			assertTrue (type2.validate(session.text("foo")));
			assertFalse(type2.assignable(type1));
			assertTrue (type1.assignable(type2));
		}
	}

	private Type makeType(Session session, String script) throws StyxException {
        Value parsed   = session.deserialize(script);
        String   serial   = session.serialize(parsed, true);
        Value reparsed = session.deserialize(serial);
        String   reserial = session.serialize(reparsed, true);
        assertTrue(parsed.isType());
        assertEquals(parsed.getClass(), reparsed.getClass());
        assertEquals(parsed, reparsed);
//      assertEquals(serial, script);
        assertEquals(serial, reserial);
		return parsed.asType();
	}
}
