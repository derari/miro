package org.cthul.miro.entity;

import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.builder.SelectorBuilder;
import org.cthul.miro.util.Completable;
import org.cthul.miro.util.CompletableBuilder;
import org.cthul.miro.util.XConsumer;

/**
 * Builds {@link EntityInitializer}.
 * @param <Entity>
 */
public interface InitializationBuilder<Entity> extends CompletableBuilder {
    
    InitializationBuilder<Entity> add(EntityInitializer<? super Entity> initializer);
    
    default InitializationBuilder<Entity> add(EntityConfiguration<? super Entity> configuration, MiResultSet resultSet) throws MiException {
        configuration.newInitializer(resultSet, this);
        return this;
    }
    
    InitializationBuilder<Entity> addInitializer(XConsumer<? super Entity, ?> initializer);
    
    default InitializationBuilder<Entity> addNamedInitializer(XConsumer<? super Entity, ?> initializer) {
        return addInitializer(initializer).addName(initializer);
    }
    
    @Override
    InitializationBuilder<Entity> addCompletable(Completable completable);
    
    @Override
    InitializationBuilder<Entity> addCloseable(AutoCloseable closeable);
    
    @Override
    InitializationBuilder<Entity> addName(Object name);
    
    @Override
    default <C extends Completable & AutoCloseable> InitializationBuilder<Entity> addCompleteAndClose(C completeAndCloseable) {
        return addCompletable(completeAndCloseable).addCloseable(completeAndCloseable);
    }
    
    default <T extends Throwable> InitializationBuilder<Entity> build(XConsumer<? super InitializationBuilder<Entity>, T> action) throws T {
        action.accept(this);
        return this;
    }
    
    default <E, T extends Throwable> EntityInitializer<E> nestedInitializer(XConsumer<? super InitializationBuilder<E>, T> action) throws T {
        return Entities.buildNestedInitializer(this, action);
    }
    
    default <E> EntityInitializer<E> nestedInitializer(EntityConfiguration<? super E> configuration, MiResultSet resultSet) throws MiException {
        return Entities.buildNestedInitializer(this, b -> configuration.newInitializer(resultSet, b));
    }
    
    default <E, T extends Throwable> EntityFactory<E> nestedFactory(XConsumer<? super FactoryBuilder<E>, T> action) throws T {
        return Entities.buildNestedFactory(this, action);
    }
    
    default <E, T extends Throwable> EntityFactory<E> nestedFactory(EntityTemplate<E> type, MiResultSet resultSet) throws MiException {
        return Entities.buildNestedFactory(this, b -> type.newFactory(resultSet, b));
    }
    
    default <E, T extends Throwable> EntitySelector<E> nestedSelector(XConsumer<? super SelectorBuilder<E>, T> action) throws T {
        return Entities.buildNestedSelector(this, action);
    }
    
//    default <E, T extends Throwable> EntitySelector<E> nestedSelector(EntityTemplate<E> type, MiResultSet resultSet) throws MiException {
//        return Entities.buildNestedSelector(this, b -> type.newSelector(resultSet, b));
//    }
}
