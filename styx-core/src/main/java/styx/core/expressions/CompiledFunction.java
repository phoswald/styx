package styx.core.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;

/**
 * Node of the abstract syntax tree that implements built-in or native function.
 */
public abstract class CompiledFunction extends AbstractFunction {

    public static final String TAG = CompiledFunction.class.getSimpleName();

    private final String name;

    public CompiledFunction(FuncRegistry registry, String name, Determinism determ, int numArgs) throws StyxException {
        super(determ, getArgs(numArgs));
        this.name = name;
        registry.register(name, this);
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG), text(name));
    }

    @Override
    public final CompiledFunction compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        return this;
    }

    private static List<IdentifierDeclaration> getArgs(int num) {
        List<IdentifierDeclaration> args = new ArrayList<>();
        for(int i = 0; i < num; i++) {
            args.add(new IdentifierDeclaration(text("arg" + (i+1)), null));
        }
        return args;
    }
}
