package styx;

/**
 * The public interface of textual values.
 *
 * A text is a unicode string, i.e. a sequence of zero or more unicode characters.
 */
public interface Text extends Value {

    // The following methods cannot be implemented efficiently if the textual value
    // is not represented as a UTF-16 string or character array.
    // This is especially the case for numbers.

//    public int charCount();
//
//    public char charAt(int pos);

    public char[] toCharArray();

    public String toTextString();
}
