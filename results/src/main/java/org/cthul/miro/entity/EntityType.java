package org.cthul.miro.entity;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 * Creates {@linkplain EntityFactory entity factories}.
 * 
 * @param <Entity>
 */
public interface EntityType<Entity> {
    
    /**
     * Creates a new entity factory.
     * <p> Closing the factory should not close the result set.
     * @param rs
     * @return factory
     * @throws MiException 
     */
    EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException;
    
    Entity[] newArray(int length);
    
    default EntityType<Entity> with(EntityConfiguration<? super Entity> configuration) {
        return EntityTypes.configuredType(this, configuration);
    }
}
