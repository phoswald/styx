package styx.core.intrinsics;

import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Session;
import styx.Value;
import styx.core.expressions.CompiledFunction;
import styx.core.expressions.FuncRegistry;
import styx.core.expressions.Stack;

public class SessionIntrinsics {

    public static Complex buildEnvironment(FuncRegistry registry, Session session) throws StyxException {
        return session.complex()
                .put(session.text("null"), new CompiledFunction(registry, "session_null", Determinism.PURE, 0) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return null;
                    }
                }.function())
                .put(session.text("browse"), new CompiledFunction(registry, "session_browse", Determinism.QUERY, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        List<Value> result = stack.session().browse(stack.getFrameValue(0).asReference());
                        return result == null ? null : stack.session().complex().addAll(result);
                    }
                }.function())
                .put(session.text("serialize"), new CompiledFunction(registry, "session_serialize", Determinism.PURE, 2) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return stack.session().text(stack.session().serialize(
                            stack.getFrameValue(0),
                            stack.getFrameValue(1).asBool().toBool()));
                    }
                }.function())
                .put(session.text("deserialize"), new CompiledFunction(registry, "session_deserialize", Determinism.PURE, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return stack.session().deserialize(
                            stack.getFrameValue(0).asText().toTextString());
                    }
                }.function())
                .put(session.text("parse"), new CompiledFunction(registry, "session_parse", Determinism.PURE, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return stack.session().parse(
                            stack.getFrameValue(0).asText().toTextString());
                    }
                }.function())
                .put(session.text("evaluate"), new CompiledFunction(registry, "session_evaluate", Determinism.NON_DETERMINISTIC, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return stack.session().evaluate(
                            stack.getFrameValue(0).asText().toTextString());
                    }
                }.function());
    }
}
