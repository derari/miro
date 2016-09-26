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
     * @param resultSet
     * @return factory
     * @throws MiException 
     */
    default EntityFactory<Entity> newFactory(MiResultSet resultSet) throws MiException {
        return EntityTypes.buildFactory(this, resultSet);
    }

    void newFactory(MiResultSet resultSet, FactoryBuilder<? super Entity> builder) throws MiException;
    
    /**
     * Creates a type that will create initializing factories.
     * @param configuration
     * @return configured type
     */
    default EntityType<Entity> with(EntityConfiguration<? super Entity> configuration) {
        return EntityTypes.configuredType(this, configuration);
    }
}
