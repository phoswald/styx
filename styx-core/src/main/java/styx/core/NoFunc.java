package styx.core;

import styx.Complex;
import styx.Function;
import styx.Session;
import styx.Value;

public final class NoFunc implements FuncProvider {

    @Override
    public Function function(Session session, Complex environment, Value definition) {
        throw new UnsupportedOperationException("This session does not support functions.");
    }
}
