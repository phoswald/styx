package styx.core.expressions;

/**
 * Enumeration of return values of Expression.execute().
 * <p>
 * Possible values are all control flow statements except those implemented as Java exceptions.
 */
public enum Flow {
    Return,
    Yield,
    Break,
    Continue
}
