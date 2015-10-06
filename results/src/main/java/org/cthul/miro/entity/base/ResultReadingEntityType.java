package org.cthul.miro.entity.base;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 * Creates entities based on columns of a result set.
 * Optionally applies a configuration.
 */
public abstract class ResultReadingEntityType<Entity> implements EntityType<Entity> {
    
    private final EntityConfiguration<Entity> configuration;

    public ResultReadingEntityType() {
        this.configuration = null;
    }

    public ResultReadingEntityType(EntityConfiguration<Entity> configuration) {
        this.configuration = configuration;
    }

    @Override
    public EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException {
        EntityFactory<Entity> f = new Factory(rs);
        if (configuration != null) {
            f = f.with(configuration.newInitializer(rs));
        }
        return f;
    }
    
    protected abstract int[] findColumns(MiResultSet rs) throws MiException;
    
    protected abstract Entity newEntity(MiResultSet rs, int[] indices) throws MiException;

    @Override
    public String toString() {
        if (configuration == null) {
            return super.toString();
        }
        return super.toString() + " with " + configuration.toString();
    }
    
    protected String getShortString() {
        return super.toString();
    }
    
    protected class Factory implements EntityFactory<Entity> {
        
        private final MiResultSet rs;
        private final int[] columns;

        public Factory(MiResultSet rs) throws MiException {
            this.rs = rs;
            columns = findColumns(rs);
        }

        @Override
        public Entity newEntity() throws MiException {
            return ResultReadingEntityType.this.newEntity(rs, columns);
        }
        
        @Override
        public void complete() throws MiException {
        }

        @Override
        public void close() throws MiException {
        }

        @Override
        public String toString() {
            return "new " + getShortString();
        }
    }
}
