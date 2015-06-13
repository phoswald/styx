package styx.core.expressions;

import java.util.Arrays;
import java.util.EnumSet;

public enum CompileFlag {

    /**
     * The 'return' statement is allowed.
     */
    AllowReturn,

    /**
     * The 'return' statement is allowed.
     */
    AllowYield,

    /**
     * The 'break' statement is allowed.
     */
    AllowBreak,

    /**
     * The 'continue' statement is allowed.
     */
    AllowContinue,

    /**
     * The 'throw' or 'retry' statement is allowed.
     */
    AllowThrow,

    /**
     * Variable and constant declarations are allowed.
     */
    AllowDeclaration,

    /**
     * Variable or reference assignments are allowed.
     */
    AllowAssignment;

    /**
     * The flags to be used for compiling top level batches: everything but 'return', 'yield', 'break', 'continue' is allowed.
     */
    public static final EnumSet<CompileFlag> BATCH = EnumSet.of(AllowThrow, AllowDeclaration, AllowAssignment);

    /**
     * The flags to be used for compiling function bodies (not lambdas) : 'return' is allowed.
     */
    public static final EnumSet<CompileFlag> BODY = EnumSet.of(AllowReturn);

    /**
     * The flags to be used for compiling expressions (including lambdas): no statements are allowed.
     */
    public static final EnumSet<CompileFlag> EXPRESSION = EnumSet.noneOf(CompileFlag.class);

    public static EnumSet<CompileFlag> add(EnumSet<CompileFlag> a, CompileFlag... b) {
        EnumSet<CompileFlag> result = EnumSet.copyOf(a);
        result.addAll(Arrays.asList(b));
        return result;
    }

    public static EnumSet<CompileFlag> add(EnumSet<CompileFlag> a, EnumSet<CompileFlag> b) {
        EnumSet<CompileFlag> result = EnumSet.copyOf(a);
        result.addAll(b);
        return result;
    }
}
