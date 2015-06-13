package styx.core.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import styx.Determinism;
import styx.Function;
import styx.StyxException;
import styx.Value;

/**
 * Abstract base class for nodes of the abstract syntax tree that implement a function.
 * <p>
 * There are two implementations of functions:
 * <ul>
 * <li> Function provides a function definition of the form "(...) -> ..."
 * <li> CompiledFunction provides a built-in or native function.
 */
public abstract class AbstractFunction extends Expression {

    protected final Function                 func;
    protected final Determinism              determ; // null if not yet compiled
    protected final List<IdentifierDeclaration> args;

    protected AbstractFunction(Determinism determ, List<IdentifierDeclaration> args) {
        this.func   = new ConcreteFunction(this);
        this.determ = determ;
        this.args   = args != null ? args : new ArrayList<IdentifierDeclaration>();
    }

    public final Function function() {
        return func;
    }

    public final Determinism determinism() {
        // Function.determinism(): This describes the function's dynamic behavior.
        // It is either derived from the body (for functions that were parsed from a script) or
        // specified by the subclass (for compiled functions which have a special 'native' implementation).
        return determ;
    }

    public final int argumentCount() {
        return args.size();
    }

    @Override
    public final Determinism effects() {
        // Statement.effects(): A function literal is a constant value!
        // This is totally independent from what it does when it's invoked.
        // See Function.determinism() for the dynamic behavior.
        return Determinism.CONSTANT;
    }

    @Override
    public final Value evaluate(Stack stack) {
        return func;
    }

    /**
     * Compiles the function.
     * <p>
     * The parameter flags is not used and can be left null.
     */
    @Override
    public abstract AbstractFunction compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException;

    /**
     * Invokes the function.
     * @param stack contains all function arguments.
     * @return the resulting value of the expression, can be null.
     * @throws StyxException if an exception was thrown during the evaluation of the expression,
     *                      either by a 'throw', by a called intrinsic function or by the interpreter.
     */
    public abstract Value invoke(Stack stack) throws StyxException;
}
