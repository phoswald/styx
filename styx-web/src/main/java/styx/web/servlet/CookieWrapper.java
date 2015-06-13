package styx.web.servlet;

import javax.servlet.http.Cookie;

import styx.Complex;
import styx.StyxException;
import styx.core.values.CompiledComplex;

public final class CookieWrapper extends CompiledComplex {

    private final Cookie cookie;

    public CookieWrapper(Cookie cookie) {
        super(null);
        this.cookie = cookie;
    }

    public CookieWrapper(Complex value) throws StyxException {
        super(value);
        this.cookie = new Cookie(
                convToString(findMember(value, "name",  true)),
                convToString(findMember(value, "value", true)));
        if(findMember(value, "comment",   false) != null) cookie.setComment (convToString (findMember(value, "comment",   false)));
        if(findMember(value, "domain",    false) != null) cookie.setDomain  (convToString (findMember(value, "domain",    false)));
        if(findMember(value, "max_age",   false) != null) cookie.setMaxAge  (convToInteger(findMember(value, "max_age",   false), 0));
        if(findMember(value, "path",      false) != null) cookie.setPath    (convToString (findMember(value, "path",      false)));
        if(findMember(value, "secure",    false) != null) cookie.setSecure  (convToBoolean(findMember(value, "secure",    false), false));
        if(findMember(value, "version",   false) != null) cookie.setVersion (convToInteger(findMember(value, "version",   false), 0));
        if(findMember(value, "http_only", false) != null) cookie.setHttpOnly(convToBoolean(findMember(value, "http_only", false), false));
    }

    public Cookie unwrap() {
        return cookie;
    }

    @Override
    protected Complex toValue() {
        return complex()
                .put(text("name"),      text  (cookie.getName()))
                .put(text("value"),     text  (cookie.getValue()))
                .put(text("comment"),   text  (cookie.getComment()))
                .put(text("domain"),    text  (cookie.getDomain()))
                .put(text("max_age"),   number(cookie.getMaxAge()))
                .put(text("path"),      text  (cookie.getPath()))
                .put(text("secure"),    bool  (cookie.getSecure()))
                .put(text("version"),   number(cookie.getVersion()))
                .put(text("http_only"), bool  (cookie.isHttpOnly()));
    }
}
