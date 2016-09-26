package org.cthul.miro.entity.base;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.util.Closeables;
import org.cthul.miro.util.Completable;

/**
 * Initializes an entity with columns from a result set.
 * @param <Entity>
 */
public class MultiInitializer<Entity> implements EntityInitializer<Entity> {

    private final MiResultSet resultSet;
    private final List<EntityInitializer<Entity>> initializers;
    private List<Completable> listeners = null;
    private List<AutoCloseable> resources = null;

    public MultiInitializer(MiResultSet resultSet, List<EntityInitializer<Entity>> readers) {
        this.resultSet = resultSet;
        this.initializers = readers;
    }

    public MultiInitializer(MiResultSet resultSet) {
        this.resultSet = resultSet;
        this.initializers = new ArrayList<>();
    }
    
    public MultiInitializer<Entity> complete(Completable oc) {
        if (listeners == null) listeners = new ArrayList<>();
        listeners.add(oc);
        return this;
    }
    
    public MultiInitializer<Entity> close(AutoCloseable ac) {
        if (resources == null) resources = new ArrayList<>();
        resources.add(ac);
        return this;
    }
    
    public <C extends AutoCloseable & Completable> MultiInitializer<Entity> completeAndClose(C c) {
        return complete(c).close(c);
    }

    public MultiInitializer<Entity> addAll(Iterable<? extends EntityConfiguration<Entity>> configurations) throws MiException {
        for (EntityConfiguration<Entity> c: configurations) {
            add(c);
        }
        return this;
    }

    public MultiInitializer<Entity> add(EntityConfiguration<Entity> configuration) throws MiException {
        EntityInitializer<Entity> re = configuration.newInitializer(resultSet);
        if (re != null) initializers.add(re);
        return this;
    }
    
    @Override
    public void apply(Entity entity) throws MiException {
        for (EntityInitializer<Entity> ei: initializers) {
            ei.apply(entity);
        }
    }

    @Override
    public void complete() throws MiException {
        if (listeners != null) {
            Closeables.completeAll(MiException.class, listeners);
        }
    }

    @Override
    public void close() throws MiException {
        MultiInitializer.this.complete();
        if (resources != null) {
            Closeables.closeAll(MiException.class, resources);
        }
    }

    @Override
    public String toString() {
        return "-> " + initializers.toString();
    }
}
