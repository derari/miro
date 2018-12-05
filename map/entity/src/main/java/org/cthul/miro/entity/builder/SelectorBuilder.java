package org.cthul.miro.entity.builder;

import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntitySelector;
import org.cthul.miro.entity.*;
import org.cthul.miro.util.Completable;
import org.cthul.miro.util.XConsumer;
import org.cthul.miro.util.XFunction;

/**
 *
 * @param <Entity>
 */
public interface SelectorBuilder<Entity> extends InitializationBuilder<Entity> {
    
    <E extends Entity> SelectorBuilder<E> set(EntitySelector<? extends E> selector);
    
    <E extends Entity> SelectorBuilder<E> setSelector(XFunction<Object[], ? extends E, ?> selector);
    
    default <E extends Entity> SelectorBuilder<E> setNamedSelector(XFunction<Object[], ? extends E, ?> factory) {
        SelectorBuilder<E> fb = setSelector(factory);
        return fb.addName(factory);
    }
    
    @Override
    SelectorBuilder<Entity> add(EntityInitializer<? super Entity> initializer);
    
    @Override
    default SelectorBuilder<Entity> add(EntityConfiguration<? super Entity> configuration, MiResultSet resultSet) throws MiException {
        configuration.newInitializer(resultSet, this);
        return this;
    }
    
    @Override
    SelectorBuilder<Entity> addInitializer(XConsumer<? super Entity, ?> initializer);
    
    @Override
    default SelectorBuilder<Entity> addNamedInitializer(XConsumer<? super Entity, ?> initializer) {
        return addInitializer(initializer).addName(initializer);
    }
    
    @Override
    SelectorBuilder<Entity> addCompletable(Completable completable);
    
    @Override
    SelectorBuilder<Entity> addCloseable(AutoCloseable closeable);
    
    @Override
    SelectorBuilder<Entity> addName(Object name);
    
    @Override
    default <C extends Completable & AutoCloseable> SelectorBuilder<Entity> addCompleteAndClose(C completeAndCloseable) {
        return addCompletable(completeAndCloseable).addCloseable(completeAndCloseable);
    }
    
    @Override
    default <T extends Throwable> SelectorBuilder<Entity> build(XConsumer<? super InitializationBuilder<Entity>, T> action) throws T {
        action.accept(this);
        return this;
    }
}
