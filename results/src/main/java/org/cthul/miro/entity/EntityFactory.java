package org.cthul.miro.entity;

import org.cthul.miro.db.MiException;
import org.cthul.miro.util.Completable;

/**
 * Creates new entities.
 *
 * @param <Entity> 
 */
public interface EntityFactory<Entity> extends Completable, AutoCloseable {
    
    Entity newEntity() throws MiException;

    /**
     * When this call returns, all entities previously created
     * must be initialized.
     * @throws MiException
     */
    @Override
    void complete() throws MiException;

    @Override
    void close() throws MiException;
    
    /**
     * Returns a factory that completes, but not closes, this factory when it is closed.
     * @return batch factory
     */
    default EntityFactory<Entity> batch() {
        return EntityTypes.batch(this);
    }
    
    default EntityFactory<Entity> with(EntityInitializer<Entity> initializer) {
        return EntityTypes.initializingFactory(this, initializer);
    }
}
