package styx.core.expressions;

import java.util.EnumSet;

import styx.Complex;
import styx.Determinism;
import styx.RetryException;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a "return ...", "yield ...", "break", "continue", "throw ..." or "retry" statement.
 */
public final class FlowStatement extends Expression {

    private final Keyword    keyw;
    private final Expression expr; // can be null

    public FlowStatement(Keyword keyw, Expression expr) {
        this.keyw = keyw;
        this.expr = expr;
    }

    public FlowStatement(ExprFactory expf, String keyw, Value value) throws StyxException {
        this.keyw = Keyword.valueOf   (keyw);
        this.expr = expf.newExpression(value);
    }

    @Override
    protected Complex toValue() {
        return complex(text(keyw.toString()), expr != null ? expr : complex());
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        keyw.checkFlags(flags);
        return new FlowStatement(keyw, expr != null ? expr.compile(scope, CompileFlag.EXPRESSION) : null);
    }

    @Override
    public Determinism effects() {
        return maxEffects(Determinism.PURE, expr); // We have to prevent optimization of return, break and continue
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        throw new UnsupportedOperationException("Control flow statements cannot be used as expressions.");
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        return keyw.execute(stack, expr);
    }

    public static enum Keyword {
        Return {
            @Override void checkFlags(EnumSet<CompileFlag> flags) throws StyxException {
                requireFlag(flags, CompileFlag.AllowReturn, "The 'return' control flow statement is not allowed here.");
            }
            @Override Flow execute(Stack stack, Expression expr) throws StyxException {
                stack.setResult(expr == null ? null : expr.evaluate(stack));
                return Flow.Return;
            }
        },
        Yield {
            @Override void checkFlags(EnumSet<CompileFlag> flags) throws StyxException {
                requireFlag(flags, CompileFlag.AllowYield, "The 'yield' control flow statement is not allowed here.");
            }
            @Override Flow execute(Stack stack, Expression expr) throws StyxException {
                stack.setResult(expr.evaluate(stack));
                return Flow.Yield;
            }
        },
        Break {
            @Override void checkFlags(EnumSet<CompileFlag> flags) throws StyxException {
                requireFlag(flags, CompileFlag.AllowBreak, "The 'break' control flow statement is not allowed here.");
            }
            @Override Flow execute(Stack stack, Expression expr) throws StyxException {
                return Flow.Break;
            }
        },
        Continue {
            @Override void checkFlags(EnumSet<CompileFlag> flags) throws StyxException {
                requireFlag(flags, CompileFlag.AllowContinue, "The 'continue' control flow statement is not allowed here.");
            }
            @Override Flow execute(Stack stack, Expression expr) throws StyxException {
                return Flow.Continue;
            }
        },
        Throw {
            @Override void checkFlags(EnumSet<CompileFlag> flags) throws StyxException {
                requireFlag(flags, CompileFlag.AllowThrow, "The 'throw' control flow statement is not allowed here.");
            }
            @Override Flow execute(Stack stack, Expression expr) throws StyxException {
                throw new StyxException("The script invoked a 'throw' statement.", expr.evaluate(stack));
            }
        },
        Retry {
            @Override void checkFlags(EnumSet<CompileFlag> flags) throws StyxException {
                requireFlag(flags, CompileFlag.AllowThrow, "The 'retry' control flow statement is not allowed here.");
            }
            @Override Flow execute(Stack stack, Expression expr) throws StyxException {
                throw new RetryException("The script invoked a 'retry' statement.");
            }
        };

        abstract void checkFlags(EnumSet<CompileFlag> flags) throws StyxException;
        abstract Flow execute(Stack stack, Expression expr) throws StyxException;
    }
}
