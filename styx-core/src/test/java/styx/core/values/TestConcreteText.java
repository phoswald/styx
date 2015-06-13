package styx.core.values;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import styx.Session;
import styx.SessionManager;
import styx.Text;

public class TestConcreteText {

	private final Session session = SessionManager.getDetachedSession();

	@Test
	public void testNull() {
		assertNotNull(ConcreteText.factory(null));
		assertNotNull(ConcreteText.factory(""));
	}

	@Test
	public void testBascis() {
		Text val = ConcreteText.factory("x");
		assertNotNull(val);
		assertTrue(val.isText());
		assertNotNull(val.asText());
		assertEquals(val.toTextString(), "x");
	}

	@Test
	public void testCompare() {
		Text val = ConcreteText.factory("abcd");
		assertEquals( 1, val.compareTo(session.text("ab")));
		assertEquals( 1, val.compareTo(session.text("abcc")));
		assertEquals( 0, val.compareTo(val));
		assertEquals( 0, val.compareTo(session.text("abcd")));
		assertEquals(-1, val.compareTo(session.text("abcdaa")));
		assertEquals(-1, val.compareTo(session.text("abce")));
		assertEquals(-1, val.compareTo(session.empty()));
		assertEquals(-1, val.compareTo(session.root()));
	}

	@Test(expected=NullPointerException.class)
	public void testCompareInvalid1() {
		ConcreteText.factory("abcd").compareTo(null);
	}

	@Test
	public void testFromAndTo() {
		assertEquals("abcd", ConcreteText.factory("abcd").toTextString());
		assertArrayEquals(new char[] { 'a', 'b', 'c', 'd'}, ConcreteText.factory("abcd").toCharArray());
	}

	@Test
	public void testFromAndTo2() {
		assertEquals("", ConcreteText.factory(null).toTextString());
		assertArrayEquals(new char[] { }, ConcreteText.factory(null).toCharArray());
	}

//	@Test
//	public void testFromAndGetters() {
//		Text val = ConcreteText.factory("abcd");
//		assertEquals(4, val.charCount());
//		assertEquals('a', val.charAt(0));
//		assertEquals('b', val.charAt(1));
//		assertEquals('c', val.charAt(2));
//		assertEquals('d', val.charAt(3));
//	}
}
