package styx.core;

import styx.Complex;
import styx.Session;
import styx.Type;
import styx.Value;

public final class NoType implements TypeProvider {

    @Override
    public Type type(Session session, Complex environment, Value definition) {
        throw new UnsupportedOperationException("This session does not support types.");
    }
}
