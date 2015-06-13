package styx.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import styx.StyxException;
import styx.Value;
import styx.core.parser.TestAnyParser;
import styx.core.utils.Serializer;

public class TestSerializerParser extends TestAnyParser {

    @Override
    protected void testRoundTrip(String str, Value val) throws StyxException {
        Value val2 = Serializer.deserialize(session, str);
        assertNotNull(val2);
        assertEquals(0, val2.compareTo(val));
        assertEquals(val.toString(), val2.toString());
        String str2 = Serializer.serialize(val2, false);
        assertNotNull(str2);
        assertEquals(str, str2);
    }

    @Override
    protected void testParse(String str, Value val) throws StyxException {
        Value val2 = Serializer.deserialize(session, str);
        assertNotNull(val2);
        assertEquals(0, val2.compareTo(val));
        assertEquals(val.toString(), val2.toString());
    }

    @Override
    protected void testParseException(String str, String message) {
        try {
            session.deserialize(str);
            fail();
        } catch(StyxException e) {
            assertTrue(e.getMessage().contains("Failed to deserialize"));
            assertTrue(e.getCause().getMessage().contains(message));
        }
    }
}
