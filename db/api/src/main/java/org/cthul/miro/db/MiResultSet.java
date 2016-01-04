package org.cthul.miro.db;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.db.impl.PrefixedResultSet;

/**
 * The result of a database query.
 */
public interface MiResultSet extends AutoCloseable {
    
    boolean next() throws MiException;
    
    boolean previous() throws MiException;

    Object get(int columnIndex) throws MiException;

    int getInt(int columnIndex) throws MiException;

    long getLong(int columnIndex) throws MiException;

    String getString(int columnIndex) throws MiException;

    @Override
    void close() throws MiException;

    int getColumnCount() throws MiException;
    
    String getColumnLabel(int columnIndex) throws MiException;
    
    default int findColumn(String label) throws MiException {
        int c = getColumnCount();
        for (int i = 0; i < c; c++) {
            if (getColumnLabel(i).equals(label)) {
                return i;
            }
        }
        return -1;
    }
    
    default List<String> listColumns() throws MiException {
        int c = getColumnCount();
        List<String> result = new ArrayList<>(c);
        for (int i = 1; i <= c; i++) {
            result.add(getColumnLabel(i));
        }
        return result;
    }
    
    default MiResultSet subResult(String prefix) {
        return new PrefixedResultSet(prefix, this);
    }
}
