package styx.core.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import styx.Session;
import styx.SessionManager;
import styx.Void;

public class TestConcreteVoid {

	private final Session session = SessionManager.getDetachedSession();

	@Test
	public void testBascis() {
		assertTrue(ConcreteVoid.VOID.isVoid());
		assertTrue(ConcreteVoid.VOID.isText());
		assertNotNull(ConcreteVoid.VOID.asVoid());
		assertNotNull(ConcreteVoid.VOID.asText());
		assertEquals("void", ConcreteVoid.VOID.toString());
	}

	@Test
	public void testCompare() {
		Void val = ConcreteVoid.VOID;
		assertEquals( 0, val.compareTo(val));
		assertEquals( 0, val.compareTo(session.empty()));
		assertEquals( 1, val.compareTo(session.number(0)));
	}

	@Test(expected=NullPointerException.class)
	public void testCompareInvalid1() {
		ConcreteVoid.VOID.compareTo(null);
	}
}
