package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements binary operator such as "&&", "||", etc.
 */
public final class BinaryOperator extends Expression {

    private final Operator   op;
    private final Expression expr1;
    private final Expression expr2;

    public BinaryOperator(Operator op, Expression expr1, Expression expr2) {
        this.op    = op;
        this.expr1 = expr1;
        this.expr2 = expr2;
    }

    public BinaryOperator(ExprFactory expf, String op, Complex value) throws StyxException {
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
        return new BinaryOperator(op, expr1.compile(scope, CompileFlag.EXPRESSION), expr2.compile(scope, CompileFlag.EXPRESSION)).optimizeConst(scope);
    }

    @Override
    public Determinism effects() {
        return maxEffects(expr1, expr2);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        return op.evaluate(stack, expr1, expr2);
    }

    public static enum Operator {
        // evaluated last
        Coal {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                Value val1 = expr1.evaluate(stack);
                return val1 != null ? val1 : expr2.evaluate(stack);
            }
        },
        // evaluated after And
        Or {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().bool(
                        expr1.evaluate(stack).asBool().toBool() ||
                        expr2.evaluate(stack).asBool().toBool());
            }
        },
        // evaluated before Or
        And {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().bool(
                        expr1.evaluate(stack).asBool().toBool() &&
                        expr2.evaluate(stack).asBool().toBool());
            }
        },
        // own category
        BitOr {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toLong() |
                        expr2.evaluate(stack).asNumber().toLong());
            }
        },
        // own category
        BitXor {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toLong() ^
                        expr2.evaluate(stack).asNumber().toLong());
            }
        },
        // own category
        BitAnd {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toLong() &
                        expr2.evaluate(stack).asNumber().toLong());
            }
        },
        // comparison operators
        Compare {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(compare(expr1.evaluate(stack), expr2.evaluate(stack)));
            }
        },
        Equal {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().bool(compare(expr1.evaluate(stack), expr2.evaluate(stack)) == 0);
            }
        },
        NotEqual {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().bool(compare(expr1.evaluate(stack), expr2.evaluate(stack)) != 0);
            }
        },
        // relational operators
        LessOrEqual {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().bool(compare(expr1.evaluate(stack), expr2.evaluate(stack)) <= 0);
            }
        },
        Less {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().bool(compare(expr1.evaluate(stack), expr2.evaluate(stack)) < 0);
            }
        },
        GreaterOrEqual {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().bool(compare(expr1.evaluate(stack), expr2.evaluate(stack)) >= 0);
            }
        },
        Greater {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().bool(compare(expr1.evaluate(stack), expr2.evaluate(stack)) > 0);
            }
        },
        // evaluated after arithmethic computations
        Concat {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().text(
                        expr1.evaluate(stack).asText().toTextString() +
                        expr2.evaluate(stack).asText().toTextString());
            }
        },
        // evaluated after Add, Sub
        Shl {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toLong() <<
                        expr2.evaluate(stack).asNumber().toLong());
            }
        },
        // own category
        Shr {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toLong() >>
                        expr2.evaluate(stack).asNumber().toLong());
            }
        },
        // evaluated after Mul, Div, Mod
        Add {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toDouble() +
                        expr2.evaluate(stack).asNumber().toDouble());
            }
        },
        Sub {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toDouble() -
                        expr2.evaluate(stack).asNumber().toDouble());
            }
        },
        // evaluated before Add or Sub
        Mul {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toDouble() *
                        expr2.evaluate(stack).asNumber().toDouble());
            }
        },
        Div {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toDouble() /
                        expr2.evaluate(stack).asNumber().toDouble());
            }
        },
        Mod {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(
                        expr1.evaluate(stack).asNumber().toDouble() %
                        expr2.evaluate(stack).asNumber().toDouble());
            }
        },
        // evaluated before Mul, Div, Mod
        Pow {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                return stack.session().number(Math.pow(
                        expr1.evaluate(stack).asNumber().toDouble(),
                        expr2.evaluate(stack).asNumber().toDouble()));
            }
        },
        // evaluated first
        Child {
            @Override Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException {
                Value val1 = expr1.evaluate(stack);
                Value val2 = expr2.evaluate(stack);
                if(val1.isReference()) {
                    return val1.asReference().child(val2);
                }
                if(val1.isComplex()) {
                    return val1.asComplex().get(val2);
                }
                throw new StyxException("The value is not a reference or a complex value.");
            }
        };

        abstract Value evaluate(Stack stack, Expression expr1, Expression expr2) throws StyxException;
    }
}
