package styx.web.servlet;

import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import styx.Complex;
import styx.Pair;
import styx.Reference;
import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;
import styx.StyxRuntimeException;
import styx.SystemConfiguration;
import styx.Value;

@SuppressWarnings("serial")
public abstract class BaseServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(BaseServlet.class.toString());

    protected static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    protected static final String CONTENT_TYPE_STYX  = "application/styx";
    protected static final String CONTENT_TYPE_STYXS = "application/styx-script";
    protected static final String CONTENT_TYPE_TEXT = "text/plain";
    protected static final String CONTENT_TYPE_HTML = "text/html";
    protected static final String CONTENT_TYPE_XML  = "text/xml";
    protected static final String CONTENT_TYPE_JSON = "application/json";

    protected static final String sessionConfigFile  = System.getProperty("styx.web.session.config", "system.styx");
    protected static final String sessionFactoryName = System.getProperty("styx.web.session.factory");

    protected final SessionFactory sessionFactory = SessionManager.lookupSessionFactory(sessionFactoryName);

    static {
        try {
            // Load the STYX session configuration from a file.
            // If the file does not exist, at least a memory session provider will be provided.
            SystemConfiguration.load(sessionConfigFile);
            LOG.info("Loaded STYX configuration from: " + sessionConfigFile);
            LOG.info("Using STYX session factory: " + (sessionFactoryName == null || sessionFactoryName.length() == 0 ? "<default>" : sessionFactoryName));
        } catch (StyxException e) {
            LOG.severe("Cannot load STYX configuration: " + e);
            throw new StyxRuntimeException("Cannot load STYX configuration.", e);
        }
    }

    protected Session createSession() throws StyxException {
        if(sessionFactory == null) {
            throw new StyxException("No session factory.");
        }
        return sessionFactory.createSession();
    }

    protected static Reference decodeUrl(Session session, Reference root, String url) throws StyxException {
        Reference ref = root;
        while(url != null && url.length() > 0) {
            if(url.charAt(0) == '/') {
                url = url.substring(1);
            }
            int pos = url.indexOf('/');
            if(pos == -1) {
                pos = url.length();
            }
            String part = url.substring(0, pos);
            url = url.substring(pos);
            if(part.length() > 0) {
                ref = ref.child(decodeUrlPart(session, part));
            }
        }
        return ref;
    }

    protected static Value decodeUrlPart(Session session, String part) {
        try {
            return session.deserialize(part);
        } catch (StyxException e) {
            return session.text(part);
        }
    }

    /**
     * Converts the properties of a HTTP request into a STYX complex value.
     * @param session the current STYX session.
     * @param request the HTTP request.
     * @return        a map containing some properties.
     */
    protected static Complex decodeRequestProps(Session session, HttpServletRequest request) {
        return session.complex()
                .put(session.text("protocol"),    session.text  (request.getProtocol()))
                .put(session.text("scheme"),      session.text  (request.getScheme()))
                .put(session.text("http_method"), session.text  (request.getMethod()))
                .put(session.text("http_url"),    session.text  (request.getRequestURI()))
                .put(session.text("local_addr"),  session.text  (request.getLocalAddr()))
                .put(session.text("local_port"),  session.number(request.getLocalPort()))
                .put(session.text("remote_addr"), session.text  (request.getRemoteAddr()))
                .put(session.text("remote_host"), session.text  (request.getRemoteHost()))
                .put(session.text("remote_port"), session.number(request.getRemotePort()))
                .put(session.text("server_name"), session.text  (request.getServerName()))
                .put(session.text("server_port"), session.number(request.getServerPort()))
                .put(session.text("is_secure"),   session.bool  (request.isSecure()));
    }

    /**
     * Converts HTTP request parameters into a STYX complex value.
     * Note: Parameters with multiple values are not supported, only the first value is returned!
     * @param session the current STYX session.
     * @param request the HTTP request.
     * @return        a map containing parameter names and values.
     */
    protected static Complex decodeRequestParams(Session session, HttpServletRequest request) {
        Complex result = session.complex();
        Map<String, String[]> params = request.getParameterMap();
        for(Entry<String, String[]> param : params.entrySet()) {
            result = result.put(session.text(param.getKey()), session.text(param.getValue()[0]));
        }
        return result;
    }

    /**
     * Converts HTTP request headers into a STYX complex value.
     * Note: Multiple headers with the same name are not supported, only the first value is returned!
     * @param session the current STYX session.
     * @param request the HTTP request.
     * @return        a map containing header names and values.
     */
    protected static Complex decodeRequestHeaders(Session session, HttpServletRequest request) {
        Complex result = session.complex();
        Enumeration<String> names = request.getHeaderNames();
        while(names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            result = result.put(session.text(name), session.text(request.getHeader(name)));
        }
        return result;
    }

    protected static void encodeResponseHeaders(Session session, HttpServletResponse response, Value headers) {
        if(headers != null) {
            for(Pair<Value, Value> pair : headers.asComplex()) {
                Pair<Value, Value> header = pair.val().asComplex().single();
                response.addHeader(
                        header.key().asText().toTextString(),
                        header.val().asText().toTextString());
            }
        }
    }

    protected static Complex decodeRequestCookies(Session session, HttpServletRequest request) {
        Complex result = session.complex();
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                result = result.add(new CookieWrapper(cookie));
            }
        }
        return result;
    }

    protected static void encodeResponseCookies(Session session, HttpServletResponse response, Value cookies) throws StyxException {
        if(cookies != null) {
            for(Pair<Value, Value> pair : cookies.asComplex()) {
                Complex cookie = pair.val().asComplex();
                response.addCookie(new CookieWrapper(cookie).unwrap());
            }
        }
    }
}
