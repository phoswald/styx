package styx.core.parser;

import java.util.ArrayList;
import java.util.List;

import styx.Complex;
import styx.StyxException;
import styx.Function;
import styx.Session;
import styx.core.EvalProvider;
import styx.core.expressions.Batch;
import styx.core.expressions.Expression;
import styx.core.expressions.FunctionExpression;
import styx.core.expressions.Scope;

public final class Interpreter implements EvalProvider {

    @Override
    public Function parse(Session session, Complex environment, String script, boolean compile) throws StyxException {
        if(script == null) {
            return null;
        }
        List<Expression> stmts = parse(session, script);
        if(stmts == null) {
            return null;
        }
        FunctionExpression function = new FunctionExpression(false, null, new Batch(stmts));
        if(compile) {
            function = function.compile(new Scope(session, environment), null);
        }
        return function.function();
    }

    private List<Expression> parse(Session session, String script) throws StyxException {
        Parser.Ref<ArrayList<Expression>> result = new Parser.Ref<>(null);
        Parser.Ref_int                    error  = new Parser.Ref_int(0);
        Parser                            parser = new Parser();
        parser.session = session;
        if(!parser.Parse_ROOT_STATEMENT_LIST(script, result, error)) {
            String next = script.length()-error.val <= 50 ?
                          script.substring(error.val, script.length()) :
                          script.substring(error.val, error.val + 50) + "...";
            throw new StyxException("Offset " + error.val+1 + ": Cannot handle '" + next + "'.");
        }
        return result.val;
    }
}
