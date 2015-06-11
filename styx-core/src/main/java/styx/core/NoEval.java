package styx.core;

import styx.Complex;
import styx.Function;
import styx.Session;

public final class NoEval implements EvalProvider {

    @Override
    public Function parse(Session session, Complex environment, String script, boolean compile) {
        throw new UnsupportedOperationException("This session does not support the evaluation of scripts.");
    }
}
