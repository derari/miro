package org.cthul.miro.entity.builder;

import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.FactoryBuilder;
import org.cthul.miro.util.*;

/**
 *
 * @param <Entity>
 */
public class NestedFactoryBuilder<Entity> extends NestedInitializationtBuilderBase<Entity, FactoryBuilder<Entity>> implements FactoryBuilder<Entity> {
    
    private XSupplier<? extends Entity, ?> factory = null;
    private Object factoryName = null;
    
    private boolean singleFactoryMode = true;
    private EntityFactory<? extends Entity> singleFactory = null;

    public NestedFactoryBuilder(CompletableBuilder completableBuilder) {
        super(completableBuilder);
    }
    
    private void disableSingleFactoryMode() {
        if (!singleFactoryMode) return;
        singleFactoryMode = false;
        if (singleFactory == null) return;
        setFactory(singleFactory::newEntity);
        singleFactory = null;
    }

    @Override
    public <E extends Entity> FactoryBuilder<E> set(EntityFactory<E> factory) {
        if (singleFactoryMode) {
            if (singleFactory == null) {
                singleFactory = factory;
                super.addCompletable(factory);
                super.addCloseable(factory);
                return (FactoryBuilder) this;
            } else {
                disableSingleFactoryMode();
            }
        }
        if (factory instanceof CompositeFactory) {
            CompositeFactory<E> cf = (CompositeFactory<E>) factory;
            if (cf.factoryName != null) {
                addName(factoryName);
            }
            return this.<E>setFactory(cf.supplier).add(cf.setup);
        } else {
            addName(factory);
            addCompleteAndClose(factory);
            return setFactory(factory::newEntity);
        }
    }

    @Override
    public <E extends Entity> FactoryBuilder<E> setFactory(XSupplier<? extends E, ?> factory) {
        disableSingleFactoryMode();
        if (this.factory != null) {
            throw new IllegalStateException("Factory already set");
        }
        this.factory = factory;
        return (FactoryBuilder) this;
    }

    @Override
    public FactoryBuilder<Entity> addName(Object name) {
        disableSingleFactoryMode();
        if (factoryName == null) {
            factoryName = name;
            return this;
        } else {
            return super.addName(name);
        }
    }

    @Override
    public FactoryBuilder<Entity> add(EntityInitializer<? super Entity> initializer) {
        disableSingleFactoryMode();
        return super.add(initializer);
    }

    @Override
    public FactoryBuilder<Entity> addInitializer(XConsumer<? super Entity, ?> initializer) {
        disableSingleFactoryMode();
        if (factoryName != null && factory == null) {
            super.addName(factoryName);
            factoryName = null;
        }
        return super.addInitializer(initializer);
    }

    @Override
    public FactoryBuilder<Entity> addCompletable(Completable completable) {
        disableSingleFactoryMode();
        return super.addCompletable(completable);
    }

    @Override
    public FactoryBuilder<Entity> addCloseable(AutoCloseable closeable) {
        disableSingleFactoryMode();
        return super.addCloseable(closeable);
    }
    
    public EntityFactory<Entity> buildFactory() {
        if (singleFactoryMode) {
            return (EntityFactory) singleFactory;
        }
        if (factory == null) {
            throw new IllegalStateException("factory required");
        }
        return new CompositeFactory<>(factory, factoryName, buildInitializer());
    }

    @Override
    public String toString() {
        return (factoryName != null ? factoryName : "?") + " with " + super.toString();
    }
    
}
