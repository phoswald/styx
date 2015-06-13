package styx.db.jdbc;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Paths;

import org.junit.Test;

import styx.StyxException;
import styx.core.sessions.TestBase;
import styx.db.RowDatabase;

public class TestDerbyDatabase extends TestBase {

    @Test
    public void testOpenUnexisting() throws StyxException {
        try(RowDatabase db = new JdbcDatabase("jdbc:derby:" + Paths.get("target", "styx-session", "TestDerbyDatabase.derby").toString(), JdbcDialect.Derby.name())) {
            fail();
        } catch(StyxException e) {
            assertTrue(e.getMessage().contains("Failed to open JDBC connection."));
        }
    }
}
