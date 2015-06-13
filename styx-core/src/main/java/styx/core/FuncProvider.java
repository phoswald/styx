package styx.core;

import styx.Complex;
import styx.StyxException;
import styx.Function;
import styx.Session;
import styx.Value;

public interface FuncProvider {

    public Function function(Session session, Complex environment, Value definition) throws StyxException;
}
