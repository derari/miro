package org.cthul.miro.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.util.Completable;

/**
 * Finds or creates nodes based on their key.
 * @param <Node>
 */
public interface NodeSelector<Node> extends AutoCloseable, Completable {
    
    Node get(Object... key) throws MiException;
    
    @Override
    void complete() throws MiException;
    
    @Override
    default void close() throws MiException {
        complete();
    }
    
    default NodeSelector<Node> fetch(Object... key) throws MiException {
        get(key);
        return this;
    }
    
    default List<Node> getAll(Object... keys) throws MiException {
        return getAll(Arrays.asList(keys));
    }
    
    default List<Node> getAll(Iterable<?> keys) throws MiException {
        Object[] k = new Object[1];
        List<Node> result = new ArrayList<>();
        for (Object o: keys) {
            if (o instanceof Object[]) {
                result.add(get((Object[]) o));
            } else {
                k[0] = o;
                result.add(get(k));
            }
        }
        return result;
    }
    
    default NodeSelector<Node> fetchAll(Object... keys) throws MiException {
        return fetchAll(Arrays.asList(keys));
    }
    
    default NodeSelector<Node> fetchAll(Iterable<?> keys) throws MiException {
        Object[] k = new Object[1];
        for (Object o: keys) {
            if (o instanceof Object[]) {
                fetch((Object[]) o);
            } else {
                k[0] = o;
                fetch(k);
            }
        }
        return this;
    }
}
