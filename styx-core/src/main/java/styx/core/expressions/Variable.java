package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Text;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a local variable or function argument, either on the left or right hand side.
 */
public final class Variable extends AssignableExpression {

    public static final String TAG = Variable.class.getSimpleName();

    private final Text ident;
    private final Boolean mutable; // null if not yet compiled
    private final Integer offset;  // null if not yet compiled

    public Variable(Text ident) {
        this.ident   = ident;
        this.mutable = null;
        this.offset  = null;
    }

    public Variable(Text ident, boolean mutable, int offset) {
        this.ident   = ident;
        this.mutable = mutable;
        this.offset  = offset;
    }

    @Override
    protected Complex toValue() {
        return complex(text(Variable.TAG), ident);
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        return scope.lookupSymbol(ident); // results in a Variable with correct mutable and offset fields, or in a Constant.
    }

    @Override
    public AssignableExpression assignCheck() throws StyxException {
        if(!mutable) {
            throw new StyxException("The variable '" + ident.toTextString() + "' is not mutable.");
        }
        return this;
    }

    @Override
    public Determinism effects() {
        // Function arguments and local variables are both PURE.
        // - They cannot be CONSTANT because access to them cannot be optimized at compile-time.
        // - They don't need to be higher than PURE because initialization or assignment with an expression
        //    higher than PURE already makes the containing function higher than PURE.
        // - A function that evaluates its arguments automatically becomes PURE or higher, while a function
        //   that does not evaluate its arguments (or read or write references) stays CONSTANT
        return Determinism.PURE;
    }

    @Override
    public Determinism assignEffects() {
        return Determinism.CONSTANT;
    }

    @Override
    public Value evaluate(Stack stack) {
        return stack.getFrameValue(offset);
    }

    @Override
    public void assign(Stack stack, Value val) throws StyxException {
        stack.setFrameValue(offset, val);
    }
}
