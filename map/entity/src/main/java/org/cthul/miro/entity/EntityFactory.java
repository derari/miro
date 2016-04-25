package org.cthul.miro.entity;

import org.cthul.miro.db.MiException;
import org.cthul.miro.util.Completable;

/**
 * Creates new entities.
 *
 * @param <Entity> 
 */
public interface EntityFactory<Entity> extends Completable, AutoCloseable {
    
    /**
     * Creates a new entity.
     * @return new entity
     * @throws MiException 
     */
    Entity newEntity() throws MiException;

    /**
     * When this call returns, all entities previously created
     * must be initialized.
     * @throws MiException
     */
    @Override
    default void complete() throws MiException {
    }

    @Override
    default void close() throws MiException {
        complete();
    }
    
    /**
     * Returns a factory that completes, but not closes, this factory when it is closed.
     * @return batch factory
     */
    default EntityFactory<Entity> batch() {
        return EntityTypes.batch(this);
    }
    
    /**
     * Creates a factory that applies an initializer to new entities.
     * @param initializer
     * @return initializing factory
     */
    default EntityFactory<Entity> with(EntityInitializer<? super Entity> initializer) {
        return EntityTypes.initializingFactory(this, initializer);
    }
}
