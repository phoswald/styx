package styx.core.expressions;

import java.util.Objects;

import styx.Determinism;
import styx.Function;
import styx.Session;
import styx.StyxException;
import styx.Value;
import styx.core.values.AbstractValue;

/**
 * Implementation of a function that is represented as a node of the abstract syntax tree.
 * <p>
 * Note that ConcreteFunction is not itself part of the AST.
 * Rather, the AbstractFunction referenced by this class is an AST node.
 * <p>
 * ConcreteFunction and AbstractFunction reference each other in a bidirectional way.
 * This is required since ConcreteFunction is a invokable and therefore derived from Function.
 * AbstractFunction on the other hand is a AST node and therefore derived from Complex.
 */
public final class ConcreteFunction extends AbstractValue implements Function {

    private final AbstractFunction def;

    public ConcreteFunction(AbstractFunction def) {
        this.def = def;
    }

    @Override
    public int compareTo(Value other) {
        if(other.isText() || other.isReference() || other.isComplex() || other.isType()) {
            return 1;
        } else if(other.isFunction()) {
            return definition().compareTo(other.asFunction().definition());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public final boolean isFunction() {
        return true;
    }

    @Override
    public final Function asFunction() {
        return this;
    }

    @Override
    public AbstractFunction definition() {
        return def;
    }

    @Override
    public Determinism determinism() {
        return def.determinism();
    }

    @Override
    public int argumentCount() {
        return def.argumentCount();
    }

    @Override
    public final Value invoke(Session session, Value[] args) throws StyxException {
        Objects.requireNonNull(session);
        // Slow but generic call: construct a temporary stack from the given array.
        // A faster call is possible when def.invoke(Stack stack) is used directly.
        Stack stack = new Stack(session);
        for(int i = 0; args != null && i < args.length; i++) {
            stack.push(args[i]);
        }
        return def.invoke(stack);
    }
}
