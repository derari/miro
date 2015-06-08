package org.cthul.miro.entity.base;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.util.Closables;
import org.cthul.miro.util.Completable;

/**
 * Configures an entity with columns from a result set.
 * @param <Entity>
 */
public class AttributeReader<Entity> extends AttributeMappingBase<Entity, MiException, AttributeReader<Entity>> implements EntityInitializer<Entity> {

    private final MiResultSet resultSet;
    private final List<AttributeMapping.ReaderEntry<Entity>> readers;
    private List<Completable> listeners = null;
    private List<AutoCloseable> resources = null;

    AttributeReader(MiResultSet resultSet, List<ReaderEntry<Entity>> readers) {
        this.resultSet = resultSet;
        this.readers = readers;
    }

    public AttributeReader(MiResultSet resultSet) {
        this.resultSet = resultSet;
        this.readers = new ArrayList<>();
    }
    
    public AttributeReader<Entity> complete(Completable oc) {
        if (listeners == null) listeners = new ArrayList<>();
        listeners.add(oc);
        return this;
    }
    
    public AttributeReader<Entity> close(AutoCloseable ac) {
        if (resources == null) resources = new ArrayList<>();
        resources.add(ac);
        return this;
    }
    
    public <C extends AutoCloseable & Completable> AttributeReader<Entity> completeAndClose(C c) {
        return complete(c).close(c);
    }

    @Override
    protected AttributeReader<Entity> add(MappingEntry<Entity> entry) throws MiException {
        ReaderEntry<Entity> re = entry.newReader(resultSet);
        if (re != null) readers.add(re);
        return this;
    }
    
    @Override
    public void apply(Entity entity) throws MiException {
        for (AttributeMapping.ReaderEntry<Entity> re: readers) {
            re.apply(entity);
        }
    }

    @Override
    public void complete() throws MiException {
        if (listeners != null) {
            Closables.completeAll(MiException.class, listeners);
        }
    }

    @Override
    public void close() throws MiException {
        AttributeReader.this.complete();
        if (resources != null) {
            Closables.closeAll(MiException.class, resources);
        }
    }

    @Override
    public String toString() {
        return "-> " + readers.toString();
    }
}
