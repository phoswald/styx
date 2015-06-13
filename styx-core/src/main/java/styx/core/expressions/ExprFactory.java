package styx.core.expressions;

import java.util.ArrayList;
import java.util.List;

import styx.Pair;
import styx.StyxException;
import styx.Value;

/**
 * Used by FuncFactory to create Expression instances from complex values.
 */
public final class ExprFactory {

    private static final List<String> ASSIGN_OPERATORS = toStringArray(Assignment.Operator.values());
    private static final List<String> DECL_OPERATORS   = toStringArray(Declaration.Operator.values());
    private static final List<String> FLOW_KEYWORDS    = toStringArray(FlowStatement.Keyword.values());
    private static final List<String> BINARY_OPERATORS = toStringArray(BinaryOperator.Operator.values());
    private static final List<String> UNARY_OPERATORS  = toStringArray(UnaryOperator.Operator.values());

    private final FuncRegistry registry;

    public ExprFactory(FuncRegistry registry) {
        this.registry = registry;
    }

    // TODO (implement) All resulting expressions should be equal to the value from which they are initialized.
    //                  This can be done easily by passing the original value to the CompiledComplex constructor.

    public Expression newExpression(Value value) throws StyxException {
        try {
            if(value == null || (value.isComplex() && value.asComplex().isEmpty())) {
                return null; // [] is used when no value is needed, for example @Break []
            }
            Pair<Value,Value> pair = value.asComplex().single();
            String key = pair.key().asText().toTextString();
            if(ASSIGN_OPERATORS.contains(key)) {
                return new Assignment(this, key, pair.val().asComplex());
            }
            if(key.equals(Atomic.TAG)) {
                return new Atomic(this, pair.val());
            }
            if(key.equals(Batch.TAG)) {
                return new Batch(this, pair.val().asComplex());
            }
            if(BINARY_OPERATORS.contains(key)) {
                return new BinaryOperator(this, key, pair.val().asComplex());
            }
            if(key.equals(Block.TAG)) {
                return new Block(this, pair.val().asComplex());
            }
            if(key.equals(Call.TAG)) {
                return new Call(this, pair.val().asComplex());
            }
            if(key.equals(ComplexExpression.TAG)) {
                return new ComplexExpression(this, pair.val().asComplex());
            }
            if(key.equals(Constant.TAG1) || key.equals(Constant.TAG2)) {
                return new Constant(key, pair.val());
            }
            if(DECL_OPERATORS.contains(key)) {
                return new Declaration(this, key, pair.val().asComplex());
            }
            if(FLOW_KEYWORDS.contains(key)) {
                return new FlowStatement(this, key, pair.val());
            }
            if(key.equals(For.TAG)) {
                return new For(this, pair.val().asComplex());
            }
            if(key.equals(ForEach.TAG)) {
                return new ForEach(this, pair.val().asComplex());
            }
            if(key.equals(IfElse.TAG)) {
                return new IfElse(this, pair.val().asComplex());
            }
            if(key.equals(ReferenceExpression.TAG)) {
                return new ReferenceExpression(this, pair.val().asComplex());
            }
            if(key.equals(ReferenceContent.TAG)) {
                return new ReferenceContent(this, pair.val());
            }
            if(key.equals(TryCatchFinally.TAG)) {
                return new TryCatchFinally(this, pair.val().asComplex());
            }
            if(UNARY_OPERATORS.contains(key)) {
                return new UnaryOperator(this, key, pair.val());
            }
            if(key.equals(Variable.TAG)) {
                return new Variable(pair.val().asText());
            }
            if(key.equals(While.TAG)) {
                return new While(this, pair.val().asComplex());
            }
        } catch(RuntimeException | StyxException e) {
            throw new StyxException("Cannot decode expression from: " + value + "\n" + e.getMessage(), e);
        }
        return newFunction(value);
    }

    public AbstractFunction newFunction(Value value) throws StyxException {
        try {
            Pair<Value,Value> pair = value.asComplex().single();
            String key = pair.key().asText().toTextString();
            if(key.equals(CompiledFunction.TAG)) {
                return registry.lookup(pair.val().asText().toTextString());
            }
            if(key.equals(FunctionExpression.TAG)) {
                return new FunctionExpression(this, pair.val().asComplex());
            }
            throw new StyxException("Unknown tag '" + key + "'.");
        } catch(RuntimeException | StyxException e) {
            throw new StyxException("Cannot decode function from: " + value + "\n" + e.getMessage(), e);
        }
    }

    public IdentifierDeclaration newIdentDecl(Value value) throws StyxException {
        if(value == null) {
            return null;
        }
        return new IdentifierDeclaration(this, value.asComplex());
    }

    public List<Expression> newStatements(Value value) throws StyxException {
        List<Expression> res = new ArrayList<>();
        for(Pair<Value,Value> child : value.asComplex()) {
            res.add(newExpression(child.val()));
        }
        return res;
    }

    public List<Expression> newExpressions(Value value) throws StyxException {
        List<Expression> res = new ArrayList<>();
        for(Pair<Value,Value> child : value.asComplex()) {
            res.add(newExpression(child.val()));
        }
        return res;
    }

    public List<IdentifierDeclaration> newIdentDecls(Value value) throws StyxException {
        List<IdentifierDeclaration> res = new ArrayList<>();
        for(Pair<Value,Value> child : value.asComplex()) {
            res.add(newIdentDecl(child.val()));
        }
        return res;
    }

    private static <T> List<String> toStringArray(T[] ary) {
        List<String> res = new ArrayList<String>();
        for(T elem : ary) {
            res.add(elem.toString());
        }
        return res;
    }
}
