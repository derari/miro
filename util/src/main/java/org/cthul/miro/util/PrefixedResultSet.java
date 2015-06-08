package org.cthul.miro.util;

import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;



/**
 *
 */
public class PrefixedResultSet implements MiResultSet {

    private final String prefix;
    private final MiResultSet resultSet;

    public PrefixedResultSet(String prefix, MiResultSet resultSet) {
        this.prefix = prefix;
        this.resultSet = resultSet;
    }
    
    @Override
    public Object get(int i) throws MiException {
        return resultSet.get(i);
    }

    @Override
    public int getInt(int i) throws MiException {
        return resultSet.getInt(i);
    }

    @Override
    public long getLong(int i) throws MiException {
        return resultSet.getLong(i);
    }

    @Override
    public String getString(int i) throws MiException {
        return resultSet.getString(i);
    }

    @Override
    public boolean next() throws MiException {
        return resultSet.next();
    }

    @Override
    public boolean previous() throws MiException {
        return resultSet.previous();
    }

    @Override
    public void close() throws MiException {
        resultSet.close();
    }

    @Override
    public int findColumn(String label) throws MiException {
        label = prefix + label;
        return resultSet.findColumn(label);
    }
    
    @Override
    public int getColumnCount() throws MiException {
        return resultSet.getColumnCount();
    }

    @Override
    public String getColumnLabel(int columnIndex) throws MiException {
        String l = resultSet.getColumnLabel(columnIndex);
        if (l.startsWith(prefix) || l.startsWith(prefix.toUpperCase())) {
            return l.substring(prefix.length());
        }
        return "";
    }
}
