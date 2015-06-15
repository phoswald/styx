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

public class TestXmlSerializer {

    private static SessionFactory sf = SessionManager.createMemorySessionFactory(false);

    @Test
    public void testSerializeChars() throws StyxException {
        try(Session session = sf.createSession()) {
            StringWriter writer = new StringWriter();
            XmlSerializer.serialize(session.text("foo"), writer, false);
            assertEquals("<?xml version=\"1.0\" ?><text>foo</text>", writer.toString());
        }
    }

    @Test
    public void testSerializeBytes() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            XmlSerializer.serialize(session.text("foo"), stream, false);
            assertArrayEquals(toUtf8Bytes("<?xml version=\"1.0\" encoding=\"utf-8\"?><text>foo</text>", true), stream.toByteArray());
        }
    }

    @Test
    public void deserializeChars() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals("foo", XmlSerializer.deserialize(session, new StringReader("<?xml version=\"1.0\" ?><text>foo</text>")).toString());
        }
    }

    @Test
    public void deserializeBytesNoBom() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            byte[] bytes = toUtf8Bytes("<?xml version=\"1.0\" ?><text>foo</text>", false);
            assertEquals("foo", XmlSerializer.deserialize(session, new ByteArrayInputStream(bytes)).toString());
        }
    }

    @Test
    public void deserializeBytesBom() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            byte[] bytes = toUtf8Bytes("<?xml version=\"1.0\" ?><text>foo</text>", true);
            assertEquals("foo", XmlSerializer.deserialize(session, new ByteArrayInputStream(bytes)).toString());
        }
    }

    @Test
    public void testIndent() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            String chars = "<?xml version=\"1.0\" ?>\n<complex>\n    <text key=\"1\">foo</text>\n    <text key=\"2\">bar</text>\n</complex>";
            byte[] bytes = toUtf8Bytes("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<complex>\n    <text key=\"1\">foo</text>\n    <text key=\"2\">bar</text>\n</complex>", true);

            StringWriter writer = new StringWriter();
            XmlSerializer.serialize(session.deserialize("[foo,bar]"), writer, true);
            assertEquals(chars, writer.toString());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            XmlSerializer.serialize(session.deserialize("[foo,bar]"), stream, true);
            assertArrayEquals(bytes, stream.toByteArray());
        }
    }

    @Test
    public void testNull() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals("<null/>", serialize(null));
            assertNull(deserialize(session, "<null/>"));
        }
    }

    @Test
    public void testRoundTrip() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals("<text></text>", serializeRoundTrip(session, session.deserialize("\"\"")));
            assertEquals("<text>123</text>", serializeRoundTrip(session, session.deserialize("123")));
            assertEquals("<text>foo</text>", serializeRoundTrip(session, session.deserialize("foo")));

            assertEquals("<reference></reference>", serializeRoundTrip(session, session.deserialize("[/]")));
            assertEquals("<reference><text>foo</text><text>bar</text></reference>", serializeRoundTrip(session, session.deserialize("[/foo/bar]")));

            assertEquals("<complex></complex>", serializeRoundTrip(session, session.deserialize("[]")));
            assertEquals("<complex><text key=\"1\">foo</text><text key=\"2\">bar</text></complex>", serializeRoundTrip(session, session.deserialize("[foo,bar]")));
            assertEquals("<complex><text key=\"A\">foo</text><text key=\"B\">bar</text></complex>", serializeRoundTrip(session, session.deserialize("[A:foo,B:bar]")));

            // a complex value with different values and even a complex key
            assertEquals(
                    "<complex>" +
                    "<reference key=\"home\"><text>home</text><text>philip</text></reference>" +
                    "<text key=\"name\">philip</text>" +
                    "<complex key=\"tags\"><complex key=\"1\"><text key=\"X\">x</text></complex><complex key=\"2\"><text key=\"Y\">y</text></complex></complex>"+
                    "<text key=\"year\">1977</text>"+
                    "<complex><key><complex><text key=\"k1\">v1</text><text key=\"k2\">v2</text></complex></key><text key=\"1\">foo</text><text key=\"2\">bar</text></complex>"+
                    "</complex>",
                    serializeRoundTrip(session, session.deserialize("[ home:[/home/philip], name:philip, tags:[@X x, @Y y], year:1977, [k1:v1,k2:v2]:[foo,bar]]")));

        }
    }

    @Test
    public void testRoundTrip2() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            // a complex with a complex key (<key> instead of key=...)
            assertEquals(
                    "<complex><complex><key><complex><text key=\"k1\">v1</text><text key=\"k2\">v2</text></complex></key><text key=\"1\">foo</text><text key=\"2\">bar</text></complex></complex>",
                    serializeRoundTrip(session, session.deserialize("[[k1:v1,k2:v2]:[foo,bar]]")));

            // a complex with a complex key and a text value (<value> after <key>)
            assertEquals(
                    "<complex><text><key><complex><text key=\"1\">complex</text><text key=\"2\">key</text></complex></key><value>text</value></text></complex>",
                    serializeRoundTrip(session, session.deserialize("[[complex,key]:text]")));
        }
    }

    @Test
    public void testRoundTripUnicode() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals(
                    "<complex><text key=\"1\">€</text><text key=\"2\">ä</text><text key=\"3\">ö</text><text key=\"4\">ü</text><text key=\"5\">\'</text><text key=\"6\">\"</text></complex>",
                    serializeRoundTrip(session, session.deserialize("[ \"€\", \"ä\", \"ö\", \"ü\", \"'\", \"\\\"\" ]")));

            assertArrayEquals(
                    toUtf8Bytes("<?xml version=\"1.0\" encoding=\"utf-8\"?><complex><text key=\"1\">€</text><text key=\"2\">ä</text><text key=\"3\">ö</text><text key=\"4\">ü</text><text key=\"5\">\'</text><text key=\"6\">\"</text></complex>", true),
                    serializeRoundTripBytes(session, session.deserialize("[ \"€\", \"ä\", \"ö\", \"ü\", \"'\", \"\\\"\" ]")));
        }
    }

    @Test
    public void testTypes() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals(
                    "<type><complex><complex key=\"Simple\"></complex></complex></type>",
                    serializeRoundTrip(session, session.deserialize(":: @Simple [ ]")));
        }
    }

    @Test
    public void testFunctions() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals(
                    "<function><complex><complex key=\"Function\"><complex key=\"args\"></complex><complex key=\"body\"><complex key=\"Constant\"></complex></complex></complex></complex></function>",
                    serializeRoundTrip(session, session.deserialize("-> @Function [ args: [ ], body: @Constant [ ] ]")));
        }
    }

    @Test
    public void testSerializeInvalid() throws StyxException {
        try(Session session = sf.createSession()) {

            try {
                XmlSerializer.serialize(session.text("XXX"), (OutputStream) null, false);
                fail();
            } catch(NullPointerException e) { }

            try {
                XmlSerializer.serialize(session.text("XXX"), (Writer) null, false);
                fail();
            } catch(NullPointerException e) { }

            try {
                Writer nasty = new Writer() {
                    @Override public void close() { }
                    @Override public void flush() { }
                    @Override public void write(char[] arg0, int arg1, int arg2) throws IOException { throw new IOException("BOOM!"); }
                };
                XmlSerializer.serialize(session.text("XXX"), nasty, false);
                fail();
            } catch(StyxException e) {
                assertTrue(e.getMessage().contains("Failed to serialize as XML."));
            }
        }
    }

    @Test
    public void testDeserializeInvalid() throws StyxException {
        try(Session session = sf.createSession()) {

            List<String> strings = Arrays.asList(
                    "", // not well formed
                    "<foo>", // not well formed
                    "<foo></foo>", // not valid
                    "<complex><text>xxxx</text></complex>", // missing attribute key='...'
                    "<complex><text aaa='bbb'>xxxx</text></complex>", // invalid attribute
                    "<type></type>", // <type> needs a nested <complex>
                    "<type><text/></type>", // <type> needs a nested <complex>
                    "<function></function>", // <function> needs a nested <complex>
                    "<function><text/></function>", // <function> needs a nested <complex>
                    "<key><text/></key>" // invalid location of <key>
                    );

            for(String string : strings) {
                try {
                    XmlSerializer.deserialize(session, new StringReader(string));
                    fail();
                } catch(StyxException e) {
                    assertTrue(e.getMessage().contains("Failed to deserialize from XML."));
                }
            }

            try {
                XmlSerializer.deserialize(session, (InputStream) null);
                fail();
            } catch(NullPointerException e) { }

            try {
                XmlSerializer.deserialize(session, (Reader) null);
                fail();
            } catch(NullPointerException e) { }

            try {
                Reader nasty = new Reader() {
                    @Override public void close() { }
                    @Override public int read(char[] cbuf, int off, int len) throws IOException { throw new IOException("BOOM!"); }
                };
                XmlSerializer.deserialize(session, nasty);
                fail();
            } catch(StyxException e) {
                assertTrue(e.getMessage().contains("Failed to deserialize from XML."));
            }
        }
    }

    private static String serializeRoundTrip(Session session, Value val) throws StyxException, IOException {
        String xml;
        try(StringWriter stm = new StringWriter()) {
            XmlSerializer.serialize(val, stm, false);
            xml = stm.toString();
        }
        try(StringReader stm = new StringReader(xml)) {
            Value val2 = XmlSerializer.deserialize(session, stm);
            assertEquals(val, val2);
        }
        assertTrue(xml.startsWith("<?xml version=\"1.0\" ?>"));
        return xml.replace("<?xml version=\"1.0\" ?>", "");
    }

    private static byte[] serializeRoundTripBytes(Session session, Value val) throws StyxException, IOException {
        byte[] json;
        try(ByteArrayOutputStream stm = new ByteArrayOutputStream()) {
            XmlSerializer.serialize(val, stm, false);
            json = stm.toByteArray();
        }
        try(ByteArrayInputStream stm = new ByteArrayInputStream(json)) {
            Value val2 = XmlSerializer.deserialize(session, stm);
            assertEquals(val, val2);
        }
        return json;
    }

    private static String serialize(Value val) throws StyxException, IOException {
        String xml;
        try(StringWriter stm = new StringWriter()) {
            XmlSerializer.serialize(val, stm, false);
            xml = stm.toString();
        }
        assertTrue(xml.startsWith("<?xml version=\"1.0\" ?>"));
        return xml.replace("<?xml version=\"1.0\" ?>", "");
    }

    private static Value deserialize(Session session, String xml) throws StyxException {
        try(StringReader stm = new StringReader(xml)) {
            return XmlSerializer.deserialize(session, stm);
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
