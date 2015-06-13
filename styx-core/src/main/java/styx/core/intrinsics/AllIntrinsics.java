package styx.core.intrinsics;

import styx.Complex;
import styx.StyxException;
import styx.Session;
import styx.core.expressions.FuncRegistry;

public final class AllIntrinsics {

    public static Complex buildEnvironment(FuncRegistry registry, Session session) throws StyxException {
        return session.complex()
                .put(session.text("session"),    SessionIntrinsics.   buildEnvironment(registry, session))
                .put(session.text("collection"), CollectionIntrinsics.buildEnvironment(registry, session))
                .put(session.text("file"),       FileIntrinsics.      buildEnvironment(registry, session))
                .put(session.text("console"),    ConsoleIntrinsics.   buildEnvironment(registry, session))
                .put(session.text("math"),       MathIntrinsics.      buildEnvironment(registry, session))
                .put(session.text("time"),       TimeIntrinsics.      buildEnvironment(registry, session));
    }
}
