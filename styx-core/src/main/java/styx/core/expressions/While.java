package styx.core.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a "loop ...", "while(...) ..." or "do ... while(...)" statement.
 */
public final class While extends Expression {

    public static final String TAG = While.class.getSimpleName();

    private final Expression precond;
    private final Expression postcond;
    private final Expression expr;

    public While(Expression precond, Expression postcond, Expression expr) {
        this.precond  = precond;
        this.postcond = postcond;
        this.expr     = expr;
    }

    public While(ExprFactory expf, Complex value) throws StyxException {
        this.precond  = expf.newExpression(findMember(value, "precond",  false));
        this.postcond = expf.newExpression(findMember(value, "postcond", false));
        this.expr     = expf.newExpression(findMember(value, "expr",     true));
    }

    @Override
    protected Complex toValue() {
        return complex(text(While.class.getSimpleName()),
                complex().
                put(text("precond"),  precond).
                put(text("postcond"), postcond).
                put(text("expr"),     expr));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        return new While(
                precond  != null ? precond. compile(scope, CompileFlag.EXPRESSION) : null,
                postcond != null ? postcond.compile(scope, CompileFlag.EXPRESSION) : null,
                expr.compile(scope, CompileFlag.add(flags, CompileFlag.AllowYield, CompileFlag.AllowBreak, CompileFlag.AllowContinue)));
    }

    @Override
    public Determinism effects() {
        return maxEffects(precond, postcond, expr);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        List<Value> result = new ArrayList<Value>();
        while(true) {
            if(precond != null && precond.evaluate(stack).asBool().toBool() == false) {
                break;
            }
            Flow flow = expr.execute(stack);
            if(flow == Flow.Yield) {
                result.add(stack.getResult());
            } else if(flow == Flow.Break) {
                break;
            } else if(flow == Flow.Continue) {
                continue; // TODO also execute postcond?
            } else if(flow != null) {
                throw new UnsupportedOperationException("Invalid control flow statement in while(...) expression.");
            }
            if(postcond != null && postcond.evaluate(stack).asBool().toBool() == false) {
                break;
            }
        }
        return stack.session().complex().addAll(result);
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        while(true) {
            if(precond != null && precond.evaluate(stack).asBool().toBool() == false) {
                break;
            }
            Flow flow = expr.execute(stack);
            if(flow == Flow.Yield) {
                // result.add(stack.getResult()); // TODO really allow 'yield' here?
            } else if(flow == Flow.Break) {
                break;
            } else if(flow == Flow.Continue) {
                continue; // TODO also execute postcond?
            } else if(flow != null) {
                return flow;
            }
            if(postcond != null && postcond.evaluate(stack).asBool().toBool() == false) {
                break;
            }
        }
        return null;
    }
}
