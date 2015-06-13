package styx.web.servlet;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Value;
import styx.core.intrinsics.FileIntrinsics;
import styx.core.utils.JsonSerializer;
import styx.core.utils.XmlSerializer;

@SuppressWarnings("serial")
public final class RestServlet extends BaseServlet {

    private static final Logger LOG = Logger.getLogger(RestServlet.class.toString());

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHandle(request, response, Method.HEAD);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHandle(request, response, Method.GET);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHandle(request, response, Method.POST);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHandle(request, response, Method.PUT);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHandle(request, response, Method.DELETE);
    }

    private void doHandle(HttpServletRequest request, HttpServletResponse response, Method method) throws ServletException, IOException {
        long startTime = System.nanoTime();
        boolean success = false;
        try(Session session = createSession()) {

            LOG.info("REQUEST:      " + request.getContentType());
            LOG.info("QUERY STRING: " + request.getQueryString());

            // Construct the default request parameters from the effective HTTP method (verb) and URL path.
            // The path is separated into the following parts: ContextPath/ServletPath/PathInfo
            // So, for a request to http://server:port/styx-web/rest/xxx?y=z :
            // - request.getRequestURI() returns styx-web/rest/xxx
            // - request.getPathInfo()   returns             /xxx
            RequestParamters params = new RequestParamters(method, request.getPathInfo(), request.getContentType());

            // Override request parameters with HTML form variables (either appended to URL or posted)
            params.processForm(request);

            Reference ref = decodeUrl(session, session.root(), params.path);

            Value req = null;
            if(params.method == Method.POST || params.method == Method.PUT) {
                req = readRequest(request, params, session);
            }

            // Note: the method to be executed (params.method) can be different from the actual HTTP verb (method).
            Value res = params.method.dispatch(this, session, ref, req);

            if(method == Method.GET || method == Method.POST) {
                writeResponse(response, params, session, res, ref);
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            success = true;

        } catch(RuntimeException | StyxException e) {
            throw new ServletException(e);
        } finally {
            LOG.info("Handling " + request.getMethod() + " " + request.getRequestURI() + (success ? " OK after " : " FAILED after ") + (System.nanoTime() - startTime)/1000000 + "ms.");
        }
    }

    private Value internalHead(Session session, Reference ref) throws StyxException {
        return null; // TODO (implement-):  implement head. It should be same as GET, but without response body. An ETag would be cool.
    }

    private Value internalGet(Session session, Reference ref) throws StyxException {
        return session.read(ref);
    }

    private Value internalList(Session session, Reference ref) throws StyxException {
        List<Value> result = session.browse(ref);
        return result == null ? null : session.complex().addAll(result);
    }

    private Value internalPost(Session session, Reference ref, Value req) throws StyxException {
        return req.asFunction().invoke(session, null);
    }

    private Value internalPut(Session session, Reference ref, Value req) throws StyxException {
        session.write(ref, req);
        return null;
    }

    private Value internalDelete(Session session, Reference ref) throws StyxException {
        session.write(ref, null);
        return null;
    }

    private Value readRequest(HttpServletRequest request, RequestParamters params, Session session) throws IOException, StyxException {
        Reader reader;
        if(params.requestData != null) {
            reader = new StringReader(params.requestData);
        } else if(request.getContentType() != null && request.getContentType().equals(CONTENT_TYPE_FORM)) {
            // If a form has been submitted in the request body (as can be guessed from the content type), we must not call request.getReader().
            // The request body has already been parsed by request.getParameterMap() and trying to read it again results in an exception.
            return null;
        } else {
            reader = request.getReader();
        }
        switch(params.requestContentType) {
            case CONTENT_TYPE_STYX:
                return session.deserialize(reader);
            case CONTENT_TYPE_STYXS:
                return session.parse(FileIntrinsics.readToEnd(reader));
            case CONTENT_TYPE_XML:
                return XmlSerializer.deserialize(session, reader);
            case CONTENT_TYPE_JSON:
                return JsonSerializer.deserialize(session, reader);
            default:
                // TODO (bug): Firefox generates 'application/styx-script; charset=UTF-8' when POST with jQuery's $.ajax
                throw new IOException("Invalid request content type '" + params.requestContentType + "'.");
        }
    }

    private void writeResponse(HttpServletResponse response, RequestParamters params, Session session, Value res, Value ref) throws IOException, StyxException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(params.responseContentType);
        response.setCharacterEncoding("UTF-8");

        Writer writer = response.getWriter();
        switch(params.responseContentType) {
            case CONTENT_TYPE_TEXT:
            case CONTENT_TYPE_STYX:
                session.serialize(res, writer, params.responseIndent);
                break;
            case CONTENT_TYPE_XML:
                XmlSerializer.serialize(res, writer, params.responseIndent);
                break;
            case CONTENT_TYPE_JSON:
                JsonSerializer.serialize(res, writer, params.responseIndent);
                break;
            default:
                throw new IOException("Invalid response content type '" + params.responseContentType + "'.");
        }
    }

    private enum Method {
        HEAD {
            @Override public Value dispatch(RestServlet inst, Session session, Reference ref, Value req) throws StyxException {
                return inst.internalHead(session, ref);
            }
        },
        GET {
            @Override public Value dispatch(RestServlet inst, Session session, Reference ref, Value req) throws StyxException {
                return inst.internalGet(session, ref);
            }
        },
        LIST {
            @Override public Value dispatch(RestServlet inst, Session session, Reference ref, Value req) throws StyxException {
                return inst.internalList(session, ref);
            }
        },
        POST {
            @Override public Value dispatch(RestServlet inst, Session session, Reference ref, Value req) throws StyxException {
                return inst.internalPost(session, ref, req);
            }
        },
        PUT {
            @Override public Value dispatch(RestServlet inst, Session session, Reference ref, Value req) throws StyxException {
                return inst.internalPut(session, ref, req);
            }
        },
        DELETE {
            @Override public Value dispatch(RestServlet inst, Session session, Reference ref, Value req) throws StyxException {
                return inst.internalDelete(session, ref);
            }
        };

        public abstract Value dispatch(RestServlet inst, Session session, Reference ref, Value req) throws StyxException;
    }

    private static class RequestParamters {
        public Method  method;
        public String  path;
        public String  requestData;
        public String  requestContentType = CONTENT_TYPE_STYX;
        public String  responseContentType = CONTENT_TYPE_TEXT; // send as plain text instead as STYX by default because it is browser friendly
        public boolean responseIndent = true;

        public RequestParamters(Method method, String path, String requestContentType) {
            this.method = method;
            this.path   = path;

            if(requestContentType != null) {
                this.requestContentType = requestContentType;
            }
        }

        public void processForm(HttpServletRequest request) {
            Map<String, String[]> params = request.getParameterMap();
            for(Entry<String, String[]> param : params.entrySet()) {
                if(param.getKey().equals("meth")) {
                    method = Method.valueOf(Method.class, param.getValue()[0]);
                } else if(param.getKey().equals("path")) {
                    path = param.getValue()[0];
                } else if(param.getKey().equals("req")) {
                    requestData = param.getValue()[0];
                } else if(param.getKey().equals("reqt")) {
                    requestContentType = param.getValue()[0];
                } else if(param.getKey().equals("rest")) {
                    responseContentType = param.getValue()[0];
                } else if(param.getKey().equals("ind")) {
                    responseIndent = param.getValue()[0].equals("true");
                }
            }
        }
    }
}
