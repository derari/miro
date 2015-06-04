package org.cthul.miro.entity;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 * Provides {@link EntityInitializer}s.
 * 
 * @param <Entity> 
 */
public interface EntityConfiguration<Entity> {
    
    /**
     * Creates a new initializer.
     * <p> Closing the initializer should not close the result set.
     * @param resultSet
     * @return initializer
     * @throws MiException 
     */
    EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException;
    
    default EntityConfiguration<Entity> and(EntityConfiguration<Entity> cfg) {
        return EntityTypes.multiConfiguration(this, cfg);
    }
}
