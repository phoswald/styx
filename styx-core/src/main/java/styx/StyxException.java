package styx;

@SuppressWarnings("serial")
public class StyxException extends Exception {

    private final Value value;

    public StyxException() {
        this.value = null;
    }

    public StyxException(String message) {
        super(message);
        this.value = null;
    }

    public StyxException(String message, Throwable cause) {
        super(message, cause);
        this.value = null;
    }

    public StyxException(String message, Value value) {
        super(message);
        this.value = value;
    }

    public Value getValue() {
        return value;
    }
}
