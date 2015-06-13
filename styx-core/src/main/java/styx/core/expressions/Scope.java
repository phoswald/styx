package styx.core.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Session;
import styx.Text;
import styx.Value;

public class Scope {

    private static final Set<String> RESERVED = new java.util.HashSet<String>(Arrays.asList(
            "return", "if", "else", "switch", "case", "default",
            "loop", "while", "do", "for", "foreach", "in", "break", "continue",
            "try", "catch", "finally", "throw", "atomic", "retry"));

    private final Session session;
    private final Complex environment;
    private final Scope      parent;
    private List<Symbol>     symbols = new ArrayList<>();

    public Scope(Session session, Complex environment) {
        this.session     = session;
        this.environment = environment;
        this.parent      = null;

        symbols.add(new Symbol(session.text("null"),  new Constant(null)));
        symbols.add(new Symbol(session.text("void"),  new Constant(session.empty())));
        symbols.add(new Symbol(session.text("false"), new Constant(session.bool(false))));
        symbols.add(new Symbol(session.text("true"),  new Constant(session.bool(true))));
    }

    public Scope(Scope parent) {
        this.session     = parent.session;
        this.environment = null;
        this.parent      = parent;
    }

    public Session getSession() {
        return session;
    }

    public int enterBlock() {
        return symbols.size();
    }

    public void leaveBlock(int base) {
        while(symbols.size() > base) {
            symbols.remove(symbols.size() - 1);
        }
    }

    public void registerConst(IdentifierDeclaration ident, Expression expr) throws StyxException {
        if(expr.effects() != Determinism.CONSTANT) {
            throw new IllegalArgumentException();
        }
        Text name = checkReserved(ident.name());
        if(findSymbol(name) != null) {
            throw new StyxException("The symbol '"+name.toTextString()+"' is already defined.");
        }
        symbols.add(new Symbol(name, expr));
    }

    public Variable registerVariable(IdentifierDeclaration ident, boolean mutable) throws StyxException {
        Text name = checkReserved(ident.name());
        if(findSymbol(name) != null) {
            throw new StyxException("The symbol '"+name.toTextString()+"' is already defined.");
        }
        Variable var = new Variable(name, mutable, symbols.size());
        symbols.add(new Symbol(name, var));
        return var;
    }

    public Expression lookupSymbol(Text name) throws StyxException {
        Expression expr = findSymbol(checkReserved(name));
        if(expr == null) {
            throw new StyxException("The symbol '"+name.toTextString()+"' is not defined.");
        }
        return expr;
    }

    private Expression findSymbol(Text name) {
        for(Symbol symbol : symbols) {
            if(symbol.name.equals(name)) {
                return symbol.expr;
            }
        }
        if(parent != null) {
            Expression expr = parent.findSymbol(name);
            if(expr != null && expr.effects() == Determinism.CONSTANT) {
                return expr; // inherited symbols only visible if compile-time constant.
            }
        }
        if(environment != null) {
            Value val = environment.get(name);
            if(val != null) {
                return new Constant(val);
            }
        }
        return null;
    }

    private static Text checkReserved(Text name) throws StyxException {
        if(RESERVED.contains(name.toTextString())) {
            throw new StyxException("The reserved word '"+name.toTextString()+"' cannot be used as a symbol.");
        }
        return name;
    }

    private static final class Symbol {
        public final Text    name;
        public final Expression expr;

        public Symbol(Text name, Expression expr) {
            this.name = name;
            this.expr = expr;
        }
    }
}
