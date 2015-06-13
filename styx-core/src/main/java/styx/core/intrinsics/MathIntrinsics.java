package styx.core.intrinsics;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Session;
import styx.Value;
import styx.core.expressions.CompiledFunction;
import styx.core.expressions.FuncRegistry;
import styx.core.expressions.Stack;

public class MathIntrinsics {

    public static Complex buildEnvironment(FuncRegistry registry, Session session) throws StyxException {
        return session.complex()
                .put(session.text("E"),  session.number(Math.E))
                .put(session.text("PI"), session.number(Math.PI))
                .put(session.text("pow"), new CompiledFunction(registry, "math_pow", Determinism.PURE, 2) {
                    @Override
                    public Value invoke(Stack stack) {
                        return stack.session().number(Math.pow(
                            stack.getFrameValue(0).asNumber().toDouble(),
                            stack.getFrameValue(1).asNumber().toDouble()));
                    }
                }.function())
                .put(session.text("rand"), new CompiledFunction(registry, "math_rand", Determinism.NON_DETERMINISTIC, 0) {
                    @Override
                    public Value invoke(Stack stack) {
                        return stack.session().number(Math.random());
                    }
                }.function());
    }
}
