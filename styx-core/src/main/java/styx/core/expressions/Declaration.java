package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements variable or constant declaration statement of the form "... := ...".
 */
public final class Declaration extends Expression {

    private final Operator              op;
    private final IdentifierDeclaration ident;
    private final Variable              var; // null if not yet compiled
    private final Expression            expr;

    public Declaration(Operator op, IdentifierDeclaration ident, Expression expr) {
        this(op, ident, null, expr);
    }

    private Declaration(Operator op, IdentifierDeclaration ident, Variable var, Expression expr) {
        this.op    = op;
        this.ident = ident;
        this.var   = var;
        this.expr  = expr;
    }

    public Declaration(ExprFactory expf, String op, Complex value) throws StyxException {
        this.op    = Operator.valueOf  (op);
        this.ident = expf.newIdentDecl (findMember(value, "ident", true));
        this.var   = null;
        this.expr  = expf.newExpression(findMember(value, "expr",  true));
    }

    @Override
    protected Complex toValue() {
        return complex(text(op.toString()),
                complex().
                put(text("ident"), ident).
                put(text("expr"),  expr));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        requireFlag(flags, CompileFlag.AllowDeclaration, "Variable or constant declarations are not allowed here.");
        return op.compile(scope, ident, expr.compile(scope, CompileFlag.EXPRESSION));
    }

    @Override
    public Determinism effects() {
        return expr.effects();
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        op.execute(stack, var, expr);
        return null;
    }

    public static enum Operator {
        DeclareConstant {
            @Override Expression compile(Scope scope, IdentifierDeclaration ident, Expression exprc) throws StyxException {
                if(exprc.effects() != Determinism.CONSTANT) {
                    throw new StyxException("Expression must be compile-time constant.");
                }
                scope.registerConst(ident, exprc);
                return new Declaration(this, ident, exprc); // results in a no-op in execute()
            }
            @Override void execute(Stack stack, Variable var, Expression expr) throws StyxException {
                // no-op, removing from compiled tree would require compile() to return null.
            }
        },
        DeclareImmutableVariable {
            @Override Expression compile(Scope scope, IdentifierDeclaration ident, Expression exprc) throws StyxException {
                return new Declaration(this, ident, scope.registerVariable(ident, false), exprc);
            }
            @Override void execute(Stack stack, Variable var, Expression expr) throws StyxException {
                var.assign(stack, expr.evaluate(stack));
            }
        },
        DeclareMutableVariable {
            @Override Expression compile(Scope scope, IdentifierDeclaration ident, Expression exprc) throws StyxException {
                return new Declaration(this, ident, scope.registerVariable(ident, true), exprc);
            }
            @Override void execute(Stack stack, Variable var, Expression expr) throws StyxException {
                var.assign(stack, expr.evaluate(stack));
            }
        };

        abstract Expression compile(Scope scope, IdentifierDeclaration ident, Expression exprc) throws StyxException;
        abstract void execute(Stack stack, Variable var, Expression expr) throws StyxException;
    }
}
