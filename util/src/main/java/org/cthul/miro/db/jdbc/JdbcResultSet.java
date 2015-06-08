package org.cthul.miro.db.jdbc;



import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 *
 */
public class JdbcResultSet implements MiResultSet {
    
    private final ResultSet rs;

    public JdbcResultSet(ResultSet rs) {
        this.rs = rs;
    }

    public ResultSet getResultSet() {
        return rs;
    }

    @Override
    public boolean next() throws MiException {
        try {
            return rs.next();
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    @Override
    public boolean previous() throws MiException {
        try {
            return rs.previous();
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    @Override
    public Object get(int i) throws MiException {
        try {
            return rs.getObject(i);
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    @Override
    public int getInt(int i) throws MiException {
        try {
            return rs.getInt(i);
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    @Override
    public long getLong(int i) throws MiException {
        try {
            return rs.getLong(i);
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    @Override
    public String getString(int i) throws MiException {
        try {
            return rs.getString(i);
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    @Override
    public void close() throws MiException {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    @Override
    public int getColumnCount() throws MiException {
        try {
            return rs.getMetaData().getColumnCount();
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    @Override
    public String getColumnLabel(int columnIndex) throws MiException {
        try {
            return rs.getMetaData().getColumnLabel(columnIndex);
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    @Override
    public int findColumn(String label) throws MiException {
        try {
            return rs.findColumn(label);
        } catch (SQLException e) {
            return -1;
        }
    }
}
