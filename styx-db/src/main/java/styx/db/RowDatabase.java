package styx.db;

import java.util.List;

import styx.StyxException;

/**
 * The interface for storing generic STYX values in a relational database.
 */
public interface RowDatabase extends AutoCloseable {

    @Override
    public void close() throws StyxException;

    public List<Row> selectAll() throws StyxException;

    public Row selectSingle(String parent, String name) throws StyxException;

    public List<Row> selectChildren(String parent) throws StyxException;

    public List<Row> selectDescendants(String parent) throws StyxException;

    public int selectMaxSuffix(String parent) throws StyxException;

    public void insert(String parent, String name, int suffix, String value) throws StyxException;

    public void update(String parent, String name, String value) throws StyxException; // TODO (cleanup-) not used, either remove or optimize usage

    public void deleteAll() throws StyxException;

    public void deleteSingle(String parent, String name) throws StyxException;

    public void deleteDescendants(String parent) throws StyxException;

    public boolean hasTransaction();

    public void beginTransaction() throws StyxException;

    public void commitTransaction() throws StyxException;

    public void abortTransaction(boolean retry) throws StyxException;
}
