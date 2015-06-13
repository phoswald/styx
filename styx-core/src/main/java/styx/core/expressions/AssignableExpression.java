package styx.core.expressions;

import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Abstract base class for nodes of the abstract syntax tree that can occur on the right-hand side of an assignment.
 */
public abstract class AssignableExpression extends Expression {

    /**
     * Called after compile() if the expression occurs on the right-hand side of an assignment.
     * @return the expression itself on success.
     * @throws StyxException if the expression does not accept assignment of a value (example: an immutable variable).
     */
    public AssignableExpression assignCheck() throws StyxException { return this; }

    /**
     * Indicates which side effects the expression can have on the right-hand side of an assignment.
     * @return the determinism of a compiled expression when assign() is called.
     */
    public abstract Determinism assignEffects();

    /**
     * Assigns the given value.
     * @param stack contains all function arguments and local variables.
     * @param val the value to be assigned.
     * @throws StyxException if an exception was thrown during the assignment of the given value.
     */
    public abstract void assign(Stack stack, Value val) throws StyxException;
}
