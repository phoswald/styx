package styx.db.jdbc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import styx.StyxException;
import styx.db.TestAnyDatabase;
import styx.db.RowDatabase;

@RunWith(Parameterized.class)
public class TestJdbcDatabase extends TestAnyDatabase {

    private static final Path FILE_SQLITE = Paths.get("target", "styx-session", "TestJdbcDatabase.sqlite");
    private static final Path FILE_DERBY  = Paths.get("target", "styx-session", "TestJdbcDatabase.derby");
    private static final Path FILE_H2     = Paths.get("target", "styx-session", "TestJdbcDatabase.h2");

    private final String connstr;
    private final String dialect;

    public TestJdbcDatabase(String connstr, String dialect) {
        this.connstr = connstr;
        this.dialect = dialect;
    }

    @Parameters
    public static Collection<?> getParameters() {
        return Arrays.asList(
                new Object[] { // test parameter [0]
                        "jdbc:sqlite:" + FILE_SQLITE,
                        JdbcDialect.SQLite.name()
                },
                new Object[] { // test parameter [1]
                        "jdbc:derby:" + FILE_DERBY + ";create=true",
                        JdbcDialect.Derby.name()
                },
                new Object[] { // test parameter [2]
                        "jdbc:h2:./" + FILE_H2, // './path/file' must be used instead of 'path/file'
                        JdbcDialect.H2.name()
                });
//                new Object[] { // test parameter [3]
//                        "jdbc:mysql://localhost:3306/styx_test_jdbc_database?user=test&password=test",
//                        JdbcDialect.MySQL.name()
//                },
//                new Object[] { // test parameter [4]
//                        "jdbc:jtds:sqlserver://SRV6030-CUST-01/styx_test_jdbc_database;instance=SQL2005;user=agsb;password=agsbtest",
//                        JdbcDialect.MSSQL.name()
//                });
    }

    @BeforeClass
    public static void deleteDatabases() throws IOException {
        deleteRecursive(FILE_DERBY);
        deleteRecursive(FILE_SQLITE);
        deleteRecursive(FILE_H2);
    }

    @Override
    protected RowDatabase newDatabase() throws StyxException {
        return new JdbcDatabase(connstr, dialect);
    }
}
