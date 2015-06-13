package styx.core.utils;

//import static org.junit.Assert.assertEquals;

//import org.junit.Test;

/*
public class TestLongDouble {

	@Test
	public void testLongDouble() {
		LongDouble val = new LongDouble(0);
		assertEquals(0, val.sign);
		assertEquals(0, val.exponent);
		assertEquals(0, val.mantissa);

		val = new LongDouble(0.0);
		assertEquals(0, val.sign);
		assertEquals(0, val.exponent);
		assertEquals(0, val.mantissa);

		val = new LongDouble(-0.0);
		assertEquals(0, val.sign);
		assertEquals(0, val.exponent);
		assertEquals(0, val.mantissa);

		val = new LongDouble(0);
		assertEquals(0, val.sign);
		assertEquals(0, val.exponent);
		assertEquals(0, val.mantissa);
		
		
		val = new LongDouble(1.0);
		assertEquals(val.toString(), 1,            val.sign);
		assertEquals(val.toString(), 0,            val.exponent);
		assertEquals(val.toString(), 1L << (63-0), val.mantissa);		

		val = new LongDouble(-1.0);
		assertEquals(val.toString(), -1,           val.sign);
		assertEquals(val.toString(), 0,            val.exponent);
		assertEquals(val.toString(), 1L << (63-0), val.mantissa);		

		val = new LongDouble(1);
		assertEquals(val.toString(), 1,            val.sign);
		assertEquals(val.toString(), 0,            val.exponent);
		assertEquals(val.toString(), 1L << (63-0), val.mantissa);		

		val = new LongDouble(-1);
		assertEquals(val.toString(), -1,           val.sign);
		assertEquals(val.toString(), 0,            val.exponent);
		assertEquals(val.toString(), 1L << (63-0), val.mantissa);		

		
		val = new LongDouble(1.5);
		assertEquals(val.toString(), 1,            val.sign);
		assertEquals(val.toString(), 0,            val.exponent);
		assertEquals(val.toString(), 3L << (63-1), val.mantissa);		

		
		val = new LongDouble(2.0);
		assertEquals(val.toString(), 1,            val.sign);
		assertEquals(val.toString(), 1,            val.exponent);
		assertEquals(val.toString(), 2L << (63-1), val.mantissa);		

		
		val = new LongDouble(3.0);
		assertEquals(val.toString(), 1,            val.sign);
		assertEquals(val.toString(), 1,            val.exponent);
		assertEquals(val.toString(), 3L << (63-1), val.mantissa);		

		val = new LongDouble(3);
		assertEquals(val.toString(), 1,            val.sign);
		assertEquals(val.toString(), 1,            val.exponent);
		assertEquals(val.toString(), 3L << (63-1), val.mantissa);		

		
		val = new LongDouble(1023.0);
		assertEquals(val.toString(), 1,               val.sign);
		assertEquals(val.toString(), 9,               val.exponent);
		assertEquals(val.toString(), 1023L << (63-9), val.mantissa);		
		
		val = new LongDouble(1023);
		assertEquals(val.toString(), 1,               val.sign);
		assertEquals(val.toString(), 9,               val.exponent);
		assertEquals(val.toString(), 1023L << (63-9), val.mantissa);		

		val = new LongDouble(-1023.0);
		assertEquals(val.toString(), -1,              val.sign);
		assertEquals(val.toString(), 9,               val.exponent);
		assertEquals(val.toString(), 1023L << (63-9), val.mantissa);		
		
		val = new LongDouble(-1023);
		assertEquals(val.toString(), -1,              val.sign);
		assertEquals(val.toString(), 9,               val.exponent);
		assertEquals(val.toString(), 1023L << (63-9), val.mantissa);		

		
		val = new LongDouble(Long.MIN_VALUE); // -2^63
		assertEquals(val.toString(), -1,              val.sign);
		assertEquals(val.toString(), 63,              val.exponent);
		assertEquals(val.toString(), 1L << (63),      val.mantissa);		

		val = new LongDouble(Long.MAX_VALUE); // 2^63 - 1 == 1.111 * 2^62 
		assertEquals(val.toString(), 1,               val.sign);
		assertEquals(val.toString(), 62,              val.exponent);
		assertEquals(val.toString(), -1 << (63-62),   val.mantissa);		
	}
	
}
*/