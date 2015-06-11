package styx.core;

import styx.Complex;
import styx.Function;
import styx.Session;
import styx.StyxException;

public interface EvalProvider {

    public Function parse(Session session, Complex environment, String script, boolean compile) throws StyxException;
}
