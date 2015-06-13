package styx.core.sessions;

import styx.Complex;
import styx.Session;
import styx.SessionFactory;
import styx.SessionProvider;
import styx.StyxException;
import styx.core.NoData;
import styx.core.NoEval;
import styx.core.NoFunc;
import styx.core.NoType;
import styx.core.values.AbstractValue;

public final class DetachedSessionProvider implements SessionProvider {

    private static final Session instance = new ConcreteSession(new NoData(), new NoType(), new NoFunc(), new NoEval(), AbstractValue.complex());

    @Override
    public String getName() {
        return "detached";
    }

    @Override
    public SessionFactory createSessionFactory(Complex parameters) throws StyxException {
        return new SessionFactory() {
            @Override
            public Session createSession() {
                return instance;
            }
        };
    }
}
