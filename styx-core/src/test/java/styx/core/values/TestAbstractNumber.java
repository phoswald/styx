package styx.core.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import styx.Numeric;
import styx.Session;
import styx.SessionManager;

public class TestAbstractNumber {

	private final Session session = SessionManager.getDetachedSession();

	@Test
	public void testZero() {
		Numeric v = AbstractNumber.factory("0");
		assertEquals("0", v.toString());
		assertTrue(v.isText() && v.asText() != null);
		assertTrue(v.isNumber() && v.asNumber() != null);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(0, v.toInteger());
		assertEquals(0, v.toLong());
		assertEquals(0.0, v.toDouble(), 0.0);

		v = AbstractNumber.factory(0);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(0, v.toInteger());

		v = AbstractNumber.factory(0L);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(0, v.toInteger());

		v = AbstractNumber.factory(0.0);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(0, v.toInteger());
	}

	@Test
	public void testIntegerPos() {
		Numeric v = AbstractNumber.factory("1234");
		assertEquals("1234", v.toString());
		assertTrue(v.isText() && v.asText() != null);
		assertTrue(v.isNumber() && v.asNumber() != null);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(1234, v.toInteger());
		assertEquals(1234, v.toLong());
		assertEquals(1234.0, v.toDouble(), 0.0);

		v = AbstractNumber.factory(1234);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(1234, v.toInteger());

		v = AbstractNumber.factory(1234L);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(1234, v.toInteger());

		v = AbstractNumber.factory(1234.0);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(1234, v.toInteger());
	}

	@Test
	public void testIntegerNeg() {
		Numeric v = AbstractNumber.factory("-1234");
		assertEquals("-1234", v.toString());
		assertTrue(v.isText() && v.asText() != null);
		assertTrue(v.isNumber() && v.asNumber() != null);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(-1234, v.toInteger());
		assertEquals(-1234, v.toLong());
		assertEquals(-1234.0, v.toDouble(), 0.0);

		v = AbstractNumber.factory(-1234);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(-1234, v.toInteger());

		v = AbstractNumber.factory(-1234L);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(-1234, v.toInteger());

		v = AbstractNumber.factory(-1234.0);
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(-1234, v.toInteger());
	}

	@Test
	public void testLongPos() {
		Numeric v = AbstractNumber.factory("1234000000000000");
		assertEquals("1234000000000000", v.toString());
		assertTrue(v.isText() && v.asText() != null);
		assertTrue(v.isNumber() && v.asNumber() != null);
		assertSame(ConcreteNumberLong.class, v.getClass());
		assertEquals(1234000000000000L, v.toLong());
		assertEquals(1234000000000000.0, v.toDouble(), 0.0);

		v = AbstractNumber.factory(1234000000000000L);
		assertSame(ConcreteNumberLong.class, v.getClass());
		assertEquals(1234000000000000L, v.toLong());

		v = AbstractNumber.factory(1234000000000000.0);
		assertSame(ConcreteNumberLong.class, v.getClass());
		assertEquals(1234000000000000L, v.toLong());
	}

	@Test
	public void testLongNeg() {
		Numeric v = AbstractNumber.factory("-1234000000000000");
		assertEquals("-1234000000000000", v.toString());
		assertTrue(v.isText() && v.asText() != null);
		assertTrue(v.isNumber() && v.asNumber() != null);
		assertSame(ConcreteNumberLong.class, v.getClass());
		assertEquals(-1234000000000000L, v.toLong());
		assertEquals(-1234000000000000.0, v.toDouble(), 0.0);

		v = AbstractNumber.factory(-1234000000000000L);
		assertSame(ConcreteNumberLong.class, v.getClass());
		assertEquals(-1234000000000000L, v.toLong());

		v = AbstractNumber.factory(-1234000000000000.0);
		assertSame(ConcreteNumberLong.class, v.getClass());
		assertEquals(-1234000000000000L, v.toLong());
	}

	@Test
	public void testDoublePos() {
		Numeric v = AbstractNumber.factory("12.34");
		assertEquals("12.34", v.toString());
		assertTrue(v.isText() && v.asText() != null);
		assertTrue(v.isNumber() && v.asNumber() != null);
		assertSame(ConcreteNumberDouble.class, v.getClass());
		assertEquals(12.34, v.toDouble(), 0.0);

		v = AbstractNumber.factory(12.34);
		assertSame(ConcreteNumberDouble.class, v.getClass());
		assertEquals(12.34, v.toDouble(), 0.0);
	}

	@Test
	public void testDoublePosNeg() {
		Numeric v = AbstractNumber.factory("-12.34");
		assertEquals("-12.34", v.toString());
		assertTrue(v.isText() && v.asText() != null);
		assertTrue(v.isNumber() && v.asNumber() != null);
		assertSame(ConcreteNumberDouble.class, v.getClass());
		assertEquals(-12.34, v.toDouble(), 0.0);

		v = AbstractNumber.factory(-12.34);
		assertSame(ConcreteNumberDouble.class, v.getClass());
		assertEquals(-12.34, v.toDouble(), 0.0);
	}

	@Test
	public void testParseInteger() {
		testStringIntegerRoundTrip("0", 0);
		testStringIntegerDenormalized("0.0", 0);
		testStringIntegerDenormalized("-0", 0);
		testStringIntegerDenormalized("-0.0", 0);

		testStringIntegerRoundTrip("1000", 1000);
		testStringIntegerDenormalized("1000.0", 1000);
		testStringIntegerDenormalized("1000.00", 1000);
		testStringIntegerDenormalized("10E2", 1000);
		testStringIntegerDenormalized("10.0E2", 1000);
		testStringIntegerDenormalized("1E3", 1000);
		testStringIntegerDenormalized("1.0E3", 1000);
		testStringIntegerDenormalized("0.1E4", 1000);
		testStringIntegerDenormalized("0.01E5", 1000);

		testStringIntegerRoundTrip("1234", 1234);
		testStringIntegerDenormalized("01234", 1234);
		testStringIntegerDenormalized("1234.0", 1234);
		testStringIntegerDenormalized("1234E0", 1234);
		testStringIntegerDenormalized("001234E00", 1234);
		testStringIntegerDenormalized("1.234E3", 1234);
		testStringIntegerDenormalized("01.2340E03", 1234);
		testStringIntegerDenormalized("00.00012340E07", 1234);

		testStringIntegerRoundTrip("-1234", -1234);
		testStringIntegerDenormalized("-01234", -1234);
		testStringIntegerDenormalized("-1234.0", -1234);
		testStringIntegerDenormalized("-1234E0", -1234);
		testStringIntegerDenormalized("-1.234E3", -1234);
		testStringIntegerDenormalized("-01.2340E03", -1234);
		testStringIntegerDenormalized("-00.00012340E07", -1234);

		testStringIntegerRoundTrip( "2147483647",  2147483647); // Integer.MAX_VALUE
		testStringIntegerRoundTrip("-2147483648", -2147483648); // Integer.MIN_VALUE
	}

	@Test
	public void testParseLong() {
		testStringLongRoundTrip( "2147483648",  2147483648L); // Integer.MAX_VALUE + 1
		testStringLongRoundTrip("-2147483649", -2147483649L); // Integer.MIN_VALUE - 1

		testStringLongRoundTrip("2147483648000", 2147483648000L);
		testStringLongDenormalized("002147483648000", 2147483648000L);
		testStringLongDenormalized("2147483648000.00", 2147483648000L);
		testStringLongDenormalized("002147483648000.00", 2147483648000L);
		testStringLongDenormalized("21474836480.00E2", 2147483648000L);
		testStringLongDenormalized("002147483.648000E6", 2147483648000L);
		testStringLongDenormalized("2147483.648E6", 2147483648000L);
		testStringLongDenormalized("2.147483648E12", 2147483648000L);
		testStringLongDenormalized("0.2147483648E13", 2147483648000L);
		testStringLongDenormalized("0.002147483648E15", 2147483648000L);
		testStringLongDenormalized("0000.002147483648E15", 2147483648000L);

		testStringLongRoundTrip( "9223372036854775807",  9223372036854775807L); // Long.MAX_VALUE
		testStringLongRoundTrip("-9223372036854775808", -9223372036854775808L); // Long.MIN_VALUE
	}

	@Test
	public void testParseDouble() {
		testStringDoubleRoundTrip("0.001", 0.001);
		testStringDoubleRoundTrip("0.01", 0.01);
		testStringDoubleRoundTrip("0.1", 0.1);
		testStringDoubleRoundTrip("1.1", 1.1);
		testStringDoubleRoundTrip("10.1", 10.1);
		testStringDoubleRoundTrip("10.01", 10.01);

		testStringDoubleRoundTrip("1.234E-6", 0.000001234);
		testStringDoubleRoundTrip("1.234E-5", 0.00001234);
		testStringDoubleRoundTrip("0.0001234", 0.0001234);
		testStringDoubleRoundTrip("0.001234", 0.001234);
		testStringDoubleRoundTrip("0.01234", 0.01234);
		testStringDoubleRoundTrip("0.1234", 0.1234);
		testStringDoubleRoundTrip("1.234", 1.234);
		testStringDoubleRoundTrip("12.34", 12.34);
		testStringDoubleRoundTrip("12340000000000000000", 12340000000000000000.0);
		testStringDoubleRoundTrip("1.234E20", 1.234E20);
		testStringDoubleRoundTrip("1.234E21", 1.234E21);

		testStringDoubleRoundTrip("2.718281828459045", Math.E); // E is defined as 2.7182818284590452354, which more precise than double
		testStringDoubleRoundTrip("3.141592653589793", Math.PI); // PI is defined  3.14159265358979323846, which more precise than double
		testStringDoubleRoundTrip("4.9E-324", Double.MIN_VALUE);
		testStringDoubleRoundTrip("2.2250738585072014E-308", Double.MIN_NORMAL);
		testStringDoubleRoundTrip("1.7976931348623157E308", Double.MAX_VALUE);

		testStringDoubleDenormalized("00.1", 0.1);
		testStringDoubleDenormalized("01.1", 1.1);
		testStringDoubleDenormalized("1.10", 1.1);
	}

	@Test
	public void testParseUnbounded() {
		testStringUnboundedRoundTrip( "9223372036854775808", 18, 19); // Long.MAX_VALUE + 1
		testStringUnboundedRoundTrip("-9223372036854775809", 18, 19); // Long.MIN_VALUE - 1
		testStringUnboundedDenormalized("0009223372036854775808", "9223372036854775808");
		testStringUnboundedDenormalized("9223372036854775.80800E03", "9223372036854775808");

		testStringUnboundedRoundTrip("4.9E-325", -325, 2); // Double.MIN_VALUE / 10
		testStringUnboundedRoundTrip("4.91E-324", -324, 3); // Double.MIN_VALUE - something
		testStringUnboundedRoundTrip("5E-324", -324, 1); // Double.MIN_VALUE - something
		testStringUnboundedRoundTrip("1.7976931348623158E308", 308, 17); // Double.MAX_VALUE + something
		testStringUnboundedRoundTrip("1.79769313486231571E308", 308, 18); // Double.MAX_VALUE + something
		testStringUnboundedRoundTrip("1.7976931348623157E309", 309, 17); // Double.MAX_VALUE * 10


		testStringUnboundedRoundTrip("1.234567890123456789E-6", -6, 19);
		testStringUnboundedRoundTrip("1.234567890123456789E-5", -5, 19);
		testStringUnboundedRoundTrip("0.0001234567890123456789", -4, 19);
		testStringUnboundedRoundTrip("0.001234567890123456789", -3, 19);
		testStringUnboundedRoundTrip("0.01234567890123456789", -2, 19);
		testStringUnboundedRoundTrip("0.1234567890123456789", -1, 19);
		testStringUnboundedRoundTrip("1.234567890123456789", 0, 19);
		testStringUnboundedRoundTrip("12.34567890123456789", 1, 19);
		testStringUnboundedRoundTrip("12345678901234567890", 19, 19);
		testStringUnboundedRoundTrip("1.234567890123456789E20", 20, 19);
		testStringUnboundedRoundTrip("1.234567890123456789E21", 21, 19);

		testStringUnboundedDenormalized("0.000001234567890123456789", "1.234567890123456789E-6");
		testStringUnboundedDenormalized("0.00001234567890123456789", "1.234567890123456789E-5");
		testStringUnboundedDenormalized("1.234567890123456789E-4", "0.0001234567890123456789");
		testStringUnboundedDenormalized("1.234567890123456789E-3", "0.001234567890123456789");
		testStringUnboundedDenormalized("1.234567890123456789E-2", "0.01234567890123456789");
		testStringUnboundedDenormalized("1.234567890123456789E-1", "0.1234567890123456789");
		testStringUnboundedDenormalized("1.234567890123456789E0", "1.234567890123456789");
		testStringUnboundedDenormalized("1.234567890123456789E1", "12.34567890123456789");
		testStringUnboundedDenormalized("1.234567890123456789E2", "123.4567890123456789");
		testStringUnboundedDenormalized("1.234567890123456789E19", "12345678901234567890");
		testStringUnboundedDenormalized("123456789012345678900", "1.234567890123456789E20");
		testStringUnboundedDenormalized("1234567890123456789000", "1.234567890123456789E21");

		testStringUnboundedRoundTrip("1E500", 500, 1);
		testStringUnboundedRoundTrip("1E-500", -500, 1);
	}

	private void testStringIntegerRoundTrip(String str, int val) {
		Numeric v = AbstractNumber.factory(str);
		assertTrue(v.normalized());
		assertSame(ConcreteNumberInteger.class, v.getClass());
		assertEquals(val, v.toInteger());
		assertEquals(str, v.toDecimalString());
		assertEquals(str, v.toDecimal().toString());
		assertEquals(str, v.toString());
	}

	private void testStringIntegerDenormalized(String str, int val) {
		Numeric v = AbstractNumber.factory(str);
		assertFalse(v.normalized());
		assertSame(ConcreteNumberInteger.class, v.normalize().getClass());
		assertEquals(val, v.toInteger());
		assertFalse(str.equals(v.toDecimalString()));
		assertEquals(str, v.toString());
	}

	private void testStringLongRoundTrip(String str, long val) {
		Numeric v = AbstractNumber.factory(str);
		assertSame(ConcreteNumberLong.class, v.getClass());
		assertEquals(val, v.toLong());
		assertEquals(str, v.toDecimalString());
		assertEquals(str, v.toDecimal().toString());
		assertEquals(str, v.toString());
	}

	private void testStringLongDenormalized(String str, long val) {
		Numeric v = AbstractNumber.factory(str);
		assertFalse(v.normalized());
		assertSame(ConcreteNumberLong.class, v.normalize().getClass());
		assertEquals(val, v.toLong());
		assertFalse(str.equals(v.toDecimalString()));
		assertEquals(str, v.toString());
	}

	private void testStringDoubleRoundTrip(String str, double val) {
		Numeric v = AbstractNumber.factory(str);
		assertSame(ConcreteNumberDouble.class, v.getClass());
		assertEquals(val, v.toDouble(), 0.0);
		assertEquals(str, v.toDecimalString());
		assertEquals(str, v.toDecimal().toString());
		assertEquals(str, v.toString());
	}

	private void testStringDoubleDenormalized(String str, double val) {
		Numeric v = AbstractNumber.factory(str);
		assertFalse(v.normalized());
		assertSame(ConcreteNumberDouble.class, v.normalize().getClass());
		assertEquals(val, v.toDouble(), 0.0);
		assertFalse(str.equals(v.toDecimalString()));
		assertEquals(str, v.toString());
	}

	private void testStringUnboundedRoundTrip(String str, int exponent, int precision) {
		Numeric v = AbstractNumber.factory(str);
		assertSame(ConcreteNumberUnbounded.class, v.getClass());
		assertEquals(exponent, v.toDecimal().exponent());
		assertEquals(precision, v.toDecimal().precision());
		assertEquals(str, v.toDecimalString());
		assertEquals(str, v.toString());
	}

	private void testStringUnboundedDenormalized(String str, String val) {
		Numeric v = AbstractNumber.factory(str);
		assertFalse(v.normalized());
		assertSame(ConcreteNumberUnbounded.class, v.normalize().getClass());
		assertEquals(val, v.toDecimalString());
		assertFalse(str.equals(v.toDecimalString()));
		assertEquals(str, v.toString());
	}

	@Test
	public void testCompare() {
		Numeric val = AbstractNumber.factory(1234);
		assertEquals( 1, val.compareTo(session.number(1233)));
		assertEquals( 0, val.compareTo(val));
		assertEquals( 0, val.compareTo(session.number(1234)));
		assertEquals(-1, val.compareTo(session.number(1235)));
		assertEquals(-1, val.compareTo(session.empty()));
		assertEquals(-1, val.compareTo(session.root()));
		assertEquals(-1, val.compareTo(session.complex()));
	}

	@Test
	public void testCompareList() {
		List<Numeric> list = new ArrayList<>();

		list.add(AbstractNumber.factory("-1E500"));

		list.add(AbstractNumber.factory("-9223372036854775809")); // Long.MIN_VALUE - 1
		list.add(AbstractNumber.factory("-9223372036854775808")); // Long.MIN_VALUE

		list.add(AbstractNumber.factory("-2147483649")); // Integer.MIN_VALUE-1
		list.add(AbstractNumber.factory("-2147483648")); // Integer.MIN_VALUE
		list.add(AbstractNumber.factory("-2147483647")); // Integer.MIN_VALUE+1

		list.add(AbstractNumber.factory("-1234.5"));
		list.add(AbstractNumber.factory("-1234.1"));
		list.add(AbstractNumber.factory("-1234"));
		list.add(AbstractNumber.factory("-1234.0"));
		list.add(AbstractNumber.factory("-1233.5"));
		list.add(AbstractNumber.factory("-1233"));

		list.add(AbstractNumber.factory("-12.5"));

		list.add(AbstractNumber.factory("0"));
		list.add(AbstractNumber.factory("0.0"));
		list.add(AbstractNumber.factory("0.00"));

		list.add(AbstractNumber.factory("1E-500"));

		list.add(AbstractNumber.factory("4.9E-324")); // Double.MIN_VALUE)
		list.add(AbstractNumber.factory("2.2250738585072014E-308")); // Double.MIN_NORMAL

		list.add(AbstractNumber.factory(Math.E));
		list.add(AbstractNumber.factory(Math.PI));

		list.add(AbstractNumber.factory("12.5"));

		list.add(AbstractNumber.factory("1000"));
		list.add(AbstractNumber.factory("1000.00"));
		list.add(AbstractNumber.factory("1000.01"));
		list.add(AbstractNumber.factory("1233"));
		list.add(AbstractNumber.factory("1233.5"));
		list.add(AbstractNumber.factory("1234"));
		list.add(AbstractNumber.factory("1234.0"));
		list.add(AbstractNumber.factory("1234.1"));
		list.add(AbstractNumber.factory("1234.5"));

		list.add(AbstractNumber.factory("2147483646")); // Integer.MAX_VALUE-1
		list.add(AbstractNumber.factory("2147483647")); // Integer.MAX_VALUE
		list.add(AbstractNumber.factory("2147483648")); // Integer.MAX_VALUE+1
		list.add(AbstractNumber.factory("2147483648.5")); // Integer.MAX_VALUE+1.5

		list.add(AbstractNumber.factory("9223372036854775807")); // Long.MAX_VALUE
		list.add(AbstractNumber.factory("9223372036854775808")); // Long.MAX_VALUE+1

		list.add(AbstractNumber.factory("1.7976931348623157E308")); // Double.MAX_VALUE
		list.add(AbstractNumber.factory("1.7976931348623158E308")); // Double.MAX_VALUE + something
		list.add(AbstractNumber.factory("1.7976931348623157E309")); // Double.MAX_VALUE * 10

		list.add(AbstractNumber.factory("1E500"));

		for(int i = 0; i < list.size(); i++) {
			for(int j = 0; j < list.size(); j++) {
				int res1 = list.get(i).compareTo(list.get(j));
//				int res2 = DecimalNumber.compare(list.get(i).toDecimal(), list.get(j).toDecimal());
				assertTrue(list.get(i) + " <=> " + list.get(j), AbstractValue.compareInteger(i, j) == res1);
//				assertTrue("decimal " + list.get(i) + " <=> decimal " + list.get(j), AbstractValue.compareInteger(i, j) == res2);
			}
		}
	}
}
