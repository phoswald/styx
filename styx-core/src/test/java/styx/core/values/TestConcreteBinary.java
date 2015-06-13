package styx.core.values;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import styx.Binary;
import styx.Session;
import styx.SessionManager;
import styx.Value;

public class TestConcreteBinary {

	private final byte[] deadbeef = new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
	private final Session session = SessionManager.getDetachedSession();

	@Test
	public void testBascis() {
		Value v = ConcreteBinary.factory("DEADBEEF");
		assertEquals("0xDEADBEEF", v.toString());
		assertSame(v, v.asBinary());
		assertTrue(v.isBinary() && v.asBinary() != null);
	}

	@Test
	public void testCompare() {
		Binary val = ConcreteBinary.factory("1234");
		assertEquals( 1, val.compareTo(session.binary("12")));
		assertEquals( 1, val.compareTo(session.binary("1233")));
		assertEquals( 0, val.compareTo(val));
		assertEquals( 0, val.compareTo(session.binary("1234")));
		assertEquals(-1, val.compareTo(session.binary("123400")));
		assertEquals(-1, val.compareTo(session.binary("1235")));
		assertEquals(-1, val.compareTo(session.root()));
		assertEquals(-1, val.compareTo(session.empty()));
	}

	@Test
	public void testCompare2() {
		String[] strs = new String[] {
				"",
				"00", "0000",
				"01", "0100",
				"7F", "7FFF",
				"80", "8001",
				"FF", "FF00", "FF02" };
		for(int i = 0; i < strs.length; i++) {
			Binary v1 = ConcreteBinary.factory(strs[i]);
			assertEquals(v1.toHexString(), 0, v1.compareTo(v1));
			assertEquals(v1.toHexString(), 0, v1.compareTo(ConcreteBinary.factory(strs[i])));
			for(int j = i + 1; j < strs.length; j++) {
				Binary v2 = ConcreteBinary.factory(strs[j]);
				assertEquals(v1.toHexString() + " vs " + v2.toHexString(), -1, v1.compareTo(v2));
				assertEquals(v2.toHexString() + " vs " + v1.toHexString(),  1, v2.compareTo(v1));
			}
		}
	}

	@Test(expected=NullPointerException.class)
	public void testCompareInvalid1() {
		ConcreteBinary.factory("1234").compareTo(null);
	}

	@Test
	public void testFromAndTo() {
		assertEquals("DEADBEEF", ConcreteBinary.factory("DEADBEEF").toHexString());
		assertEquals("DEADBEEF", ConcreteBinary.factory(deadbeef).toHexString());
		assertArrayEquals(deadbeef, ConcreteBinary.factory("DEADBEEF").toByteArray());
		assertArrayEquals(deadbeef, ConcreteBinary.factory(deadbeef).toByteArray());
	}

	@Test
	public void testFromAndTo2() {
		assertEquals("", ConcreteBinary.factory((byte[]) null).toHexString());
		assertEquals("", ConcreteBinary.factory((String) null).toHexString());
		assertArrayEquals(new byte[] { }, ConcreteBinary.factory((byte[]) null).toByteArray());
		assertArrayEquals(new byte[] { }, ConcreteBinary.factory((String) null).toByteArray());

		assertEquals("007F80FF", ConcreteBinary.factory(new byte[] { 0, 127, (byte) 128, (byte) 255 }).toHexString());
	}

	@Test
	public void testFromAndGetters() {
		Binary val = ConcreteBinary.factory("0123456789ABCDEF");
		assertEquals(8, val.byteCount());
		assertEquals((byte) 0x01, val.byteAt(0));
		assertEquals((byte) 0x23, val.byteAt(1));
		assertEquals((byte) 0x45, val.byteAt(2));
		assertEquals((byte) 0x67, val.byteAt(3));
		assertEquals((byte) 0x89, val.byteAt(4));
		assertEquals((byte) 0xab, val.byteAt(5));
		assertEquals((byte) 0xcd, val.byteAt(6));
		assertEquals((byte) 0xef, val.byteAt(7));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromInvalid1() {
		ConcreteBinary.factory("000");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromInvalid2() {
		ConcreteBinary.factory("00..");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromInvalid3() {
		ConcreteBinary.factory("00xx");
	}
}
