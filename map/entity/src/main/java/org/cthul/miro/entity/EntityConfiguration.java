package org.cthul.miro.entity;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 * Creates {@linkplain EntityInitializer initializers} 
 * for a {@linkplain MiResultSet result set}.
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
    default EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException {
        return Entities.buildInitializer(builder -> newInitializer(resultSet, builder));
    }

    void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Entity> builder) throws MiException;
    
    /**
     * Creates a configuration that will apply both this and the given configuration.
     * @param <E> entity type of the new configuration
     * @param cfg other configuration
     * @return combined configuration
     */
    default <E extends Entity> EntityConfiguration<E> and(EntityConfiguration<? super E> cfg) {
        return Entities.multiConfiguration(this, cfg);
    }
}
