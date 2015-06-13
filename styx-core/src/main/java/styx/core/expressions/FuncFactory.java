package styx.core.expressions;

import java.util.Objects;

import styx.Complex;
import styx.StyxException;
import styx.Function;
import styx.Session;
import styx.Value;
import styx.core.FuncProvider;

public final class FuncFactory implements FuncProvider {

    private final FuncRegistry registry;

    public FuncFactory(FuncRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Function function(Session session, Complex environment, Value definition) throws StyxException {
        return new ExprFactory(registry)
            .newFunction(Objects.requireNonNull(definition))
            .compile(new Scope(session, environment), null)
            .function();
    }
}
