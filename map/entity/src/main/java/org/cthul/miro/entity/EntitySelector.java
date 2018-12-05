package org.cthul.miro.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.util.Completable;

/**
 * Finds or creates entities based on their key.
 * @param <Entity>
 */
public interface EntitySelector<Entity> extends AutoCloseable, Completable {
    
    Entity get(Object... key) throws MiException;
    
    @Override
    void complete() throws MiException;
    
    @Override
    default void close() throws MiException {
        complete();
    }
    
    default EntitySelector<Entity> fetch(Object... key) throws MiException {
        get(key);
        return this;
    }
    
    default List<Entity> getAll(Object... keys) throws MiException {
        return getAll(Arrays.asList(keys));
    }
    
    default List<Entity> getAll(Iterable<?> keys) throws MiException {
        Object[] k = new Object[1];
        List<Entity> result = new ArrayList<>();
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
    
    default EntitySelector<Entity> fetchAll(Object... keys) throws MiException {
        return fetchAll(Arrays.asList(keys));
    }
    
    default EntitySelector<Entity> fetchAll(Iterable<?> keys) throws MiException {
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
    
    default EntitySelector<Entity> and(EntityInitializer<? super Entity> initializer) {
        return Entities.buildSelector(b -> b.set(this).add(initializer));
    }
}
