package org.cthul.miro.entity.map;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityTypes;

/**
 *
 * @param <Entity>
 */
public interface EntityAttribute<Entity> extends EntityConfiguration<Entity>, ColumnValue {
    
    String getKey();
    
    @Override
    List<String> getColumns();
    
    @Override
    Object[] toColumns(Object value, Object[] result);
    
    @Override
    EntityFactory<?> newValueReader(MiResultSet rs) throws MiException;
    
    Object get(Entity e);
    
    void set(Entity e, Object value) throws MiException;

    @Override
    public default EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException {
        EntityFactory<?> reader = newValueReader(resultSet);
        if (reader == null) return EntityTypes.noInitialization();
        return new EntityInitializer<Entity>() {
            @Override
            public void apply(Entity entity) throws MiException {
                Object value = reader.newEntity();
                set(entity, value);
            }
            @Override
            public void complete() throws MiException {
                reader.complete();
            }
            @Override
            public void close() throws MiException {
                reader.close();
            }
        };
    }
    
    static <E> SimpleAttribute.Builder<E> build() {
        return build(null);
    }
    
    static <E> SimpleAttribute.Builder<E> build(Class<E> clazz) {
        return new SimpleAttribute.Builder<>(clazz, null);
    }
}
