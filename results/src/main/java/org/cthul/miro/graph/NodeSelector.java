package org.cthul.miro.graph;

import org.cthul.miro.db.MiException;
import org.cthul.miro.util.Completable;

/**
 *
 */
public interface NodeSelector<Node> extends AutoCloseable, Completable {
    
    Node get(Object... key) throws MiException;
    
    @Override
    void complete() throws MiException;
    
    @Override
    void close() throws MiException;
}
