package org.cthul.miro.entity;

import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.util.XSupplier;

/**
 *
 * @param <Entity>
 */
public interface FactoryBuilder<Entity> extends InitializationBuilder<Entity> {
    
    <E extends Entity> FactoryBuilder<E> set(EntityFactory<E> factory);
    
    default <E extends Entity> FactoryBuilder<E> set(EntityType<E> type, MiResultSet resultSet) throws MiException {
        type.newFactory(resultSet, this);
        return (FactoryBuilder<E>) this;
    }
    
    <E extends Entity> FactoryBuilder<E> setFactory(XSupplier<E, ?> factory);
    
    default <E extends Entity> FactoryBuilder<E> setNamedFactory(XSupplier<E, ?> factory) {
        FactoryBuilder<E> fb = setFactory(factory);
        fb.addName(factory);
        return fb;
    }
}
