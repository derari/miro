package org.cthul.miro.entity.map;

import java.util.*;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.InitializationBuilder;

/**
 *
 * @param <Entity>
 * @param <Cnn>
 */
public interface EntityProperties<Entity, Cnn> {
    
    EntityProperties<Entity, Cnn> select(Collection<String> fields);
    
    default EntityProperties<Entity, Cnn> select(String... fields) {
        return select(Arrays.asList(fields));
    }
    
    default EntityConfiguration<Entity> newConfiguration(Cnn cnn) {
        return new EntityConfiguration<Entity>() {
            @Override
            public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Entity> builder) throws MiException {
                EntityProperties.this.newInitializer(resultSet, cnn, builder);
            }
            @Override
            public String toString() {
                return EntityProperties.this.toString();
            }
        };
    }
    
    default EntityConfiguration<Entity> newConfiguration(Cnn cnn, String... fields) {
        return select(fields).newConfiguration(cnn);
    }
    
    default EntityConfiguration<Entity> newConfiguration(Cnn cnn, Collection<String> fields) {
        return select(fields).newConfiguration(cnn);
    }
    
    void newInitializer(MiResultSet rs, Cnn cnn, InitializationBuilder<? extends Entity> builder) throws MiException;
    
    default void newInitializer(MiResultSet rs, Cnn cnn, List<String> fields, InitializationBuilder<? extends Entity> builder) throws MiException {
        select(fields).newInitializer(rs, cnn, builder);
    }
    
    static <E, Cnn> PropertiesConfiguration<E, Cnn> build() {
        return build(null);
    }
    
    static <E, Cnn> PropertiesConfiguration<E, Cnn> build(Class<E> clazz) {
        return new PropertiesConfiguration<>(clazz);
    }
}
