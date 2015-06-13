package styx.core.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Value;

/**
 * Node of the abstract syntax tree that implements a block of statements of the form "{stmt, stmt}".
 */
public final class Block extends Expression {

    public static final String TAG = Block.class.getSimpleName();

    private final List<Expression> children;

    public Block(List<Expression> children) {
        this.children = children != null ? children : new ArrayList<Expression>();
    }

    public Block(ExprFactory expf, Complex value) throws StyxException {
        this.children = expf.newExpressions(value);
    }

    @Override
    protected Complex toValue() {
        return complex(text(TAG), convToComplex(children));
    }

    @Override
    public Expression compile(Scope scope, EnumSet<CompileFlag> flags) throws StyxException {
        // A block may always contain throw, declarations and assignments.
        // When used as an expression, block may also contain yield.
        if(flags.equals(CompileFlag.EXPRESSION)) {
            flags = CompileFlag.add(flags, CompileFlag.AllowYield);
            flags = CompileFlag.add(flags, CompileFlag.AllowThrow, CompileFlag.AllowDeclaration, CompileFlag.AllowAssignment);
        } else {
            flags = CompileFlag.add(flags, CompileFlag.AllowThrow, CompileFlag.AllowDeclaration, CompileFlag.AllowAssignment);
        }
        List<Expression> childrenc = new ArrayList<>();
        int base = scope.enterBlock();
        for(Expression child : children) {
            childrenc.add(child.compile(scope, flags));
        }
        scope.leaveBlock(base);
        return new Block(childrenc).optimizeConst(scope);
    }

    @Override
    public Determinism effects() {
        return maxEffects(children);
    }

    @Override
    public Value evaluate(Stack stack) throws StyxException {
        for(Expression child : children) {
            Flow flow = child.execute(stack);
            if(flow == Flow.Return || flow == Flow.Yield) {
                return stack.getResult(); // the block is left before the end using a 'return' or 'yield'. The result is on the stack.
            } else if(flow != null) {
                throw new UnsupportedOperationException("Invalid control flow statement in {...} expression.");
            }
        }
        return null; // the block is left at the end without a result.
    }

    @Override
    public Flow execute(Stack stack) throws StyxException {
        for(Expression child : children) {
            Flow flow = child.execute(stack);
            if(flow != null) {
                return flow; // the block is left before the end using a control flow statement.
            }
        }
        return null; // the block is left at the end.
    }
}
