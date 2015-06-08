package org.cthul.miro.db;

import org.cthul.miro.util.PrefixedResultSet;

/**
 *
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
    
    default MiResultSet subResult(String prefix) {
        return new PrefixedResultSet(prefix, this);
    }
}
