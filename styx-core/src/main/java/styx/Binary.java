package styx;

/**
 * The public interface of binary values.
 *
 * A binary value is a byte string, i.e. a sequence of zero or more bytes.
 */
public interface Binary extends Text {

    public int byteCount();

    public byte byteAt(int pos);

    public byte[] toByteArray();

    public String toHexString();
}
