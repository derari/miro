package org.cthul.miro.entity;

import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.util.Completable;
import org.cthul.miro.util.XConsumer;
import org.cthul.miro.util.XSupplier;

/**
 *
 * @param <Entity>
 */
public interface FactoryBuilder<Entity> extends InitializationBuilder<Entity> {
    
    <E extends Entity> FactoryBuilder<E> set(EntityFactory<E> factory);
    
    default <E extends Entity> FactoryBuilder<E> set(EntityTemplate<E> type, MiResultSet resultSet) throws MiException {
        type.newFactory(resultSet, this);
        return (FactoryBuilder<E>) this;
    }
    
    <E extends Entity> FactoryBuilder<E> setFactory(XSupplier<? extends E, ?> factory);
    
    default <E extends Entity> FactoryBuilder<E> setNamedFactory(XSupplier<E, ?> factory) {
        FactoryBuilder<E> fb = setFactory(factory);
        fb.addName(factory);
        return fb;
    }
    
    @Override
    FactoryBuilder<Entity> add(EntityInitializer<? super Entity> initializer);
    
    @Override
    default FactoryBuilder<Entity> add(EntityConfiguration<? super Entity> configuration, MiResultSet resultSet) throws MiException {
        configuration.newInitializer(resultSet, this);
        return this;
    }
    
    @Override
    FactoryBuilder<Entity> addInitializer(XConsumer<? super Entity, ?> initializer);
    
    @Override
    default FactoryBuilder<Entity> addNamedInitializer(XConsumer<? super Entity, ?> initializer) {
        return addInitializer(initializer).addName(initializer);
    }
    
    @Override
    FactoryBuilder<Entity> addCompletable(Completable completable);
    
    @Override
    FactoryBuilder<Entity> addCloseable(AutoCloseable closeable);
    
    @Override
    FactoryBuilder<Entity> addName(Object name);
    
    @Override
    default <C extends Completable & AutoCloseable> FactoryBuilder<Entity> addCompleteAndClose(C completeAndCloseable) {
        return addCompletable(completeAndCloseable).addCloseable(completeAndCloseable);
    }
    
    @Override
    default <T extends Throwable> FactoryBuilder<Entity> build(XConsumer<? super InitializationBuilder<Entity>, T> action) throws T {
        action.accept(this);
        return this;
    }
}
