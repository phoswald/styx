package styx.core;

import styx.Complex;
import styx.Session;
import styx.StyxException;
import styx.Type;
import styx.Value;

public interface TypeProvider {

    public Type type(Session session, Complex environment, Value definition) throws StyxException;
}
