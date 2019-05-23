package org.cthul.miro.db.request;

/**
 *
 */
public interface AutocloseableBuilder extends AutoCloseable {
    
    @Override
    void close();
}
