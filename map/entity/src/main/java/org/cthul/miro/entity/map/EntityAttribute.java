package org.cthul.miro.entity.map;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.InitializationBuilder;
import org.cthul.miro.util.XConsumer;

/**
 *
 * @param <Entity>
 * @param <Cnn>
 */
public interface EntityAttribute<Entity, Cnn> extends ColumnMapping<Cnn> {
    
    String getKey();
    
    @Override
    List<String> getColumns();
    
    @Override
    Object[] toColumns(Object value, Object[] result);
    
    @Override
    EntityFactory<?> newValueReader(MiResultSet rs, Cnn cnn) throws MiException;
    
    Object get(Entity e);
    
    void set(Entity e, Object value) throws MiException;

    public default void newInitializer(MiResultSet resultSet, Cnn cnn, InitializationBuilder<? extends Entity> builder) throws MiException {
        EntityFactory<?> reader = newValueReader(resultSet, cnn);
        if (reader == null) return;
        builder.addCompleteAndClose(reader);
        builder.addNamedInitializer(new XConsumer<Entity, MiException>() {
            @Override
            public void accept(Entity entity) throws MiException {
                Object value = reader.newEntity();
                set(entity, value);
            }
            @Override
            public String toString() {
                return EntityAttribute.this + " := " + reader;
            }
        });
    }
    
    static <E, Cnn> SimpleAttribute.Builder<E, Cnn> build() {
        return build(null);
    }
    
    static <E, Cnn> SimpleAttribute.Builder<E, Cnn> build(Class<E> clazz) {
        return new SimpleAttribute.Builder<>(clazz, null);
    }
}
