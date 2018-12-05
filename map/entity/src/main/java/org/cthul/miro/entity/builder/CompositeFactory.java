package org.cthul.miro.entity.builder;

import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.util.Closeables;
import org.cthul.miro.util.XSupplier;

/**
 *
 */
public class CompositeFactory<Entity> implements EntityFactory<Entity> {
    
    final XSupplier<? extends Entity, ?> supplier;
    final Object factoryName;
    final EntityInitializer<? super Entity> setup;

    public CompositeFactory(XSupplier<? extends Entity, ?> supplier, Object factoryName, EntityInitializer<? super Entity> setup) {
        this.supplier = supplier;
        this.factoryName = factoryName;
        this.setup = setup;
    }

    @Override
    public Entity newEntity() throws MiException {
        Entity e;
        try {
            e = supplier.get();
        } catch (Throwable t) {
            throw Closeables.exceptionAs(t, MiException.class);
        }
        setup.apply(e);
        return e;
    }

    @Override
    public void complete() throws MiException {
        setup.complete();
    }

    @Override
    public void close() throws MiException {
        setup.close();
    }

    @Override
    public String toString() {
        if (setup == Entities.noInitialization()) {
            return String.valueOf(factoryName);
        }
        return String.valueOf(factoryName) + " with " + setup.toString();
    }
}
