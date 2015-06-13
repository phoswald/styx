package styx.core.expressions;

import styx.Complex;
import styx.StyxException;
import styx.Text;
import styx.core.values.CompiledComplex;

public final class IdentifierDeclaration extends CompiledComplex {

    private final Text    name;
    private final Expression type;

    public IdentifierDeclaration(Text name, Expression type) {
        this.name = name;
        this.type = type;
    }

    public IdentifierDeclaration(ExprFactory expf, Complex value) throws StyxException {
        this.name =                    findMember(value, "name", true).asText();
        this.type = expf.newExpression(findMember(value, "type", false));
    }

    public Text name() {
        return name;
    }

    public Expression type() {
        return type;
    }

    @Override
    protected Complex toValue() {
        return complex().
                put(text("name"), name).
                put(text("type"), type);
    }
}
