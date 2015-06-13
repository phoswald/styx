package styx.db.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.sql.Statement;

public enum JdbcDialect {

    Default { },

    SQLite { // driver: org.sqlite.JDBC
        @Override
        public void initConnection(Connection conn, int timeoutMS) throws SQLException {
            // The default isolation level is TRANSACTION_SERIALIZABLE, so there is no need to set it.
            // Only TRANSACTION_SERIALIZABLE and TRANSACTION_READ_UNCOMMITTED are supported.
            // conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            // The default timeout is 3000 ms.
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA busy_timeout = " + timeoutMS + ";");
            }
        }

        @Override
        public boolean recoverable(SQLException e) {
            // The same error occurs in two different formats.
            return  (e.getMessage() != null && e.getMessage().contains("[SQLITE_BUSY]  The database file is locked (database is locked)")) ||
                    (e.getMessage() != null && e.getMessage().equals("database is locked")) ||
                    super.recoverable(e);
        }
    },

    Derby { // driver: org.apache.derby.jdbc.EmbeddedDriver or org.apache.derby.jdbc.AutoloadedDriver
        @Override
        public void initConnection(Connection conn, int timeoutMS) throws SQLException {
            // The default isolation level is TRANSACTION_READ_COMMITTED.
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            // The default timeout is 60 sec.
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.locks.waitTimeout', '" + (timeoutMS / 1000) + "')");
            }
        }

        @Override
        public boolean recoverable(SQLException e) {
            return  (e.getSQLState() != null && e.getSQLState().equals("40XL1")) || // lockwait timeout
                    (e.getSQLState() != null && e.getSQLState().equals("40XL2")) || // lockwait timeout
                    (e.getSQLState() != null && e.getSQLState().equals("40001")) || // deadlock timeout
                    super.recoverable(e);
        }
    },

    H2 { // driver: org.h2.Driver
        @Override
        public void initConnection(Connection conn, int timeoutMS) throws SQLException {
            // Set mode 1 (Serializable). The default mode is 3 (Read Committed)
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("SET LOCK_MODE 1");
            }

            // The default timeout is maybe 1000 ms.
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("SET LOCK_TIMEOUT " + timeoutMS);
            }
        }

        @Override
        public boolean recoverable(SQLException e) {
            return  (e.getErrorCode() == 40001) || // DEADLOCK_1
                    (e.getErrorCode() == 50200) || // LOCK_TIMEOUT_1
                    super.recoverable(e);
        }
    },

    MySQL { // driver: com.mysql.jdbc.Driver
        @Override
        public void initConnection(Connection conn, int timeoutMS) throws SQLException {
            // The default isolation level is TRANSACTION_READ_COMMITTED.
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            // The default timeout is maybe 50 sec.
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("SET innodb_lock_wait_timeout = " + timeoutMS / 1000);
            }
        }

        @Override
        public void createSchema(Connection conn) throws SQLException {
        	// Notes:
        	// - The max. value size is 21845 bytes, so we use TEXT instead of VARCHAR
        	// - The max. key size is 767 bytes (how many characters is unclear)
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE STYX_DATA(PARENT VARCHAR(100) NOT NULL, NAME VARCHAR(250) NOT NULL, SUFFIX INT, VALUE TEXT, PRIMARY KEY (PARENT, NAME))");
            }
        }

        @Override
        public boolean recoverable(SQLException e) {
            return  (e.getSQLState() != null && e.getSQLState().equals("41000")) || // Error Code: 1205 (ER_LOCK_WAIT_TIMEOUT)
            		(e.getSQLState() != null && e.getSQLState().equals("40001")) || // Error Code: 1213 (ER_LOCK_DEADLOCK)
            		super.recoverable(e);
        }
    },

    MSSQL { // driver: net.sourceforge.jtds.jdbc.Driver
        @Override
        public void initConnection(Connection conn, int timeoutMS) throws SQLException {
            // The default isolation level is TRANSACTION_READ_COMMITTED.
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            // The default timeout is maybe -1 (infinite).
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("SET LOCK_TIMEOUT " + timeoutMS);
            }
        }

        @Override
        public void createSchema(Connection conn) throws SQLException {
        	// Notes:
        	// - Use NVARCHAR instead of VARCHAR for Unicode
        	// - Use MAX instead of a limited length where possible.
        	// - The max. key size is 900 bytes (450 characters)
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE STYX_DATA(PARENT NVARCHAR(100) NOT NULL, NAME NVARCHAR(350) NOT NULL, SUFFIX INT, VALUE NVARCHAR(MAX), PRIMARY KEY (PARENT, NAME))");
            }
        }

        @Override
        public boolean recoverable(SQLException e) {
            return  (e.getSQLState() != null && e.getSQLState().equals("S1000")) || // Error Code: 1222 (Lock request time out period exceeded.)
            		(e.getSQLState() != null && e.getSQLState().equals("40001")) || // Error Code: 1205 (Transaction (Process ID ....) was deadlocked on lock resources with another process and has been chosen as the deadlock victim. Rerun the transaction.)
            		super.recoverable(e);
        }
    };

    public void initConnection(Connection conn, int timeoutMS) throws SQLException {
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }

    public void createSchema(Connection conn) throws SQLException {
        try(Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE STYX_DATA(PARENT VARCHAR(100) NOT NULL, NAME VARCHAR(10000) NOT NULL, SUFFIX INT, VALUE VARCHAR(30000), PRIMARY KEY (PARENT, NAME))");
        }
    }

    public boolean recoverable(SQLException e) {
        return e instanceof SQLTransientException;
    }
}
