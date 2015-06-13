package styx.core.intrinsics;

import java.util.ArrayList;
import java.util.List;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Function;
import styx.Pair;
import styx.Session;
import styx.Value;
import styx.core.expressions.CompiledFunction;
import styx.core.expressions.FuncRegistry;
import styx.core.expressions.Stack;

public class CollectionIntrinsics {

    public static Complex buildEnvironment(FuncRegistry registry, Session session) throws StyxException {
        return session.complex()
                .put(session.text("filter"), new CompiledFunction(registry, "collection_filter", Determinism.PURE, 2) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return filter(stack.session(), stack.getFrameValue(0).asComplex(), stack.getFrameValue(1).asFunction());
                    }
                }.function())
                .put(session.text("filter_vals"), new CompiledFunction(registry, "collection_filter_vals", Determinism.PURE, 2) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return filter_vals(stack.session(), stack.getFrameValue(0).asComplex(), stack.getFrameValue(1).asFunction());
                    }
                }.function())
                .put(session.text("map"), new CompiledFunction(registry, "collection_map", Determinism.PURE, 2) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return map(stack.session(), stack.getFrameValue(0).asComplex(), stack.getFrameValue(1).asFunction());
                    }
                }.function())
                .put(session.text("map_vals"), new CompiledFunction(registry, "collection_map_vals", Determinism.PURE, 2) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return map_vals(stack.session(), stack.getFrameValue(0).asComplex(), stack.getFrameValue(1).asFunction());
                    }
                }.function())
                .put(session.text("reduce"), new CompiledFunction(registry, "collection_reduce", Determinism.PURE, 2) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return reduce(stack.session(), stack.getFrameValue(0).asComplex(), stack.getFrameValue(1).asFunction());
                    }
                }.function());
    }

    public static Complex filter(Session session, Complex complex, Function predicate) throws StyxException {
        List<Pair<Value, Value>> result = new ArrayList<Pair<Value,Value>>();
        for(Pair<Value, Value> entry : complex) {
            if(predicate.invoke(session, new Value[] { entry.key(), entry.val() }).asBool().toBool()) {
                result.add(entry);
            }
        }
        return session.complex().putAll(result);
    }

    public static Complex filter_vals(Session session, Complex complex, Function predicate) throws StyxException {
        List<Value> result = new ArrayList<Value>();
        for(Pair<Value, Value> entry : complex) {
            if(predicate.invoke(session, new Value[] { entry.key(), entry.val() }).asBool().toBool()) {
                result.add(entry.val());
            }
        }
        return session.complex().addAll(result);
    }

    public static Complex map(Session session, Complex complex, Function mapper) throws StyxException {
        List<Pair<Value, Value>> result = new ArrayList<Pair<Value,Value>>();
        for(Pair<Value, Value> entry : complex) {
            Value mapped = mapper.invoke(session, new Value[] { entry.key(), entry.val() });
            if(mapped == null) {
                throw new StyxException("map(): The provided mapper function did not return a value.");
            }
            result.add(new Pair<Value, Value>(entry.key(), mapped));
        }
        return session.complex().putAll(result);
    }

    public static Complex map_vals(Session session, Complex complex, Function mapper) throws StyxException {
        List<Value> result = new ArrayList<Value>();
        for(Pair<Value, Value> entry : complex) {
            Value mapped = mapper.invoke(session, new Value[] { entry.key(), entry.val() });
            if(mapped == null) {
                throw new StyxException("map_vals(): The provided mapper function did not return a value.");
            }
            result.add(mapped);
        }
        return session.complex().addAll(result);
    }

    public static Value reduce(Session session, Complex complex, Function accumulator) throws StyxException {
        Value result = null;
        for(Pair<Value, Value> entry : complex) {
            if(result == null) {
                result = entry.val();
            } else {
                result = accumulator.invoke(session, new Value[] { result, entry.val() });
            }
            if(result == null) {
                throw new StyxException("reduce(): The provided accumulator function did not return a value.");
            }
        }
        return result;
    }
}
