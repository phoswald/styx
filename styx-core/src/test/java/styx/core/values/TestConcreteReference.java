package styx.core.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import styx.Reference;
import styx.Session;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;

public class TestConcreteReference {

	private final Session session = SessionManager.getDetachedSession();

	@Test
	public void testBascis() {
		Value v = ConcreteReference.ROOT;
		assertEquals("[/]", v.toString());
		assertSame(v, v.asReference());
	}

	@Test
	public void testParentAndLevel() {
		Reference val0 = ConcreteReference.ROOT;
		Reference val1 = val0.child(session.empty());
		Reference val2 = val1.child(session.empty());
		assertNull(val0.parent());
		assertSame(val0, val1.parent());
		assertSame(val1, val2.parent());
		assertEquals(0, val0.level());
		assertEquals(1, val1.level());
		assertEquals(2, val2.level());
		assertSame(val0, val0.parent(0));
		assertSame(val0, val1.parent(0));
		assertSame(val0, val2.parent(0));
		assertSame(val1, val1.parent(1));
		assertSame(val1, val2.parent(1));
		assertSame(val2, val2.parent(2));
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testParentInvalid1() {
		ConcreteReference.ROOT.child(session.empty()).child(session.empty()).parent(-1);
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testParentInvalid2() {
		ConcreteReference.ROOT.child(session.empty()).child(session.empty()).parent(3);
	}

	@Test
	public void testCompare() {
		Reference val = ConcreteReference.ROOT;
		Reference val2 = val.child(session.number(1));

		assertEquals( 1, val.compareTo(session.empty()));
		assertEquals( 0, val.compareTo(val));
		assertEquals( 0, val.compareTo(session.root()));
		assertEquals(-1, val.compareTo(session.root().child(session.empty())));
		assertEquals(-1, val.compareTo(session.complex()));

		assertEquals( 1, val2.compareTo(val.child(session.number(0))));
		assertEquals( 0, val2.compareTo(val.child(session.number(1))));
		assertEquals( 0, val2.compareTo(val2));
		assertEquals(-1, val2.compareTo(val.child(session.number(2))));

		assertEquals(-1, val2.compareTo(val2.child(session.number(10))));
		assertEquals( 1, val2.child(session.number(10)).compareTo(val2));

		try {
			ConcreteReference.ROOT.compareTo(null);
			fail();
		} catch(NullPointerException e) { }
	}

	@Test(expected=NullPointerException.class)
	public void testChildIllegal() {
		ConcreteReference.ROOT.child(null);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testRead() throws StyxException {
		session.read(ConcreteReference.ROOT);
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testWrite() throws StyxException {
		session.write(ConcreteReference.ROOT, session.empty());
	}
}
