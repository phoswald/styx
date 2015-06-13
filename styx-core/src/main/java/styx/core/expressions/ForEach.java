package styx.core.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.Pair;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a "foreach(... in ...) ..." statement.
 */
public final class ForEach extends Expression {

    public static final String TAG = ForEach.class.getSimpleName();

    private final IdentifierDeclaration ident1;
    private final IdentifierDeclaration ident2; // optional
    private final Variable              var1; // null if not yet compiled
    private final Variable              var2; // null if not yet compiled or ident2 is omitted
    private final Expression            init;
    private final Expression            expr;

    public ForEach(IdentifierDeclaration ident1, IdentifierDeclaration ident2, Expression init, Expression expr) {
        this.ident1 = ident1;
        this.ident2 = ident2;
        this.var1   = null;
        this.var2   = null;
        this.init   = init;
        this.expr   = expr;
    }

    private ForEach(IdentifierDeclaration ident1, IdentifierDeclaration ident2, Variable var1, Variable var2, Expression init, Expression expr) {
        this.ident1 = ident1;
        this.ident2 = ident2;
        this.var1   = var1;
        this.var2   = var2;
        this.init   = init;
        this.expr   = expr;
    }

    public ForEach(ExprFactory expf, Complex value) throws StyxException {
        this.ident1 = expf.newIdentDecl (findMember(value, "ident1", true));
        this.ident2 = expf.newIdentDecl (findMember(value, "ident2", false));
        this.var1   = null;
        this.var2   = null;
        this.init   = expf.newExpression(findMember(value, "init",  true));
        this.expr   = expf.newExpression(findMember(value, "expr",  true));
    }

    @Override
    protected Complex toValue() {
        return complex(text(ForEach.class.getSimpleName()),
                complex().
                put(text("ident1"), ident1).
                put(text("ident2"), ident2).
                put(text("init"),   init).
                put(text("expr"),   expr));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        Expression exprc = init.compile(scope, CompileFlag.EXPRESSION);
        int base = scope.enterBlock();
        Variable var1 = scope.registerVariable(ident1, true);
        Variable var2 = ident2 != null ? scope.registerVariable(ident2, true) : null;
        ForEach result = new ForEach(
                ident1, ident2, var1, var2,
                exprc,
                expr.compile(scope, CompileFlag.add(flags, CompileFlag.AllowYield, CompileFlag.AllowBreak, CompileFlag.AllowContinue)));
        scope.leaveBlock(base);
        return result;
    }

    @Override
    public Determinism effects() {
        return maxEffects(init, expr);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        List<Value> result = new ArrayList<Value>();
        Value coll = init.evaluate(stack);
        if(coll != null) {
            for(Pair<Value, Value> pair : coll.asComplex()) {
                if(ident2 != null) {
                    var1.assign(stack, pair.key());
                    var2.assign(stack, pair.val());
                } else {
                    var1.assign(stack, pair.val());
                }
                Flow flow = expr.execute(stack);
                if(flow == Flow.Yield) {
                    result.add(stack.getResult());
                } else if(flow == Flow.Break) {
                    break;
                } else if(flow == Flow.Continue) {
                    continue;
                } else if(flow != null) {
                    throw new UnsupportedOperationException("Invalid control flow statement in foreach(...) expression.");
                }
            }
        }
        return stack.session().complex().addAll(result);
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        Value coll = init.evaluate(stack);
        if(coll != null) {
            for(Pair<Value, Value> pair : coll.asComplex()) {
                if(ident2 != null) {
                    var1.assign(stack, pair.key());
                    var2.assign(stack, pair.val());
                } else {
                    var1.assign(stack, pair.val());
                }
                Flow flow = expr.execute(stack);
                if(flow == Flow.Yield) {
                    // result.add(stack.getResult()); // TODO really allow 'yield' here?
                } else if(flow == Flow.Break) {
                    break;
                } else if(flow == Flow.Continue) {
                    continue;
                } else if(flow != null) {
                    return flow;
                }
            }
        }
        return null;
    }
}
