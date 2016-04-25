package org.cthul.miro.migrate;

import org.cthul.miro.db.MiException;

/**
 *
 */
public interface Migration<DB> extends Comparable<Migration<DB>> {
    
    Version getVersion();

    void up(DB db) throws MiException;
    
    void down(DB db) throws MiException;
    
    @Override
    default int compareTo(Migration<DB> o) {
        return getVersion().compareTo(o.getVersion());
    }
}
