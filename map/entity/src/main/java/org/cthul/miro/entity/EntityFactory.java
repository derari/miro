package org.cthul.miro.entity;

import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.builder.BatchFactory;
import org.cthul.miro.util.Completable;
import org.cthul.miro.util.XFunction;

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
        return new BatchFactory<>(this);
    }
    
    /**
     * Creates a factory that applies an initializer to new entities.
     * @param initializer
     * @return initializing factory
     */
    default EntityFactory<Entity> with(EntityInitializer<? super Entity> initializer) {
        return Entities.initializingFactory(this, initializer);
    }
    
    default <T> EntityFactory<T> andThen(XFunction<? super Entity, ? extends T, MiException> function) {
        return new EntityFactory<T>() {
            @Override
            public T newEntity() throws MiException {
                Entity e = EntityFactory.this.newEntity();
                return function.apply(e);
            }
            @Override
            public void complete() throws MiException {
                EntityFactory.this.complete();
            }
            @Override
            public void close() throws MiException {
                EntityFactory.this.close();
            }
        };
    }
}
