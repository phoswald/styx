package styx.web.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import styx.Complex;
import styx.Pair;
import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Value;
import styx.core.utils.XmlExporter;

@SuppressWarnings("serial")
public final class ContentServlet extends BaseServlet {

    private static final Logger LOG = Logger.getLogger(ContentServlet.class.toString());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long startTime = System.nanoTime();
        boolean success = false;
        try(Session session = createSession()) {
            LOG.info("REQUEST:      " + request.getContentType());
            LOG.info("QUERY STRING: " + request.getQueryString());

            Reference ref = decodeUrl(session, session.root().child(session.text("urls")), request.getPathInfo());

            Value pageDyn = session.read(ref);
            if(pageDyn == null ) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType(CONTENT_TYPE_HTML);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println("<html><body>Page " + session.serialize(ref, false) + " not found!</body></html>");
                return;
            }

            Pair<Value, Value> taggedPage = pageDyn.asComplex().single();
            PageType tag = PageType.valueOf(taggedPage.key().asText().toTextString());
            Complex page = taggedPage.val().asComplex();

            tag.dispatch(session, ref, page, request, response);
            success = true;

        } catch(RuntimeException | StyxException e) {
            throw new ServletException(e);
        } finally {
            LOG.info("Handling " + request.getMethod() + " " + request.getRequestURI() + (success ? " OK after " : " FAILED after ") + (System.nanoTime() - startTime)/1000000 + "ms.");
        }
    }

    private enum PageType {
        StaticText {
            @Override public void dispatch(Session session, Reference ref, Complex page, HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(page.get(session.text("ContentType")).asText().toTextString());
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(page.get(session.text("Body")).asText().toTextString());
            }
        },
        StaticBinary {
            @Override public void dispatch(Session session, Reference ref, Complex page, HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(page.get(session.text("ContentType")).asText().toTextString());
                response.getOutputStream().write(page.get(session.text("Body")).asBinary().toByteArray());
            }
        },
        StaticHtml {
            @Override public void dispatch(Session session, Reference ref, Complex page, HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(page.get(session.text("ContentType")).asText().toTextString());
                response.setCharacterEncoding("UTF-8");
                new XmlExporter(session).exportDocument(page.get(session.text("Body")), response.getOutputStream(), false);
            }
        },
        Dynamic {
            @Override public void dispatch(Session session, Reference ref, Complex page, HttpServletRequest request, HttpServletResponse response) throws IOException, StyxException {
                Value[] arguments = new Value[] { ref, decodeRequestProps(session, request), decodeRequestParams(session, request), decodeRequestHeaders(session, request), decodeRequestCookies(session, request) };
                Value   pageDyn   = page.get(session.text("Func")).asFunction().invoke(session, arguments);

                Pair<Value, Value> taggedPage = pageDyn.asComplex().single();
                PageType tag = PageType.valueOf(taggedPage.key().asText().toTextString());
                Complex page2 = taggedPage.val().asComplex();

                encodeResponseHeaders(session, response, page2.get(session.text("Headers")));
                encodeResponseCookies(session, response, page2.get(session.text("Cookies")));
                tag.dispatch(session, ref, page2, request, response);
            }
        };

        public abstract void dispatch(Session session, Reference ref, Complex page, HttpServletRequest request, HttpServletResponse response) throws IOException, StyxException;
    }
}
