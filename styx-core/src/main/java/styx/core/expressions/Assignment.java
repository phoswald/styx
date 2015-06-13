package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements an assignment statement such as "=", "+=", etc.
 */
public final class Assignment extends Expression {

    private final Operator   op;
    private final Expression expr1;
    private final Expression expr2;

    public Assignment(Operator op, Expression expr1, Expression expr2) {
        this.op    = op;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public Assignment(ExprFactory expf, String op, Complex value) throws StyxException {
        this.op    = Operator.valueOf  (op);
        this.expr1 = expf.newExpression(findMember(value, "expr1", true));
        this.expr2 = expf.newExpression(findMember(value, "expr2", true));
    }

    @Override
    protected Complex toValue() {
        return complex(text(op.toString()),
                complex().
                put(text("expr1"), expr1).
                put(text("expr2"), expr2));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        requireFlag(flags, CompileFlag.AllowAssignment, "Variable or reference assignments are not allowed here.");
        return new Assignment(op, cast(expr1.compile(scope, CompileFlag.EXPRESSION)).assignCheck(), expr2.compile(scope, CompileFlag.EXPRESSION));
    }

    @Override
    public Determinism effects() {
        return maxEffects(expr1, expr2);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        op.execute(stack, cast(expr1), expr2);
        return null;
    }

    private static AssignableExpression cast(Expression expr) throws StyxException {
        if(expr instanceof AssignableExpression) {
           return (AssignableExpression) expr;
        } else {
            throw new StyxException("The left expression is not assignable (" + expr.getClass().getSimpleName() + ").");
        }
    }

    public static enum Operator {
        Assign {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, expr2.evaluate(stack));
            }
        },
        AssignCoal {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Coal.evaluate(stack, expr1, expr2));
            }
        },
        AssignOr {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Or.evaluate(stack, expr1, expr2));
            }
        },
        AssignAnd {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.And.evaluate(stack, expr1, expr2));
            }
        },
        AssignBitOr {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.BitOr.evaluate(stack, expr1, expr2));
            }
        },
        AssignBitXor {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.BitXor.evaluate(stack, expr1, expr2));
            }
        },
        AssignBitAnd {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.BitAnd.evaluate(stack, expr1, expr2));
            }
        },
        AssignConcat {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Concat.evaluate(stack, expr1, expr2));
            }
        },
        AssignShl {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Shl.evaluate(stack, expr1, expr2));
            }
        },
        AssignShr {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Shr.evaluate(stack, expr1, expr2));
            }
        },
        AssignAdd {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Add.evaluate(stack, expr1, expr2));
            }
        },
        AssignSub {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Sub.evaluate(stack, expr1, expr2));
           }
        },
        AssignMul {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Mul.evaluate(stack, expr1, expr2));
            }
        },
        AssignDiv {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Div.evaluate(stack, expr1, expr2));
            }
        },
        AssignMod {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Mod.evaluate(stack, expr1, expr2));
            }
        },
        AssignPow {
            @Override void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException {
                expr1.assign(stack, BinaryOperator.Operator.Pow.evaluate(stack, expr1, expr2));
            }
        };

        abstract void execute(Stack stack, AssignableExpression expr1, Expression expr2) throws StyxException;
    }
}
