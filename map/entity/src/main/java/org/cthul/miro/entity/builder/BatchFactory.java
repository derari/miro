package org.cthul.miro.entity.builder;

import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityFactory;

/**
 *
 */
public class BatchFactory<Entity> implements EntityFactory<Entity> {
    
    private final EntityFactory<Entity> factory;

    public BatchFactory(EntityFactory<Entity> factory) {
        this.factory = factory;
    }

    @Override
    public Entity newEntity() throws MiException {
        return factory.newEntity();
    }

    @Override
    public void complete() throws MiException {
        factory.complete();
    }

    @Override
    public void close() throws MiException {
        complete();
    }

    @Override
    public EntityFactory<Entity> batch() {
        return this;
    }

    @Override
    public String toString() {
        return "batch " + factory;
    }
    
}
