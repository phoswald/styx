package styx.db.jdbc;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import styx.core.sessions.AbstractSessionFactory;
import styx.core.sessions.TestAnyTransaction;

@RunWith(Parameterized.class)
public class TestJdbcTransaction extends TestAnyTransaction {

    public TestJdbcTransaction(AbstractSessionFactory sf) {
        super(sf);
    }

    @Parameters
    public static Collection<?> getParameters() {
        return Arrays.<Object[]>asList(
                new Object[] { // test parameter [0]
                        JdbcSessionProvider.createSessionFactory("jdbc:sqlite:" + Paths.get("target", "styx-session", "TestJdbcSession.sqlite"), JdbcDialect.SQLite.name())
                },
                new Object[] { // test parameter [1]
                        JdbcSessionProvider.createSessionFactory("jdbc:derby:" + Paths.get("target", "styx-session", "TestJdbcSession.derby") + ";create=true", JdbcDialect.Derby.name())
                },
                new Object[] { // test parameter [2]
                        JdbcSessionProvider.createSessionFactory("jdbc:h2:./" + Paths.get("target", "styx-session", "TestJdbcSession.h2"), JdbcDialect.H2.name())
                });
//                new Object[] { // test parameter [3]
//                		JdbcSessionProvider.createSessionFactory("jdbc:mysql://localhost:3306/styx_test_jdbc_session?user=test&password=test", JdbcDialect.MySQL.name())
//                },
//                new Object[] { // test parameter [4]
//                		JdbcSessionProvider.createSessionFactory("jdbc:jtds:sqlserver://SRV6030-CUST-01/styx_test_jdbc_session;instance=SQL2005;user=agsb;password=agsbtest", JdbcDialect.MSSQL.name())
//                });
    }

}
