package styx.db.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import styx.ConcurrentException;
import styx.StyxException;
import styx.db.Row;
import styx.db.RowDatabase;

/**
 * Implements relational database access using JDBC.
 */
public final class JdbcDatabase implements RowDatabase {

    private static final Logger LOG = Logger.getLogger(JdbcDatabase.class.toString());

    private final String      connstr;
    private final JdbcDialect dialect;

    /**
     * The connection is opened in the constructor and closed in close().
     * Also, the connection can be closed and reopened when an error occurs.
     */
    private Connection conn;

    /**
     * Contains all PreparedStatements. They are created in makePreparedStatement() and closed in close() and can be reused.
     * When working with ResultSets, the ResultSet must be closed explicitly after usage.
     *
     * Normal (not reusable) Statements  must be closed explicitly after usage, but their ResultSets are closed automatically.
     */
    private Map<String, PreparedStatement> stmts;

    public JdbcDatabase(String connstr, String dialect) throws StyxException {
        this.connstr = connstr;
        this.dialect = JdbcDialect.valueOf(dialect);
        try {
            open();
            checkSchema();
        } catch(RuntimeException | StyxException e) {
            close();
            throw e;
        }
    }

    private void open() throws StyxException {
        try {
            LOG.info("Opening database (" + dialect + ").");
            conn  = DriverManager.getConnection(connstr);
            stmts = new HashMap<>();
            dialect.initConnection(conn, 2000);
        } catch (SQLException e) {
            LOG.severe("Failed to open JDBC connection (" + dialect + "): " + e);
            throw new StyxException("Failed to open JDBC connection.", e);
        }
    }

    @Override
    public void close() throws StyxException {
        if(stmts != null) {
            for(PreparedStatement stmt: stmts.values()) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.warning("Failed to close JDBC prepared statement (" + dialect + "): " + e);
                }
            }
            stmts = null;
        }
        if(conn != null) {
            try {
                LOG.info("Closing database (" + dialect + ").");
                conn.close();
            } catch (SQLException e) {
                LOG.severe("Failed to close JDBC connection (" + dialect + "): " + e);
                throw new StyxException("Failed to close JDBC connection.", e);
            } finally {
                conn = null;
            }
        }
    }

    @Override
    public List<Row> selectAll() throws StyxException {
        try {
            List<Row> result = new ArrayList<>();
            try(Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT PARENT, NAME, SUFFIX, VALUE FROM STYX_DATA ORDER BY PARENT, NAME");
                while(rs.next()) {
                    result.add(new Row(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
                }
            }
            return result;
        } catch (SQLException e) {
            throw wrap("Failed to execute SELECT query.", e);
        }
    }

    @Override
    public Row selectSingle(String parent, String name) throws StyxException {
        try {
            PreparedStatement stmt = makePreparedStatement("SELECT SUFFIX, VALUE FROM STYX_DATA WHERE PARENT = ? AND NAME = ?");
            stmt.setString(1, parent);
            stmt.setString(2, name);
            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return new Row(parent, name, rs.getInt(1), rs.getString(2));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw wrap("Failed to execute SELECT query.", e);
        }
    }

    @Override
    public List<Row> selectChildren(String parent) throws StyxException {
        try {
            List<Row> result = new ArrayList<>();
            PreparedStatement stmt = makePreparedStatement("SELECT NAME, SUFFIX FROM STYX_DATA WHERE PARENT = ?");
            stmt.setString(1, parent);
            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    result.add(new Row(parent, rs.getString(1), rs.getInt(2), null));
                }
            }
            return result;
        } catch (SQLException e) {
            throw wrap("Failed to execute SELECT query.", e);
        }
    }

    @Override
    public List<Row> selectDescendants(String parent) throws StyxException {
        try {
            List<Row> result = new ArrayList<>();
            PreparedStatement stmt = makePreparedStatement("SELECT PARENT, NAME, SUFFIX, VALUE FROM STYX_DATA WHERE PARENT LIKE ?");
            stmt.setString(1, parent + "%");
            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    result.add(new Row(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
                }
            }
            return result;
        } catch (SQLException e) {
            throw wrap("Failed to execute SELECT query.", e);
        }
    }

    @Override
    public int selectMaxSuffix(String parent) throws StyxException {
        try {
             PreparedStatement stmt = makePreparedStatement("SELECT MAX(SUFFIX) FROM STYX_DATA WHERE PARENT = ?");
             stmt.setString(1, parent);
             try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw wrap("Failed to execute SELECT statement.", e);
        }
    }

    @Override
    public void insert(String parent, String name, int suffix, String value) throws StyxException {
        try {
            PreparedStatement stmt = makePreparedStatement("INSERT INTO STYX_DATA (PARENT, NAME, SUFFIX, VALUE) VALUES (?,?,?,?)");
            stmt.setString(1, parent);
            stmt.setString(2, name);
            stmt.setInt   (3, suffix);
            stmt.setString(4, value);
            stmt.execute();
        } catch (SQLException e) {
            throw wrap("Failed to execute INSERT statement.", e);
        }
    }

    @Override
    public void update(String parent, String name, String value) throws StyxException {
        try {
            PreparedStatement stmt = makePreparedStatement("UPDATE STYX_DATA SET VALUE = ? WHERE PARENT = ? AND NAME = ?");
            stmt.setString(1, value);
            stmt.setString(2, parent);
            stmt.setString(3, name);
            stmt.execute();
        } catch (SQLException e) {
            throw wrap("Failed to execute UPDATE statement.", e);
        }
    }

    @Override
    public void deleteAll() throws StyxException {
        try {
            try(Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM STYX_DATA");
            }
        } catch (SQLException e) {
            throw wrap("Failed to execute DELETE statement.", e);
        }
    }

    @Override
    public void deleteSingle(String parent, String name) throws StyxException {
        try {
            PreparedStatement stmt = makePreparedStatement("DELETE FROM STYX_DATA WHERE PARENT = ? AND NAME = ?");
            stmt.setString(1, parent);
            stmt.setString(2, name);
            stmt.execute();
        } catch (SQLException e) {
            throw wrap("Failed to execute DELETE statement.", e);
        }
    }

    @Override
    public void deleteDescendants(String parent) throws StyxException {
        try {
            PreparedStatement stmt = makePreparedStatement("DELETE FROM STYX_DATA WHERE PARENT LIKE ?");
            stmt.setString(1, parent + "%");
            stmt.execute();
        } catch (SQLException e) {
            throw wrap("Failed to execute DELETE statement.", e);
        }
    }

    @Override
    public boolean hasTransaction() {
        try {
            return !conn.getAutoCommit();
        } catch (SQLException e) {
            LOG.severe("Failed to check transaction state (" + dialect + "): " + e);
            return false;
        }
    }

    @Override
    public void beginTransaction() throws StyxException {
        try {
            // Note: multiple calls to are silently ignored.
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw wrap("Failed to start transaction.", e);
        }
    }

    @Override
    public void commitTransaction() throws StyxException {
        try {
            // Note: multiple calls to are silently ignored.
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw wrap("Failed to commit transaction.", e);
        }
    }

    @Override
    public void abortTransaction(boolean retry) throws StyxException {
        try {
            // Note: this will fail of no transaction is active.
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            if(dialect != JdbcDialect.SQLite) {
                throw wrap("Failed to rollback transaction.", e);
            } else {
                LOG.severe("Failed to rollback transaction (" + dialect + "): " + e);
                // SQLite is nasty: rollback() starts a new transaction which is immediately committed in setAutoCommit().
                // This is unnecessary and makes problems if the database is locked, especially with prepared statements.
                // As a last resort, we throw away the connection and open a new one. If that fails, we are doomed.
                close();
                open();
            }
        }

        if(retry) {
            // TODO (optimize+): make retry after rollback smarter
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private void checkSchema() throws StyxException {
        try {
            boolean exists = false;
            try(ResultSet rs = conn.getMetaData().getTables(null, null, "STYX_DATA", null)) {
                while(rs.next()) {
                    exists = true;
                }
            }
            if(!exists) {
                LOG.info("Creating schema (" + dialect + ").");
                dialect.createSchema(conn);
            }
        } catch (SQLException e) {
            throw wrap("Failed to check or create schema.", e);
        }
    }

    private PreparedStatement makePreparedStatement(String sql) throws SQLException {
        PreparedStatement stmt = stmts.get(sql);
        if(stmt == null) {
            stmt = conn.prepareStatement(sql);
            stmts.put(sql, stmt);
        }
        return stmt;
    }

    private StyxException wrap(String message, SQLException e) {
        Throwable t = e;
        while(t != null) {
            if(t instanceof SQLException) {
                SQLException t2 = (SQLException) t;
                LOG.severe("SQL Exception (" + dialect + ", SQLState=" + t2.getSQLState() + ", ErrorCode=" + t2.getErrorCode() + "): " + t2.getMessage());
                if(dialect.recoverable(t2)) {
                    return new ConcurrentException(message, e);
                }
            }
            t = t.getCause();
        }
        return new StyxException(message, e);
    }
}
