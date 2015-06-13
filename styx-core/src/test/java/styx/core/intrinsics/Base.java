package styx.core.intrinsics;

import static org.junit.Assert.assertEquals;
import styx.StyxException;
import styx.Session;
import styx.Value;

public class Base {

    protected Value evaluate(Session session, String script) throws StyxException {
        try {
            Value parsed   = session.parse(script, true);
            String   serial   = session.serialize(parsed, true);
            Value reparsed = session.deserialize(serial);
            String   reserial = session.serialize(reparsed, true);
            assertEquals(parsed.getClass(), reparsed.getClass());
            assertEquals(serial, reserial);
        } catch (StyxException e) {
            throw new RuntimeException(e);
        }
        return session.evaluate(script);
    }
}
