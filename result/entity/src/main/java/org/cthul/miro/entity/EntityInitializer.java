package org.cthul.miro.entity;

import org.cthul.miro.db.MiException;
import org.cthul.miro.util.Completable;

/**
 * Initializes entities.
 * 
 * @param <Entity> 
 */
public interface EntityInitializer<Entity> extends Completable, AutoCloseable {
    
    /**
     * Marks entity as to be initialized.
     * @param entity
     * @throws MiException
     */
    void apply(Entity entity) throws MiException;

    /**
     * When this call returns, all entities previously passed to 
     * {@link #apply(java.lang.Object)} must be initialized.
     * @throws MiException
     */
    @Override
    default void complete() throws MiException {
    }

    @Override
    default void close() throws MiException {
        complete();
    }
}
