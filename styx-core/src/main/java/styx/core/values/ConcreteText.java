package styx.core.values;

import styx.Text;

/**
 * A simple implementation of textual values.
 *
 * Instances of this class are created by AbstractText.factory().
 */
final class ConcreteText extends AbstractText {

    public static final Text EMPTY = new ConcreteText("");

    /**
     * The immutable string value, never null.
     */
    private final String val;

    private ConcreteText(String val) {
        this.val = val;
    }

    @Override
    public String toTextString() {
        return val;
    }

    public static Text factory(String val) {
        if(val == null) {
            return EMPTY;
        }
        return new ConcreteText(val);
    }
}
