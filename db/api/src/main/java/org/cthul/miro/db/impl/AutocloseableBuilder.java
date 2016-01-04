package org.cthul.miro.db.impl;

/**
 *
 */
public interface AutocloseableBuilder extends AutoCloseable {
    
    @Override
    void close();
}
