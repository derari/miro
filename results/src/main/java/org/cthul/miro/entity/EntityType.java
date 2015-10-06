package org.cthul.miro.entity;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 * Creates {@linkplain EntityFactory entity factories}
 * for a {@linkplain MiResultSet result set}.
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
    
    /**
     * Creates a typed array of the given length.
     * @param length
     * @return new array
     */
    Entity[] newArray(int length);
    
    /**
     * Creates a type that will create initializing factories.
     * @param configuration
     * @return configured type
     */
    default EntityType<Entity> with(EntityConfiguration<? super Entity> configuration) {
        return EntityTypes.configuredType(this, configuration);
    }
}
