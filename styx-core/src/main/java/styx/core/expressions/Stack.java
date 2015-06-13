package styx.core.expressions;

import java.util.Arrays;

import styx.Session;
import styx.Value;

public class Stack {

    private final Session session;
    private Value[]       values = new Value[1];
    private int              size   = 0;
    private int              frame  = 0;
    private Value         result = null;

    public Stack(Session session) {
        this.session = session;
    }

    public Session session() {
        return session;
    }

    public void push(Value val) {
        if(size == values.length) {
            values = Arrays.copyOf(values, values.length << 1);
        }
        values[size++] = val;
    }

    public int prepareFrame() {
        push(session.number(frame));
        return size;
    }

    public void enterFrame(int base) {
        frame = base;
    }

    public void leaveFrame(int base) {
        size  = base - 1;
        frame = values[size].asNumber().toInteger();
    }

    public Value[] getFrameAsArray() {
        return Arrays.copyOfRange(values, frame, size);
    }

    public int getFrameBase() {
        return frame;
    }

    public int getFrameSize() {
        return size - frame;
    }

    public void setFrame(int base, int size) {
        this.frame = base;
        this.size  = base + size;
    }

    public Value getFrameValue(int offset) {
        return values[frame + offset];
    }

    public void setFrameValue(int offset, Value val) {
        while(frame + offset >= values.length) {
            push(null);
        }
        if(frame + offset >= size) {
            size = frame + offset + 1;
        }
        values[frame + offset] = val;
    }

    public void setResult(Value result) {
        this.result = result;
    }

    public Value getResult() {
        return result;
    }
}
