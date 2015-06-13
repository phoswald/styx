package styx.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import styx.StyxException;
import styx.StyxRuntimeException;
import styx.Value;
import styx.core.utils.Serializer;

public class TestValueParser extends TestAnyParser {

//    @Test
//    public void testQuoteUnquote() throws StyxException {
//        testParse("123", session.text("123"));
//        testParse("@ 123", session.text("123"));
//        testParse("@ # 123", session.text("123"));
//
//        testParse("ABC", session.text("ABC"));
//        testParse("@ # ABC", session.text("ABC"));
//
//        testParse("{ 123, ABC }", session.complex(session.text("123")).add(session.text("ABC")));
//        testParse("@ { 123, # ABC }", session.complex(session.text("123")).add(session.text("ABC")));
//        testParse("@ # { 123, ABC }", session.complex(session.text("123")).add(session.text("ABC")));
//    }

//    @Test
//    public void testQuoteUnquoteInvalid() throws StyxException {
//        testParseException("#xxx", "Invalid value");
//        testParseException("@xxx", "The expression is not constant.");
//        testParseException("{#xxx}", "Invalid complex value");
//        testParseException("{@xxx}", "The expression is not constant.");
//        testParseException("[#xxx]", "Invalid reference value");
//        testParseException("[@xxx]", "The expression is not constant.");
//    }

    @Override
    protected void testRoundTrip(String str, Value val) throws StyxException {
        Parser               parser = new Parser();
        Parser.Ref<Value> output = new Parser.Ref<Value>(null);
        Parser.Ref_int       pos    = new Parser.Ref_int(0);
        parser.session = session;
        assertTrue(parser.Parse_ROOT_VALUE(str, output, pos));
        Value val2 = output.val;
        assertNotNull(val2);
        assertEquals(0, val2.compareTo(val));
        assertEquals(val.toString(), val2.toString());
        String str2 = Serializer.serialize(val2, false);
        assertNotNull(str2);
        assertEquals(str, str2);
    }

    @Override
    protected void testParse(String str, Value val) throws StyxException {
        Parser               parser = new Parser();
        Parser.Ref<Value> output = new Parser.Ref<Value>(null);
        Parser.Ref_int       pos    = new Parser.Ref_int(0);
        parser.session = session;
        assertTrue(parser.Parse_ROOT_VALUE(str, output, pos));
        Value val2 = output.val;
        assertNotNull(val2);
        assertEquals(0, val2.compareTo(val));
        assertEquals(val.toString(), val2.toString());
    }

    @Override
    protected void testParseException(String str, String message) throws StyxException {
        Parser               parser = new Parser();
        Parser.Ref<Value> output = new Parser.Ref<Value>(null);
        Parser.Ref_int       pos    = new Parser.Ref_int(0);
        parser.session = session;
        try {
            assertFalse(parser.Parse_ROOT_VALUE(str, output, pos));
//          assertTrue(e.getMessage().contains(message)); // TODO (implement) Parser does not yet generate error messages
        } catch(StyxRuntimeException e) {
            assertEquals(message, e.getMessage());
        }
    }
}
