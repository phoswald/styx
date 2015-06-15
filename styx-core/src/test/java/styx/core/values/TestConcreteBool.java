package styx.core.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import styx.Bool;
import styx.Session;
import styx.SessionManager;

public class TestConcreteBool {

	private final Session session = SessionManager.getDetachedSession();

	@Test
	public void testBascis() {
		assertTrue(ConcreteBool.FALSE.isBool());
		assertTrue(ConcreteBool.FALSE.isText());
		assertNotNull(ConcreteBool.FALSE.asBool());
		assertNotNull(ConcreteBool.FALSE.asText());
		assertEquals("false", ConcreteBool.FALSE.toString());
		assertEquals("true", ConcreteBool.TRUE.toString());
		assertFalse(ConcreteBool.FALSE.asBool().toBool());
		assertTrue(ConcreteBool.TRUE.asBool().toBool());
	}

	@Test
	public void testCompare() {
		Bool val = ConcreteBool.FALSE;
		assertEquals(-1, val.compareTo(session.bool(true)));
		assertEquals( 0, val.compareTo(val));
		assertEquals( 0, val.compareTo(session.bool(false)));
		assertEquals( 1, val.compareTo(session.number(0)));
	}

	@Test(expected=NullPointerException.class)
	public void testCompareInvalid1() {
		ConcreteBool.FALSE.compareTo(null);
	}
}
