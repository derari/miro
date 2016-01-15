package org.cthul.miro.db.syntax;

/**
 *
 */
public interface AutocloseableBuilder extends AutoCloseable {
    
    @Override
    void close();
}
