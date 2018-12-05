package org.cthul.miro.entity.map;

import org.cthul.miro.domain.Repository;
import java.util.*;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.*;

/**
 *
 * @param <Entity>
 */
public interface EntityProperties<Entity> {
    
    EntityProperties<Entity> select(Collection<String> fields);
    
    default EntityProperties<Entity> select(String... fields) {
        return select(Arrays.asList(fields));
    }
    
    void newReader(Repository repository, MiResultSet resultSet, InitializationBuilder<? extends Entity> initBuilder) throws MiException;
    
    default EntityConfiguration<Entity> read(Repository repository) {
        return new EntityConfiguration<Entity>() {
            @Override
            public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Entity> builder) throws MiException {
                EntityProperties.this.newReader(repository, resultSet, builder);
            }
            @Override
            public String toString() {
                return EntityProperties.this.toString();
            }
        };
    }
    
    default EntityConfiguration<Entity> read(Repository repository, String... fields) {
        return select(fields).read(repository);
    }
    
    default EntityConfiguration<Entity> read(Repository repository, Collection<String> fields) {
        return select(fields).read(repository);
    }
    
//    void newInitializer(MiResultSet rs, Cnn cnn, InitializationBuilder<? extends Entity> builder) throws MiException;
//    
//    default void newInitializer(MiResultSet rs, Cnn cnn, List<String> fields, InitializationBuilder<? extends Entity> builder) throws MiException {
//        select(fields).newInitializer(rs, cnn, builder);
//    }
    
//    static <E, Cnn> PropertiesConfiguration<E, Cnn> build() {
//        return build(null);
//    }
//    
//    static <E, Cnn> PropertiesConfiguration<E, Cnn> build(Class<E> clazz) {
//        return new PropertiesConfiguration<>(clazz);
//    }
}
