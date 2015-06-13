package styx.core.expressions;

import java.util.EnumSet;
import java.util.List;

import styx.Complex;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a function definition of the form "(...) -> ...".
 */
public final class FunctionExpression extends AbstractFunction {

    public static final String TAG = "Function";

    private final Expression body;

    public FunctionExpression(boolean compiled, List<IdentifierDeclaration> args, Expression body) {
        // The 1st argument of the constructor is the return value of Function.determinism(). This is computed
        // from the body on compilation (not yet known and null when parsing). If the body evaluates the function's
        // arguments, the determinism will be PURE or higher. Otherwise, it might be CONST.
        super(compiled ? body.effects() : null, args);
        this.body = body;
    }

    public FunctionExpression(ExprFactory expf, Complex value) throws StyxException {
        super(null, expf.newIdentDecls(findMember(value, "args", true)));
        this.body = expf.newExpression (findMember(value, "body", true));
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG),
                complex().
                put(text("args"), convToComplex(args)).
                put(text("body"), body));
    }

    @Override
    public FunctionExpression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        Scope scope2 = new Scope(scope);
        for(IdentifierDeclaration arg : args) {
            scope2.registerVariable(arg, false);
        }
        return new FunctionExpression(true, args, body.compile(scope2, body instanceof Block ? CompileFlag.BODY : CompileFlag.EXPRESSION));
    }

    @Override
    public Value invoke(Stack stack) throws StyxException {
        // Fast call, but not generic: pass stack from caller to target.
        // The current stack frame is set up and moved by the caller.
        if(stack.getFrameSize() != args.size()) {
            throw new IllegalArgumentException("Argument count mismatch (expected: " + args.size() + ", provided: "+ stack.getFrameSize() + ")");
        }
        return body.evaluate(stack);
    }
}
