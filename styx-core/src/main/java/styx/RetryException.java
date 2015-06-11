package styx;

@SuppressWarnings("serial")
public class RetryException extends StyxException
{
    public RetryException(String message) {
        super(message);
    }
}
