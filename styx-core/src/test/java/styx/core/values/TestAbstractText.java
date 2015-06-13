package styx.core.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import styx.Session;
import styx.SessionManager;
import styx.Text;

public class TestAbstractText {

	private final Session session = SessionManager.getDetachedSession();

	@Test
	public void testNull() {
		assertNotNull(AbstractText.factory(null));
		assertNotNull(AbstractText.factory(""));
	}

	@Test
	public void testBascis() {
		Text val = AbstractText.factory("x");
		assertNotNull(val);
		assertTrue(val.isText());
		assertFalse(val.isNumber());
		assertFalse(val.isBinary());
		assertNotNull(val.asText());
		assertEquals(val.toTextString(), "x");
	}

	@Test
	public void textSubclasses() {
		assertTrue(AbstractText.factory("void").isVoid());
		assertTrue(AbstractText.factory("false").isBool());
		assertTrue(AbstractText.factory("true").isBool());
		assertTrue(AbstractText.factory("1234").isNumber());
		assertTrue(AbstractText.factory("0").isNumber());
		assertTrue(AbstractText.factory("-123").isNumber());
		assertTrue(AbstractText.factory("0x").isBinary());
		assertTrue(AbstractText.factory("0x3F").isBinary());

		assertFalse(AbstractText.factory("123X").isNumber());
		assertFalse(AbstractText.factory("0x3F3").isBinary());
		assertFalse(AbstractText.factory("0x3Fxx").isBinary());
		assertFalse(AbstractText.factory("0x3f").isBinary());
	}

	@Test
	public void testCompare() {
		Text val = AbstractText.factory("abcd");
		assertEquals( 1, val.compareTo(session.text("ab")));
		assertEquals( 1, val.compareTo(session.text("abcc")));
		assertEquals( 0, val.compareTo(val));
		assertEquals( 0, val.compareTo(session.text("abcd")));
		assertEquals(-1, val.compareTo(session.text("abcdaa")));
		assertEquals(-1, val.compareTo(session.text("abce")));
		assertEquals(-1, val.compareTo(session.root()));
		assertEquals(-1, val.compareTo(session.empty()));
	}

	@Test
	public void testCompare2() {
		assertEquals( 0, AbstractText.factory("void"). compareTo(session.empty()));
		assertEquals( 0, AbstractText.factory("false").compareTo(session.bool(false)));
		assertEquals( 0, AbstractText.factory("true"). compareTo(session.bool(true)));
		assertEquals(-1, AbstractText.factory("1233"). compareTo(session.number(1234)));
		assertEquals( 0, AbstractText.factory("1234"). compareTo(session.number(1234)));
		assertEquals( 1, AbstractText.factory("1235"). compareTo(session.number(1234)));
		assertEquals(-1, AbstractText.factory("0x3E"). compareTo(session.binary(new byte[] { 0x3F })));
		assertEquals( 1, AbstractText.factory("0x40"). compareTo(session.binary(new byte[] { 0x3F })));
	}
}
