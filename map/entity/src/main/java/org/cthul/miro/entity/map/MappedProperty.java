package org.cthul.miro.entity.map;

import java.util.Collection;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.*;

/**
 *
 * @param <Entity>
 */
public interface MappedProperty<Entity> {
    
    String getKey();

    Object get(Entity e);
    
    void set(Entity e, Object value) throws MiException;
    
    ColumnMapping getMapping();
    
//    MappedProperty<Entity> nested(Collection<?> attributes);
    
    default Object[] writeProperty(Entity e, int index, Object[] target) {
        return getMapping().writeColumns(get(e), index, target);
    }

    default void newInitializer(Repository repository, MiResultSet resultSet, InitializationBuilder<? extends Entity> builder) throws MiException {
        EntityFactory<Object> valueReader = builder.nestedFactory(fb -> getMapping().newValueReader(repository, resultSet, fb));
        builder.addName(MappedProperty.this + " := " + valueReader)
            .addInitializer(entity -> set(entity, valueReader.newEntity()));
    }
    
    MappedProperty<Entity> nested(Collection<?> properties);
    
//    static <E, Cnn> SimpleProperty.Builder<E, Cnn> build() {
//        return build(null);
//    }
//    
//    static <E, Cnn> SimpleProperty.Builder<E, Cnn> build(Class<E> clazz) {
//        return new SimpleProperty.Builder<>(clazz, null);
//    }
}
