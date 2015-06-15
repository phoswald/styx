package styx.core.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import styx.Complex;
import styx.Session;
import styx.SessionManager;
import styx.Value;

public class TestConcreteComplex {

	private final Session session = SessionManager.getDetachedSession();

	@Test
	public void testBascis() {
		Value v = ConcreteComplex.EMPTY.put(session.text("key"), session.text("val"));
		assertEquals("@key val", v.toString());
	}

	@Test
	public void testEquals() {
		Value v1 = ConcreteComplex.EMPTY.put(session.number(1), session.empty());
		Value v2 = ConcreteComplex.EMPTY.put(session.number(1), session.empty());
		assertTrue(v1.equals(v1));
		assertTrue(v1.equals(v2));
		assertFalse(v1.equals("foo"));
		// assertEquals(v1.toString().hashCode(), v1.hashCode());
	}

	@Test
	public void testCompare() {
		Complex val = ConcreteComplex.EMPTY.put(session.number(1), session.number(10));
		Complex val2 = val.put(session.number(2), session.number(20));
		assertEquals( 1, val.compareTo(session.empty()));
		assertEquals( 0, val.compareTo(val));
		assertEquals( 0, val.compareTo(session.complex(session.number(1), session.number(10))));
		assertEquals(-1, val.compareTo(session.complex(session.number(2), session.number(20))));

		assertEquals(-1, val.compareTo(val2));
		assertEquals(-1, val.compareTo(val.put(session.number(1), session.number(11))));
		assertEquals( 1, val2.compareTo(val));
		assertEquals( 1, val.compareTo(val.put(session.number(1), session.number(9))));

		try {
			ConcreteComplex.EMPTY.put(session.number(1), session.empty()).compareTo(null);
			fail();
		} catch(NullPointerException e) { }
	}

	@Test
	public void testGet() {
		Complex val = ConcreteComplex.EMPTY;
		assertTrue(val.isEmpty());
		assertFalse(val.hasSingle());
		assertFalse(val.hasMany());
		assertNull(val.single());

		val = ConcreteComplex.EMPTY.put(session.number(1), session.empty());
		assertFalse(val.isEmpty());
		assertTrue(val.hasSingle());
		assertFalse(val.hasMany());
		assertEquals(1, val.single().key().asNumber().toInteger());

		val = ConcreteComplex.EMPTY.put(session.number(1), session.empty()).put(session.number(2), session.root());
		assertTrue(val.get(session.number(1)).isVoid());
		assertTrue(val.get(session.number(2)).isReference());
		assertTrue(val.get(session.number(3)) == null);
		assertFalse(val.isEmpty());
		assertFalse(val.hasSingle());
		assertTrue(val.hasMany());
		assertNull(val.single());
//		assertEquals(val.first().key(), sess.number(1));
//		assertEquals(val.last().key(), sess.number(2));
//		assertEquals(val.next(null).key(), sess.number(1));
//		assertEquals(val.prev(null).key(), sess.number(2));
//		assertEquals(val.next(null).key(), sess.number(1));
//		assertEquals(val.prev(null).key(), sess.number(2));
//		assertEquals(val.next(sess.number(1)).key(), sess.number(2));
//		assertEquals(val.prev(sess.number(2)).key(), sess.number(1));
//		assertNull(val.next(sess.number(2)));
//		assertNull(val.prev(sess.number(1)));

		try {
			val.get(null);
			fail();
		} catch(NullPointerException e) { }
	}

	@Test
	public void testPut() {
		Complex val = ConcreteComplex.EMPTY.put(session.number(1), session.empty()).put(session.number(2), session.root());
		assertNotNull(val.put(session.number(3), session.empty()));
		assertNotNull(val.put(session.number(1), null));
		assertNotNull(val.put(session.number(1), null).put(session.number(2), null));

		try {
			val.put(null, session.empty());
			fail();
		} catch(NullPointerException e) { }
	}

	@Test
	public void testAdd() {
		Complex val = ConcreteComplex.EMPTY.add(session.text("x")).add(session.text("y")).add(session.text("z"));
		assertEquals("[x,y,z]", val.toString());
	}

	@Test
	public void testFactory() {
		assertNotNull(ConcreteComplex.EMPTY.put(session.empty(), null));
		assertTrue(ConcreteComplex.EMPTY.put(session.empty(), session.empty()) instanceof ConcreteComplex);

		try {
			ConcreteComplex.EMPTY.put(null, session.empty());
			fail();
		} catch(NullPointerException e) { }
	}
}
