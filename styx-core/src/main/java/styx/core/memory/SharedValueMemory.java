package styx.core.memory;

import styx.Session;
import styx.Value;

/**
 * Implementation of a shared value in memory.
 */
public final class SharedValueMemory implements SharedValue {

    /**
     * The actual shared value, always an array with a single element.
     * The shared value may be null and multiple instances may refer to the same array.
     */
    private final Value[] state;

    /**
     * The last value read or written, used to detect changes in testset() and monitor().
     */
    private Value base;

    /**
     * Constructs a new instance with a new shared value.
     * @param value the initial value of the new shared value.
     */
    public SharedValueMemory(Value value) {
        this.state = new Value[] { value };
    }

    /**
     * Constructs a new instance that references an existing shared value.
     * @param state the shared value to reference, must not be null.
     */
    private SharedValueMemory(Value[] state) {
        this.state = state;
    }

    @Override
    public SharedValue clone() {
        return new SharedValueMemory(state);
    }

    @Override
    public Value get(Session session) {
        synchronized(state) {
            base = state[0];
            return base;
        }
    }

    @Override
    public void set(Session session, Value value) {
        synchronized(state) {
            base = value;
            state[0] = value;
            state.notifyAll();
        }
    }

    @Override
    public boolean testset(Session session, Value value) {
        synchronized(state) {
            if(state[0] != base) {
                return false;
            }
            base = value;
            state[0] = value;
            state.notifyAll();
            return true;
        }
    }

    @Override
    public void monitor(Session session) {
        synchronized(state) {
            while(state[0] == base) {
                try {
                    state.wait(10000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
