package styx.core.expressions;

import java.util.HashMap;
import java.util.Map;

import styx.StyxException;

/**
 * Manages a of set of built-in or native functions that are accessible by a unique, well known name.
 */
public final class FuncRegistry {

    private final Map<String, CompiledFunction> instances = new HashMap<>();

    public void register(String name, CompiledFunction func) throws StyxException {
        if(instances.containsKey(name)) {
            throw new StyxException("Already registiered compiled function '" + name + "'.");
        }
        instances.put(name, func);
    }

    public CompiledFunction lookup(String name) throws StyxException {
        CompiledFunction inst = instances.get(name);
        if(inst == null) {
            throw new StyxException("Unknown compiled function '" + name + "'.");
        }
        return inst;
    }
}
