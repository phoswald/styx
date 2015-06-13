package styx.core.parser;

import org.junit.Test;

import styx.Session;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;

public abstract class TestAnyParser {

    protected final Session session = SessionManager.getDetachedSession();

    protected abstract void testRoundTrip(String str, Value val) throws StyxException;

    protected abstract void testParse(String str, Value val) throws StyxException;

    protected abstract void testParseException(String str, String message) throws StyxException;

    @Test
    public void testWhitespace() throws StyxException {
        testParse(" 123 ", session.text("123"));
        testParse("/* xxx */ 123 /* xxx */", session.text("123"));
        testParse("/* xxx */ 123 /*/ xxx */", session.text("123"));
        testParse("/* xx/xx */ 123 /* xx*xx */", session.text("123"));
        testParse("  123  // xxx\n\n\n", session.text("123"));
        testParse("//xxx\n123", session.text("123"));
        testParse(" 123 // xxx", session.text("123"));
        testParse(" /* xx /* yy */ yy */ 123 ", session.text("123"));
        testParse(" /* xx /*/ yy */ yy */ 123 ", session.text("123"));
    }

    @Test
    public void testWhitespaceComplex() throws StyxException {
        testParse(" [ 123 // xxxx \n  456 ] ", session.complex(session.text("123")).add(session.text("456")));
        testParse(" [ 123 \n 456 // xxxx \n ] ", session.complex(session.text("123")).add(session.text("456")));
    }

    @Test
    public void testVoid() throws StyxException {
        testRoundTrip("\"void\"", session.empty());
        testParse("  \"void\"  ", session.empty());
    }

    @Test
    public void testBool() throws StyxException {
        testRoundTrip("\"true\"", session.bool(true));
        testParse("  \"true\"  ", session.bool(true));
    }

    @Test
    public void testNumber() throws StyxException {
        testRoundTrip("1234", session.number(1234));
        testRoundTrip("0", session.number(0));
        testRoundTrip("-123", session.number(-123));
        testRoundTrip("123.45", session.number(123.45));
        testRoundTrip("1E27", session.number("1E27"));
        testRoundTrip("1.5E27", session.number("1.5E27"));
        testParse("  1234  ", session.number(1234));
    }

    @Test
    public void testNumberInvalid() throws StyxException {
        testParseException("-", "Invalid numeric value: digits expected.");
        testParseException("1.", "Invalid numeric value: fractional digits expected.");
        testParseException("1.E", "Invalid numeric value: fractional digits expected.");
        testParseException("1.1E", "Invalid numeric value: exponential digits expected.");
        testParseException("1.1E-", "Invalid numeric value: exponential digits expected.");
        testParseException("1E-", "Invalid numeric value: exponential digits expected.");
        testParseException("1E-x", "Invalid numeric value: exponential digits expected.");
    }

    @Test
    public void testBinary() throws StyxException {
        testRoundTrip("0x", session.binary(""));
        testRoundTrip("0x0123456789ABCDEF", session.binary("0123456789ABCDEF"));
        testRoundTrip("0xDEADBEEF", session.binary("DEADBEEF"));
        testParse("  0xDEADBEEF  ", session.binary("DEADBEEF"));
    }

    @Test
    public void testBinaryInvalid() throws StyxException {
        testParseException("0xDEA", "Invalid binary value: odd number of digits.");
    }

    @Test
    public void testText() throws StyxException {
        // everything has to be quoted at top level
        testRoundTrip("\"abcd\"", session.text("abcd"));
        testRoundTrip("\"ABCD\"", session.text("ABCD"));
        testRoundTrip("\"_x_\"", session.text("_x_"));
        testRoundTrip("\"€URO\"", session.text("€URO"));
        testRoundTrip("\"foo bar\"", session.text("foo bar"));
        testRoundTrip("\"\\\\\'\\\"\\t\\r\\n\"", session.text("\\\'\"\t\r\n"));
        testRoundTrip("\"\\u0001\"", session.text("\u0001"));
        testRoundTrip("\"\\u001F\"", session.text("\u001F"));
        testRoundTrip("\"\u0100\"", session.text("\u0100"));
        testRoundTrip("\"\uFFEE\"", session.text("\uFFEE"));
        testParse("  \"abcd\"   ", session.text("abcd"));
        testParse("  \"\"   ", session.text(""));
        testParse("  \" \\\\ \\' ' \\\" \"   ", session.text(" \\ ' ' \" "));
        testParse("  \" \\u0061 \\u0062 \\u0063 \\u0064 \"   ", session.text(" a b c d "));
    }

    @Test
    public void testTextNested() throws StyxException {
        // identifiers dont have to be quoted, which includes letters >= 0x0100
        testRoundTrip("[/abcd]", session.root().child(session.text("abcd")));
        testRoundTrip("[/ABCD]", session.root().child(session.text("ABCD")));
        testRoundTrip("[/_x_]", session.root().child(session.text("_x_")));
        testRoundTrip("[/€URO]", session.root().child(session.text("€URO")));
        testRoundTrip("[/\"foo bar\"]", session.root().child(session.text("foo bar")));
        testRoundTrip("[/\"\\\\\'\\\"\\t\\r\\n\"]", session.root().child(session.text("\\\'\"\t\r\n")));
        testRoundTrip("[/\"\\u0001\"]", session.root().child(session.text("\u0001")));
        testRoundTrip("[/\"\\u001F\"]", session.root().child(session.text("\u001F")));
        testRoundTrip("[/\u0100]", session.root().child(session.text("\u0100")));
        testRoundTrip("[/\uFFEE]", session.root().child(session.text("\uFFEE")));
    }

    @Test
    public void testTextInvalid() throws StyxException {
        testParseException("\"xx\\xx\"", "Invalid text value: Invalid escape sequence.");
        testParseException("\"xx\\uyyyy\"", "Invalid text value: Invalid unicode escape sequence.");
        testParseException("\"xx\\u12\"", "Invalid text value: Invalid unicode escape sequence.");
        testParseException("\"xx", "Unexpected end of input before '\"'.");
    }

    @Test
    public void testReference() throws StyxException {
        testRoundTrip("[/]", session.root());
        testRoundTrip("[/1]", session.root().child(session.number(1)));
        testRoundTrip("[/1/2]", session.root().child(session.number(1)).child(session.number(2)));
        testParse("  [/]  ", session.root());
        testParse("  [/  1]    ", session.root().child(session.number(1)));
        testParse("  [/  1  / 2]  ", session.root().child(session.number(1)).child(session.number(2)));
    }

    @Test
    public void testReferenceInvalid() throws StyxException {
        testParseException("[/", "Invalid reference value");
        testParseException("[/void", "Invalid reference value");
        testParseException("[/void/", "Invalid reference value");
        testParseException("[/void/]", "Invalid reference value");
        testParseException("[/ /void]", "Invalid reference value");
    }

    @Test
    public void testComplex() throws StyxException {
        testRoundTrip("[]", session.complex());
        testRoundTrip("@key \"val\"", session.complex(session.text("key"), session.text("val")));
        testRoundTrip("[123:\"val\"]", session.complex(session.text("123"), session.text("val")));
        testRoundTrip("[key1:\"val1\",key2:\"val2\"]", session.complex(session.text("key1"), session.text("val1")).put(session.text("key2"), session.text("val2")));
        testRoundTrip("[\"val1\",\"val2\"]", session.complex(session.number(1), session.text("val1")).put(session.number(2), session.text("val2")));
        testParse("  @  key   \"val\"  ", session.complex(session.text("key"), session.text("val")));
        testParse(" @tag1 @tag2 \"void\"  ", session.complex(session.text("tag1"), session.complex(session.text("tag2"), session.text("void"))));
        testParse(" [/ @tag1 @tag2 \"void\"  ]  ", session.root().child(session.complex(session.text("tag1"), session.complex(session.text("tag2"), session.text("void")))));
        testParse("  [  key1  :  \"val1\"  ,  key2  :  \"val2\"  ]  ", session.complex(session.text("key1"), session.text("val1")).put(session.text("key2"), session.text("val2")));
        testParse(" \n [ \n key1  :  \"val1\" \n    key2  :  \"val2\" \n  ] \n ", session.complex(session.text("key1"), session.text("val1")).put(session.text("key2"), session.text("val2")));
        testParse(" \n [ \n key1  :  \"val1\" \n ,  key2  :  \"val2\" \n  ] \n ", session.complex(session.text("key1"), session.text("val1")).put(session.text("key2"), session.text("val2")));
        testParse(" \n [ \n key1  :  \"val1\" , \n  key2  :  \"val2\" \n  ] \n ", session.complex(session.text("key1"), session.text("val1")).put(session.text("key2"), session.text("val2")));
        testParse(" [ @person [ key1:\"val1\", key2:\"val2\" ] ] ", session.complex(session.complex(session.text("person"), session.complex(session.text("key1"), session.text("val1")).put(session.text("key2"), session.text("val2")))));
        testParse(" [ me: @person [ key1:\"val1\", key2:\"val2\" ] ] ", session.complex(session.text("me"), session.complex(session.text("person"), session.complex(session.text("key1"), session.text("val1")).put(session.text("key2"), session.text("val2")))));
        testParse(" [ key: @tag1 @tag2 \"void\" ] ", session.complex(session.text("key"), session.complex(session.text("tag1"), session.complex(session.text("tag2"), session.text("void")))));
        testParse(" [ @tag1 @tag2 \"void\" ] ", session.complex(session.complex(session.text("tag1"), session.complex(session.text("tag2"), session.text("void")))));
    }

    @Test
    public void testComplexInvalid() throws StyxException {
        testParseException("[", "Invalid complex value");
        testParseException("[void", "Invalid complex value");
        testParseException("[void:", "Invalid complex value");
        testParseException("[void:\"void\"", "Invalid complex value");
        testParseException("[void:\"void\",", "Invalid complex value");
        testParseException("[void:]", "Invalid complex value"); // "Value expected");
        testParseException("[\"void\",]", "Invalid complex value"); // "Value expected");
        testParseException("[void:\"void\",]", "Invalid complex value"); // "Value expected");
        testParseException("[,\"void\"]", "Invalid complex value"); // "Value expected");
        testParseException("@", "Invalid tagged value: key expected.");
        testParseException("@xxx", "Invalid tagged value: value expected.");
        testParseException("@xxx ", "Invalid tagged value: value expected.");
    }
}
