package styx.core.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

import styx.Text;
import styx.Value;
import styx.core.utils.LimitingWriter;
import styx.core.values.AbstractNumber;
import styx.core.values.ConcreteText;
import styx.core.values.ConcreteVoid;

public class TestAbstractValue {

	@Test
	public void testEquals() {
		Value v1 = ConcreteText.factory("abcd");
		Value v2 = ConcreteText.factory("abcd");
		assertTrue(v1.equals(v1));
		assertTrue(v1.equals(v2));
		assertFalse(v1.equals("foo"));
		assertFalse(v1.equals(null));
		// assertEquals(v1.toString().hashCode(), v1.hashCode());
	}

	@Test
	public void testToStringLimit() {
		StringBuilder str2k = new StringBuilder();
		for(int i = 0; i < 2000; i++) str2k.append('x');
		Text v = ConcreteText.factory(str2k.toString());
		assertEquals(2000, v.toTextString().length());
//		assertEquals(2000, v.charCount());
		assertEquals(1000, v.toString().length());
	}

	@Test
	public void testBasics() {
		try {
			ConcreteVoid.VOID.asNumber();
			fail();
		} catch(ClassCastException e) { }
		assertEquals("void", ConcreteVoid.VOID.asText().toTextString());
		try {
			ConcreteVoid.VOID.asBinary();
			fail();
		} catch(ClassCastException e) { }
		try {
			ConcreteVoid.VOID.asReference();
			fail();
		} catch(ClassCastException e) { }
		try {
			ConcreteVoid.VOID.asComplex();
			fail();
		} catch(ClassCastException e) { }
		try {
			AbstractNumber.factory(1234).asVoid();
			fail();
		} catch(ClassCastException e) { }
		assertEquals("1234", AbstractNumber.factory(1234).asText().toTextString());
		try {
			AbstractNumber.factory(1234).asBinary();
			fail();
		} catch(ClassCastException e) { }
		try {
			AbstractNumber.factory(1234).asReference();
			fail();
		} catch(ClassCastException e) { }
		try {
			AbstractNumber.factory(1234).asComplex();
			fail();
		} catch(ClassCastException e) { }
	}

	@Test
	public void testWriter() {
		StringWriter stm = new StringWriter();
		Writer stm2 = new LimitingWriter(stm, 5);
		try {
			stm2.append('1');
			stm2.append('2');
			stm2.append('3');
			stm2.append('4');
			stm2.append('5');
			stm2.append('6');
			fail();
		} catch(IOException e) { }
		try {
			stm2.flush();
			stm2.close();
		} catch(IOException e) { }
		assertEquals("12345", stm.toString());
	}
}
