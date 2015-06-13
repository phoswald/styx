package styx.core.sessions;

import styx.Complex;
import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.SessionProvider;
import styx.Value;
import styx.core.memory.MemoryData;
import styx.core.memory.SharedMemoryData;
import styx.core.memory.SharedValue;
import styx.core.memory.SharedValueMemory;

public final class MemorySessionProvider implements SessionProvider {

    private static final Session detached = SessionManager.getDetachedSession();

    @Override
    public String getName() {
        return "memory";
    }

    @Override
    public SessionFactory createSessionFactory(Complex parameters) {
        Value shared = parameters == null ? null : parameters.get(detached.text("shared"));
        Value value  = parameters == null ? null : parameters.get(detached.text("value"));
        return createSessionFactory(shared == null ? false : shared.asBool().toBool(), value);
    }

    private static AbstractSessionFactory createSessionFactory(final boolean shared, final Value value) {
        if(shared) {
            final SharedValue state = new SharedValueMemory(value);
            return new AbstractSessionFactory() {
                @Override
                public Session createSession() {
                    return new ConcreteSession(new SharedMemoryData(state.clone()), type, func, eval, environment);
                }
            };
        } else {
            return new AbstractSessionFactory() {
                @Override
                public Session createSession() {
                    return new ConcreteSession(new MemoryData(value), type, func, eval, environment);
                }
            };
        }
    }
}
