package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.Session;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements an unary operator such as "!", "-", etc.
 */
public final class UnaryOperator extends Expression {

    private final Operator   op;
    private final Expression expr;

    public UnaryOperator(Operator op, Expression expr) {
        this.op   = op;
        this.expr = expr;
    }

    public UnaryOperator(ExprFactory expf, String op, Value value) throws StyxException {
        this.op   = Operator.valueOf  (op);
        this.expr = expf.newExpression(value);
    }

    public Expression propagateConst(Session session) {
        if(expr instanceof Constant == false) {
            return this;
        }
        try {
            return op.propagateConst(session, this, Constant.unwrap(expr));
        } catch (RuntimeException | StyxException e) {
            return this; // propagation possible, but evaluation leads to an exception.
        }
    }

    @Override
    protected Complex toValue() {
        return complex(text(op.toString()), expr);
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        return new UnaryOperator(op, expr.compile(scope, CompileFlag.EXPRESSION)).optimizeConst(scope);
    }

    @Override
    public Determinism effects() {
        return expr.effects();
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        return op.evaluate(stack, expr);
    }

    public static enum Operator {
        Not {
            @Override Value evaluate(Stack stack, Expression expr) throws StyxException {
                return stack.session().bool(!expr.evaluate(stack).asBool().toBool());
            }
        },
        Neg {
            @Override Expression propagateConst(Session session, Expression self, Value value) {
                return new Constant(session.number("-" + value.asText().toTextString()));
            }
            @Override Value evaluate(Stack stack, Expression expr) throws StyxException {
                return stack.session().number(-expr.evaluate(stack).asNumber().toLong());
            }
        },
        BitNot {
            @Override Value evaluate(Stack stack, Expression expr) throws StyxException {
                return stack.session().number(~expr.evaluate(stack).asNumber().toLong());
            }
        },
        TypeExpression {
            @Override Expression propagateConst(Session session, Expression self, Value value) throws StyxException {
                return new Constant(session.type(value));
            }
            @Override Value evaluate(Stack stack, Expression expr) throws StyxException {
                return stack.session().type(expr.evaluate(stack));
            }
        },
        FunctionExpression {
            @Override Expression propagateConst(Session session, Expression self, Value value) throws StyxException {
                return new Constant(session.function(value));
            }
            @Override Value evaluate(Stack stack, Expression expr) throws StyxException {
                return stack.session().function(expr.evaluate(stack));
            }
        };

        Expression propagateConst(Session session, Expression self, Value value) throws StyxException {
            return self;
        }

        abstract Value evaluate(Stack stack, Expression expr) throws StyxException;
    }
}
