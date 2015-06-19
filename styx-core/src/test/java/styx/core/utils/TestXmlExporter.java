package styx.core.utils;

import static org.junit.Assert.assertEquals;

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
            assertEquals("<html>xxx</html>", exportXml(session.deserialize("@html xxx")).toString());
            assertEquals("<html>xxx</html>", exportXml(session.deserialize("[@html [xxx]]")).toString());
            assertEquals("<html>xxxyyy</html>", exportXml(session.deserialize("@html [xxx,yyy]")).toString());
            assertEquals("<html>xxxyyy</html>", exportXml(session.deserialize("[[@html [xxx,[[yyy]]]]]")).toString());
            assertEquals("<html><head>xxx</head><body>yyy</body></html>", exportXml(session.deserialize("@html [@head xxx, @body yyy]")).toString());
            assertEquals("<html><head><title>xxx</title></head><body><h1>yyy</h1><p>aaa</p><p>bbb</p></body></html>", exportXml(session.deserialize("@html [@head @title xxx, @body [@h1 yyy, @p aaa, @p bbb]]")).toString());
            assertEquals("<html><body><h1>yyy</h1><p id=\"i\" class=\"xxx\">aaa</p></body></html>", exportXml(session.deserialize("@html [@body [@h1 yyy, @p [@\"#id\" i, @\"#class\" xxx, aaa]]]")).toString());
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

    private static String exportXml(Value val) throws IOException {
        StringWriter stm = new StringWriter();
        XmlExporter.exportDocument(val, stm, false);
        return stm.toString();
    }

    private static Value importXml(Session session, String xml) throws IOException {
        return XmlExporter.importDocument(session, new StringReader(xml));
    }
}
