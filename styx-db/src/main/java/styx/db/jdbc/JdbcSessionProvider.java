package styx.db.jdbc;

import styx.Complex;
import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.SessionProvider;
import styx.StyxException;
import styx.core.sessions.AbstractSessionFactory;
import styx.core.sessions.ConcreteSession;
import styx.db.RowDatabaseData;

public class JdbcSessionProvider implements SessionProvider {

    private static final Session detached = SessionManager.getDetachedSession();

    @Override
    public String getName() {
        return "jdbc";
    }

    @Override
    public SessionFactory createSessionFactory(Complex parameters) throws StyxException {
        return createSessionFactory(
                parameters.get(detached.text("connstr")).asText().toTextString(),
                parameters.get(detached.text("dialect")).asText().toTextString());
    }

    public static AbstractSessionFactory createSessionFactory(final String connstr, final String dialect) {
        return new AbstractSessionFactory() {
            @Override
            public Session createSession() throws StyxException {
                return new ConcreteSession(new RowDatabaseData(new JdbcDatabase(connstr, dialect)), type, func, eval, environment);
            }
        };
    }
}
