package styx.core.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;

public class TestJsonSerializer {

    private static SessionFactory sf = SessionManager.createMemorySessionFactory(false);

    @Test
    public void testSerializeChars() throws StyxException {
        try(Session session = sf.createSession()) {
            StringWriter writer = new StringWriter();
            JsonSerializer.serialize(session.text("foo"), writer, false);
            assertEquals("{\"@text\":\"foo\"}", writer.toString());
        }
    }

    @Test
    public void testSerializeBytes() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonSerializer.serialize(session.text("foo"), stream, false);
            assertArrayEquals(toUtf8Bytes("{\"@text\":\"foo\"}", true), stream.toByteArray());
        }
    }

    @Test
    public void deserializeChars() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals("\"foo\"", JsonSerializer.deserialize(session, new StringReader("{\"@text\":\"foo\"}")).toString());
        }
    }

    @Test
    public void deserializeBytesNoBom() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            byte[] bytes = toUtf8Bytes("{\"@text\":\"foo\"}", false);
            assertEquals("\"foo\"", JsonSerializer.deserialize(session, new ByteArrayInputStream(bytes)).toString());
        }
    }

    @Test
    public void deserializeBytesBom() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            byte[] bytes = toUtf8Bytes("{\"@text\":\"foo\"}", true);
            assertEquals("\"foo\"", JsonSerializer.deserialize(session, new ByteArrayInputStream(bytes)).toString());
        }
    }

    @Test
    public void testIndent() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            String chars = "\n{\n    \"1\":\"foo\",\n    \"2\":\"bar\"\n}";
            byte[] bytes = toUtf8Bytes(chars, true);

            StringWriter writer = new StringWriter();
            JsonSerializer.serialize(session.deserialize("[\"foo\",\"bar\"]"), writer, true);
            assertEquals(chars, writer.toString());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonSerializer.serialize(session.deserialize("[\"foo\",\"bar\"]"), stream, true);
            assertArrayEquals(bytes, stream.toByteArray());
        }
    }

    @Test
    public void testNull() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals("{\"@null\":\"\"}", serialize(null));
            assertNull(deserialize(session, "{\"@null\":\"\"}"));
        }
    }

    @Test
    public void testRoundTrip() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals("{\"@text\":\"\"}", serializeRoundTrip(session, session.deserialize("\"\"")));
            assertEquals("{\"@text\":\"foo\"}", serializeRoundTrip(session, session.deserialize("\"foo\"")));

            assertEquals("{\"@ref\":[]}", serializeRoundTrip(session, session.deserialize("[/]")));
            assertEquals("{\"@ref\":[\"foo\",\"bar\"]}", serializeRoundTrip(session, session.deserialize("[/foo/bar]")));

            assertEquals("{}", serializeRoundTrip(session, session.deserialize("[]")));
            assertEquals("{\"1\":\"foo\",\"2\":\"bar\"}", serializeRoundTrip(session, session.deserialize("[\"foo\",\"bar\"]")));
            assertEquals("{\"A\":\"foo\",\"B\":\"bar\"}", serializeRoundTrip(session, session.deserialize("[A:\"foo\",B:\"bar\"]")));

            // a complex value with different values and but no complex key
            assertEquals(
                    "{" +
                    "\"home\":{\"@ref\":[\"home\",\"philip\"]}," +
                    "\"name\":\"philip\"," +
                    "\"tags\":{\"1\":{\"X\":\"x\"},\"2\":{\"Y\":\"y\"}},"+
                    "\"year\":\"1977\""+
                    "}",
                    serializeRoundTrip(session, session.deserialize("[ home:[/home/philip], name:\"philip\", tags:[@X \"x\", @Y \"y\"], year:1977 ]")));

            // a complex value with different values and even a complex key
            assertEquals(
                    "[" +
                    "{\"@key\":\"home\",\"@val\":{\"@ref\":[\"home\",\"philip\"]}}," +
                    "{\"@key\":\"name\",\"@val\":\"philip\"}," +
                    "{\"@key\":\"tags\",\"@val\":{\"1\":{\"X\":\"x\"},\"2\":{\"Y\":\"y\"}}},"+
                    "{\"@key\":\"year\",\"@val\":\"1977\"},"+
                    "{\"@key\":{\"k1\":\"v1\",\"k2\":\"v2\"},\"@val\":{\"1\":\"foo\",\"2\":\"bar\"}}"+
                    "]",
                    serializeRoundTrip(session, session.deserialize("[ home:[/home/philip], name:\"philip\", tags:[@X \"x\", @Y \"y\"], year:1977, [k1:\"v1\",k2:\"v2\"]:[\"foo\",\"bar\"]]")));
        }
    }

    @Test
    public void testRoundTrip2() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            // a complex with a complex key (array instead of map)
            assertEquals(
                    "[{\"@key\":{\"k1\":\"v1\",\"k2\":\"v2\"},\"@val\":{\"1\":\"foo\",\"2\":\"bar\"}}]",
                    serializeRoundTrip(session, session.deserialize("[[k1:\"v1\",k2:\"v2\"]:[\"foo\",\"bar\"]]")));

            // a complex with a complex key and a text value (<value> after <key>)
            assertEquals(
                    "[{\"@key\":{\"1\":\"complex\",\"2\":\"key\"},\"@val\":\"text\"}]",
                    serializeRoundTrip(session, session.deserialize("[[\"complex\",\"key\"]:\"text\"]")));

            // a complex with a complex key nested instead of top level
            assertEquals(
                    "{\"xxx\":[{\"@key\":{\"1\":\"complex\",\"2\":\"key\"},\"@val\":\"text\"}]}",
                    serializeRoundTrip(session, session.deserialize("@xxx [[\"complex\",\"key\"]:\"text\"]")));

            // Special handling of '@' in keys
            assertEquals(
                    "{\"@:@\":\"X\",\"@:@@\":\"X\",\"@:@@text\":\"X\",\"@:@text\":\"X\",\"@:h@@llo\":\"X\",\"@:h@llo\":\"X\",\"text\":\"X\"}",
                    serializeRoundTrip(session, session.deserialize("[ \"@\":\"X\", \"@@\":\"X\", \"@@text\":\"X\", \"@text\":\"X\", \"h@@llo\":\"X\", \"h@llo\":\"X\", \"text\":\"X\" ]")));
        }
    }

    @Test
    public void testRoundTripUnicode() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals(
                    "{\"1\":\"€\",\"2\":\"ä\",\"3\":\"ö\",\"4\":\"ü\",\"5\":\"\'\",\"6\":\"\\\"\"}",
                    serializeRoundTrip(session, session.deserialize("[ \"€\", \"ä\", \"ö\", \"ü\", \"'\", \"\\\"\" ]")));

            assertArrayEquals(
                    toUtf8Bytes("\uFEFF{\"1\":\"€\",\"2\":\"ä\",\"3\":\"ö\",\"4\":\"ü\",\"5\":\"\'\",\"6\":\"\\\"\"}", false),
                    serializeRoundTripBytes(session, session.deserialize("[ \"€\", \"ä\", \"ö\", \"ü\", \"'\", \"\\\"\" ]")));
        }
    }

    @Test
    public void testTypes() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals(
                    "{\"@type\":{\"Simple\":{}}}",
                    serializeRoundTrip(session, session.deserialize(":: @Simple [ ]")));
        }
    }

    @Test
    public void testFunctions() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals(
                    "{\"@func\":{\"Function\":{\"args\":{},\"body\":{\"Constant\":{}}}}}",
                    serializeRoundTrip(session, session.deserialize("-> @Function [ args: [ ], body: @Constant [ ] ]")));
        }
    }

    @Test
    public void testSerializeInvalid() throws StyxException {
        try(Session session = sf.createSession()) {

            try {
                JsonSerializer.serialize(session.text("XXX"), (OutputStream) null, false);
                fail();
            } catch(NullPointerException e) { }

            try {
                JsonSerializer.serialize(session.text("XXX"), (Writer) null, false);
                fail();
            } catch(NullPointerException e) { }

            try {
                Writer nasty = new Writer() {
                    @Override public void close() { }
                    @Override public void flush() { }
                    @Override public void write(char[] arg0, int arg1, int arg2) throws IOException { throw new IOException("BOOM!"); }
                };
                JsonSerializer.serialize(session.text("XXX"), nasty, false);
                fail();
            } catch(StyxException e) {
                assertTrue(e.getMessage().contains("Failed to serialize as JSON."));
            }
        }
    }

    @Test
    public void testDeserializeInvalid() throws StyxException {
        try(Session session = sf.createSession()) {

            List<String> strings = Arrays.asList(
                    "", // not well formed
                    "{", // not well formed
                    "{\"foo\"", // not well formed
                    "{\"foo\"}", // not well formed
                    "{[\"xxx\"]}", // not valid
                    "{\"@type\":\"\"}", // @type needs a nested {}
                    "{\"@func\":\"\"}", // @func needs a nested {}
                    "{\"foo\":{\"@text\":\"bar\"}}", // invalid location of @text
                    "{\"foo\":{\"@null\":\"\"}}", // invalid location of @null
                    "{\"@text\":{}}", // @text must be text
                    "{\"@null\":\"foo\"}", // @null must be empty
                    "{\"@ref\":\"foo\"}", // @ref requires [], not ""
                    "{\"@ref\":{}}", // @ref requires [], not {}
                    "[{\"@key\":1}]", // [] required 'key' and 'val'
                    "[{\"@val\":1}]", // [] required 'key' and 'val'
                    "[{\"xxx\":1}]" // [] required 'key' and 'val'
                    );

            for(String string : strings) {
                try {
                    JsonSerializer.deserialize(session, new StringReader(string));
                    fail();
                } catch(StyxException e) {
                    assertTrue(e.getMessage().contains("Failed to deserialize from JSON."));
                }
            }

            try {
                JsonSerializer.deserialize(session, (InputStream) null);
                fail();
            } catch(NullPointerException e) { }

            try {
                JsonSerializer.deserialize(session, (Reader) null);
                fail();
            } catch(NullPointerException e) { }

            try {
                Reader nasty = new Reader() {
                    @Override public void close() { }
                    @Override public int read(char[] cbuf, int off, int len) throws IOException { throw new IOException("BOOM!"); }
                };
                JsonSerializer.deserialize(session, nasty);
                fail();
            } catch(StyxException e) {
                assertTrue(e.getMessage().contains("Failed to deserialize from JSON."));
            }
        }
    }

    private static String serializeRoundTrip(Session session, Value val) throws StyxException, IOException {
        String json;
        try(StringWriter stm = new StringWriter()) {
            JsonSerializer.serialize(val, stm, false);
            json = stm.toString();
        }
        try(StringReader stm = new StringReader(json)) {
            Value val2 = JsonSerializer.deserialize(session, stm);
            assertEquals(val, val2);
        }
        return json;
    }

    private static byte[] serializeRoundTripBytes(Session session, Value val) throws StyxException, IOException {
        byte[] json;
        try(ByteArrayOutputStream stm = new ByteArrayOutputStream()) {
            JsonSerializer.serialize(val, stm, false);
            json = stm.toByteArray();
        }
        try(ByteArrayInputStream stm = new ByteArrayInputStream(json)) {
            Value val2 = JsonSerializer.deserialize(session, stm);
            assertEquals(val, val2);
        }
        return json;
    }

    private static String serialize(Value val) throws StyxException, IOException {
        try(StringWriter stm = new StringWriter()) {
            JsonSerializer.serialize(val, stm, false);
            return stm.toString();
        }
    }

    private static Value deserialize(Session session, String xml) throws StyxException {
        try(StringReader stm = new StringReader(xml)) {
            return JsonSerializer.deserialize(session, stm);
        }
    }

    private static byte[] toUtf8Bytes(String text, boolean bom) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(bom) {
            stream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
        }
        stream.write(text.getBytes(StandardCharsets.UTF_8));
        return stream.toByteArray();
    }
}
