package styx;

@SuppressWarnings("serial")
public class StyxRuntimeException extends RuntimeException {

    public StyxRuntimeException() { }

    public StyxRuntimeException(String message) {
        super(message);
    }

    public StyxRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
