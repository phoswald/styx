package styx;

@SuppressWarnings("serial")
public class ConcurrentException extends StyxException {

    public ConcurrentException(String message) {
        super(message);
    }

    public ConcurrentException(String message, Throwable cause) {
        super(message, cause);
    }
}
