package styx.core.sessions;

import styx.Complex;
import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;
import styx.StyxRuntimeException;
import styx.Value;
import styx.core.EvalProvider;
import styx.core.FuncProvider;
import styx.core.TypeProvider;
import styx.core.expressions.FuncFactory;
import styx.core.expressions.FuncRegistry;
import styx.core.intrinsics.AllIntrinsics;
import styx.core.parser.Interpreter;
import styx.core.types.TypeFactory;

public abstract class AbstractSessionFactory implements SessionFactory {

    protected static final Session detached = SessionManager.getDetachedSession();

    protected final FuncRegistry registry = new FuncRegistry();

    protected final TypeProvider type = new TypeFactory();
    protected final FuncProvider func = new FuncFactory(registry);
    protected final EvalProvider eval = new Interpreter();

    protected Complex environment;

    protected AbstractSessionFactory() {
        try {
            this.environment = AllIntrinsics.buildEnvironment(registry, detached);
        } catch (StyxException e) {
            throw new StyxRuntimeException("Cannot build environment.", e);
        }
    }

    public AbstractSessionFactory addEnvironment(Value key, Value val) {
        environment = environment.put(key, val);
        return this;
    }

    public FuncRegistry getRegistry() {
        return registry;
    }
}
