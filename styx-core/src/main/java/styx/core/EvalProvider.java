package styx.core;

import styx.Complex;
import styx.StyxException;
import styx.Function;
import styx.Session;

public interface EvalProvider {

    public Function parse(Session session, Complex environment, String script, boolean compile) throws StyxException;
}
