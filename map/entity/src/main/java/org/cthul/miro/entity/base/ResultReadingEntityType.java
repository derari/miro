package org.cthul.miro.entity.base;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.FactoryBuilder;

/**
 * Creates entities based on columns of a result set.
 * Optionally applies a configuration.
 * @param <Entity>
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
    public void newFactory(MiResultSet rs, FactoryBuilder<? super Entity> builder) throws MiException {
        int[] columns = findColumns(rs);
        FactoryBuilder<Entity> b = builder.setFactory(() -> this.newEntity(rs, columns));
        b.addName("new " + getShortString());
        if (configuration != null) {
            configuration.newInitializer(rs, b);
        }
    }
    
    protected abstract int[] findColumns(MiResultSet rs) throws MiException;
    
    protected abstract Entity newEntity(MiResultSet rs, int[] indices) throws MiException;

    @Override
    public String toString() {
        if (configuration == null) {
            return getShortString();
        }
        return getShortString() + " with " + configuration.toString();
    }
    
    protected String getShortString() {
        return super.toString();
    }
}
