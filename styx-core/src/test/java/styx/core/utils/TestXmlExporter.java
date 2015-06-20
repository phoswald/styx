package styx.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;

public class TestXmlExporter {

    private static SessionFactory sf = SessionManager.createMemorySessionFactory(false);

    @Test
    public void testExportHtml() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals("<html>xxx</html>", exportXml(session, session.deserialize("@html xxx")).toString());
            assertEquals("<html>xxx</html>", exportXml(session, session.deserialize("[@html [xxx]]")).toString());
            assertEquals("<html>xxxyyy</html>", exportXml(session, session.deserialize("@html [xxx,yyy]")).toString());
            assertEquals("<html>xxxyyy</html>", exportXml(session, session.deserialize("[[@html [xxx,[[yyy]]]]]")).toString());
            assertEquals("<html><head>xxx</head><body>yyy</body></html>", exportXml(session, session.deserialize("@html [@head xxx, @body yyy]")).toString());
            assertEquals("<html><head><title>xxx</title></head><body><h1>yyy</h1><p>aaa</p><p>bbb</p></body></html>", exportXml(session, session.deserialize("@html [@head @title xxx, @body [@h1 yyy, @p aaa, @p bbb]]")).toString());
            assertEquals("<html><body><h1>yyy</h1><p id=\"i\" class=\"xxx\">aaa</p></body></html>", exportXml(session, session.deserialize("@html [@body [@h1 yyy, @p [@\"#id\" i, @\"#class\" xxx, aaa]]]")).toString());
        }
    }

    @Test
    public void testImportHtml() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals("[@html xxx]", importXml(session, "<html>xxx</html>").toString());
            assertEquals("[@html xxxyyy]", importXml(session, "<html>xxxyyy</html>").toString());
            assertEquals("[@html [@head xxx,@body yyy]]", importXml(session, "<html><head>xxx</head><body>yyy</body></html>").toString());
            assertEquals("[@html [@head [@title xxx],@body [@h1 yyy,@p aaa,@p bbb]]]", importXml(session, "<html><head><title>xxx</title></head><body><h1>yyy</h1><p>aaa</p><p>bbb</p></body></html>").toString());
            assertEquals("[@html [@body [@h1 yyy,@p [[\"#id\":i],[\"#class\":xxx],aaa]]]]", importXml(session, "<html><body><h1>yyy</h1><p id=\"i\" class=\"xxx\">aaa</p></body></html>").toString());
        }
    }

    @Test
    public void testImportLargeXml() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            session.evaluate("[/][*] = []");
            session.evaluate("[/swap][*] = []"); // XXX
            Value result = new XmlLargeExporter(session, 6, session.root().child(session.text("swap"))).
                    importDocument(new StringReader("<a><b><x>01</x><x>02</x><x>03</x><x>04</x><x>05</x><x>06</x><x>07</x><x>08</x><x>09</x><x>10</x><YYY><y/><y/><y/><y/><y/><y/><y/><y/></YYY>"+
                                                          "<x>11</x><x>12</x><x>13</x><x>14</x><x>15</x><x>16</x><x>17</x><x>18</x><x>19</x><x>20</x></b></a>"));

            assertEquals("[@a [@b [@x 01,@x 02,@x 03,@x 04,@x 05,@x 06,@x 07,@x 08,@x 09,@x 10," + "@YYY [@y [],@y [],@y [],@y [],@y [],@y [],@y [],@y []]," +
                                  "@x 11,@x 12,@x 13,@x 14,@x 15,@x 16,@x 17,@x 18,@x 19,@x 20]]]", result.toString());

            assertNull(session.evaluate("[/swap/2][*]"));

            assertEquals("[@x 01,@x 02,@x 03,@x 04,@x 05,@x 06,@x 07,@x 08,@x 09,@x 10," + "@YYY [@y [],@y [],@y [],@y [],@y [],@y [],@y [],@y []]," +
                          "@x 11,@x 12,@x 13,@x 14,@x 15,@x 16,@x 17,@x 18,@x 19,@x 20]", session.evaluate("[/swap/3][*]").toString());

            assertEquals("[@y [],@y [],@y [],@y [],@y [],@y [],@y [],@y []]", session.evaluate("[/swap/4][*]").toString());

            assertNull(session.evaluate("[/swap/5][*]"));
        }
    }

    private static String exportXml(Session session, Value val) throws IOException {
        StringWriter stm = new StringWriter();
        new XmlExporter(session).exportDocument(val, stm, false);
        return stm.toString();
    }

    private static Value importXml(Session session, String xml) throws IOException, StyxException {
        return new XmlExporter(session).importDocument(new StringReader(xml));
    }
}
