package styx.core.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a "for(..., ..., ...) ..." statement.
 */
public final class For extends Expression {

    public static final String TAG = For.class.getSimpleName();

    private final Expression decl;
    private final Expression cond;
    private final Expression incr;
    private final Expression expr;

    public For(Expression decl, Expression cond, Expression incr, Expression expr) {
        this.decl = decl;
        this.cond = cond;
        this.incr = incr;
        this.expr = expr;
    }

    public For(ExprFactory expf, Complex value) throws StyxException {
        this.decl = expf.newExpression(findMember(value, "decl", true));
        this.cond = expf.newExpression(findMember(value, "cond", true));
        this.incr = expf.newExpression(findMember(value, "incr", true));
        this.expr = expf.newExpression(findMember(value, "expr", true));
    }

    @Override
    protected Complex toValue() {
        return complex(text(For.class.getSimpleName()),
                complex().
                put(text("decl"), decl).
                put(text("cond"), cond).
                put(text("incr"), incr).
                put(text("expr"), expr));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        int base = scope.enterBlock();
        For result = new For(
                decl.compile(scope, CompileFlag.add(CompileFlag.EXPRESSION, CompileFlag.AllowDeclaration)),
                cond.compile(scope, CompileFlag.EXPRESSION),
                incr.compile(scope, CompileFlag.add(CompileFlag.EXPRESSION, CompileFlag.AllowAssignment)),
                expr.compile(scope, CompileFlag.add(flags, CompileFlag.AllowYield, CompileFlag.AllowBreak, CompileFlag.AllowContinue)));
        scope.leaveBlock(base);
        return result;
    }

    @Override
    public Determinism effects() {
        return maxEffects(cond, incr, expr);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        List<Value> result = new ArrayList<Value>();
        decl.evaluate(stack);
        while(cond.evaluate(stack).asBool().toBool()) {
            Flow flow = expr.execute(stack);
            if(flow == Flow.Yield) {
                result.add(stack.getResult());
            } else if(flow == Flow.Break) {
                break;
            } else if(flow == Flow.Continue) {
                continue; // TODO also execute incr?
            } else if(flow != null) {
                throw new UnsupportedOperationException("Invalid control flow statement in for(...) expression.");
            }
            incr.evaluate(stack);
        }
        return stack.session().complex().addAll(result);
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        decl.evaluate(stack);
        while(cond.evaluate(stack).asBool().toBool()) {
            Flow flow = expr.execute(stack);
            if(flow == Flow.Yield) {
                // result.add(stack.getResult()); // TODO really allow 'yield' here?
            } else if(flow == Flow.Break) {
                break;
            } else if(flow == Flow.Continue) {
                continue; // TODO also execute incr?
            } else if(flow != null) {
                return flow;
            }
            incr.evaluate(stack);
        }
        return null;
    }
}
