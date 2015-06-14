package styx.core.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
import java.nio.file.Path;

import org.junit.Test;

import styx.Session;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;

public class TestSerializer {

    private Session session = SessionManager.getDetachedSession();

    @Test
    public void testSerializeChars() throws StyxException {
        StringWriter writer = new StringWriter();
        Serializer.serialize(session.text("foo"), writer, false);
        assertEquals("\"foo\"", writer.toString());
    }

    @Test
    public void testSerializeBytes() throws StyxException, IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Serializer.serialize(session.text("foo"), stream, false);
        assertArrayEquals(toUtf8Bytes("\"foo\"", true), stream.toByteArray());
    }

    @Test
    public void deserializeChars() throws StyxException {
        assertEquals("\"foo\"", Serializer.deserialize(session, new StringReader("\"foo\"")).toString());
    }

    @Test
    public void deserializeBytesNoBom() throws StyxException, IOException {
        byte[] bytes = toUtf8Bytes("\"foo\"", false);
        assertEquals("\"foo\"", Serializer.deserialize(session, new ByteArrayInputStream(bytes)).toString());
    }

    @Test
    public void deserializeBytesBom() throws StyxException, IOException {
        byte[] bytes = toUtf8Bytes("\"foo\"", true);
        assertEquals("\"foo\"", Serializer.deserialize(session, new ByteArrayInputStream(bytes)).toString());
    }

    @Test(expected=NullPointerException.class)
    public void testNoOutPath() throws StyxException {
        Serializer.serialize(session.empty(), (Path) null, false);
    }

    @Test(expected=NullPointerException.class)
    public void testNoOutStream() throws StyxException {
        Serializer.serialize(session.empty(), (OutputStream) null, false);
    }

    @Test(expected=NullPointerException.class)
    public void testNoWriter() throws StyxException {
        Serializer.serialize(session.empty(), (Writer) null, false);
    }

    @Test
    public void testNoValue1() throws StyxException {
        assertEquals("", Serializer.serialize((Value) null, false));
    }

    @Test
    public void testNoValue2() throws StyxException {
        ByteArrayOutputStream stm = new ByteArrayOutputStream();
        Serializer.serialize(null, stm, false);
        assertEquals(0, stm.toByteArray().length);
    }

    @Test
    public void testNoValue3() throws StyxException {
        StringWriter stm = new StringWriter();
        Serializer.serialize(null, stm, false);
        assertEquals("", stm.toString());
    }

    @Test(expected=NullPointerException.class)
    public void testNoSession1() throws StyxException {
        assertNull(Serializer.deserialize(null, (String) null));
    }

    @Test(expected=NullPointerException.class)
    public void testNoSession2() throws StyxException {
        Serializer.deserialize(null, (Path) null);
    }

    @Test(expected=NullPointerException.class)
    public void testNoSession3() throws StyxException {
        Serializer.deserialize(null, (InputStream) null);
    }

    @Test(expected=NullPointerException.class)
    public void testNoSession4() throws StyxException {
        Serializer.deserialize(null, (Reader) null);
    }

    public void testNoString() throws StyxException {
        assertNull(Serializer.deserialize(session, (String) null));
    }

    @Test(expected=NullPointerException.class)
    public void testNoPath() throws StyxException {
        Serializer.deserialize(session, (Path) null);
    }

    @Test(expected=NullPointerException.class)
    public void testNoInStream() throws StyxException {
        Serializer.deserialize(session, (InputStream) null);
    }

    @Test(expected=NullPointerException.class)
    public void testNoReader() throws StyxException {
        Serializer.deserialize(session, (Reader) null);
    }

    @Test
    public void testNoText() throws StyxException, IOException {
        assertNull(Serializer.deserialize(session, new ByteArrayInputStream(toUtf8Bytes("", false))));
        assertNull(Serializer.deserialize(session, new ByteArrayInputStream(toUtf8Bytes("", true))));
        assertNull(Serializer.deserialize(session, new StringReader("")));
        assertNull(Serializer.deserialize(session, ""));
        assertNull(Serializer.deserialize(session, "\uFEFF \t\r\n"));
        assertNull(Serializer.deserialize(session, new StringReader(" \t\r\n")));
        assertNull(Serializer.deserialize(session, new StringReader("\uFEFF \t\r\n")));
        assertEquals("\"\"", Serializer.serialize(session.text(null), false));
    }

    @Test
    public void testSimpleText() throws StyxException, IOException {
        assertEquals("\"abcd\"", Serializer.deserialize(session, new ByteArrayInputStream(toUtf8Bytes("\"abcd\"", false))).toString());
        assertEquals("\"abcd\"", Serializer.deserialize(session, new ByteArrayInputStream(toUtf8Bytes("\"abcd\"", true))).toString());
        assertEquals("\"abcd\"", Serializer.deserialize(session, new StringReader("\"abcd\"")).toString());
        assertEquals("\"abcd\"", Serializer.deserialize(session, "\"abcd\"").toString());
        assertEquals("\"abcd\"", Serializer.deserialize(session, "\uFEFF\"abcd\"").toString());
        assertEquals("\"abcd\"", Serializer.deserialize(session, new StringReader(" \t\r\n \"abcd\" \t\r\n ")).toString());
        assertEquals("\"abcd\"", Serializer.deserialize(session, new StringReader("\uFEFF \t\r\n \"abcd\" \t\r\n ")).toString());
        ByteArrayOutputStream stm = new ByteArrayOutputStream();
        Serializer.serialize(session.text("abcd"), stm, false);
        assertArrayEquals(toUtf8Bytes("\"abcd\"",true), stm.toByteArray());
        StringWriter wrt = new StringWriter();
        Serializer.serialize(session.text("abcd"), wrt, false);
        assertEquals("\"abcd\"", wrt.toString());
        assertEquals("\"abcd\"", Serializer.serialize(session.text("abcd"), false));
    }

    @Test(expected=StyxException.class)
    public void testNoEOF() throws StyxException {
        Serializer.deserialize(session, new StringReader("void $$$"));
    }

    @Test
    public void testFormatNoIndent() throws StyxException {
        assertEquals("@key1 \"val1\"", format("[key1:\"val1\"]", false));
        assertEquals("[1234:\"val1\"]", format("[1234:\"val1\"]", false));
        assertEquals("[key1:\"val1\",key2:\"val2\"]", format("[key2:\"val2\",key1:\"val1\"]", false));
        assertEquals("[key1:\"val1\",[3:4]:[5:6]]", format("[key1:\"val1\",[3:4]:[5:6]]", false));
        assertEquals("@x [a:\"b\",c:\"d\"]", format("[x:[a:\"b\",c:\"d\"]]", false));
        assertEquals("[2:[a:\"b\",c:\"d\"]]", format("[2:[a:\"b\",c:\"d\"]]", false));

        assertEquals("@key1 \"val1\"", format("@key1 \"val1\"", false));
        assertEquals("[key1:\"val1\",[3:4]:[5:6]]", format("[key1:\"val1\",@3 4:@5 6]", false));
        assertEquals("@x [a:\"b\",c:\"d\"]", format("@x [a:\"b\",c:\"d\"]", false));
    }

    @Test
    public void testFormatIndent() throws StyxException {
        assertEquals("@key1 \"val1\"", format("[key1:\"val1\"]", true));
        assertEquals("[\n    1234: \"val1\"\n]", format("[1234:\"val1\"]", true));
        assertEquals("[\n    key1: \"val1\"\n    key2: \"val2\"\n]", format("[key2:\"val2\",key1:\"val1\"]", true));
        assertEquals("[\n    key1: 1\n    key2: [\n        key21: 21\n        key22: 22\n    ]\n    [1234: 3]: 33\n]", format("[key1:1,key2:[key21:21,key22:22],[1234:3]:33]", true));
        assertEquals("[\n    key1: [\n        key11: \"val11\"\n        key12: \"val12\"\n    ]\n    key2: \"val2\"\n]", format("[key2:\"val2\",key1:[key11:\"val11\",key12:\"val12\"]]", true));
        assertEquals("[\n    @person [\n        age: 36\n        name: \"philip\"\n    ]\n    @person [\n        age: 5\n        name: \"marc\"\n    ]\n]", format("[1:[person:[name:\"philip\",age:36]],2:[person:[name:\"marc\",age:5]]]", true));
        assertEquals("[\n    [\n        123456: [\n            age: 36\n            name: \"philip\"\n        ]\n    ]\n    [\n        123456: [\n            age: 5\n            name: \"marc\"\n        ]\n    ]\n]", format("[1:[123456:[name:\"philip\",age:36]],2:[123456:[name:\"marc\",age:5]]]", true));
        assertEquals("[\n    me: @person [\n        age: 36\n        name: \"philip\"\n    ]\n    you: @person [\n        age: 5\n        name: \"marc\"\n    ]\n]", format("[me:[person:[name:\"philip\",age:36]],you:[person:[name:\"marc\",age:5]]]", true));
        assertEquals("[\n    me: [\n        123456: [\n            age: 36\n            name: \"philip\"\n        ]\n    ]\n    you: [\n        123456: [\n            age: 5\n            name: \"marc\"\n        ]\n    ]\n]", format("[me:[123456:[name:\"philip\",age:36]],you:[123456:[name:\"marc\",age:5]]]", true));
//      assertEquals("[\n    [ref, one]: [\n        tag: [\n            foo: bar\n            name: complex1\n        ]\n    ]\n    [ref, [a: b, c: d, e: f]]: void\n    [ref, [two: three]]: void\n]", format("[[ref,one]:[tag:[name:complex1,foo:bar]],[ref,[two:three]]:void,[ref,[a:b,c:d,e:f]]:\"void\"]", true));
    }

    private String format(String str, boolean indent) throws StyxException {
        Value val = Serializer.deserialize(session, str);
        return Serializer.serialize(val, indent);
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
