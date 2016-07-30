package org.cthul.miro.entity.map;

import java.util.*;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;

/**
 *
 * @param <Entity>
 * @param <Cnn>
 */
public interface EntityAttributes<Entity, Cnn> {
    
    default EntityConfiguration<Entity> newConfiguration(Cnn cnn) {
        return new EntityConfiguration<Entity>() {
            @Override
            public EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException {
                return EntityAttributes.this.newInitializer(resultSet, cnn);
            }
            @Override
            public String toString() {
                return EntityAttributes.this.toString();
            }
        };
    }
    
    
    default EntityConfiguration<Entity> newConfiguration(Cnn cnn, String... fields) {
        return newConfiguration(cnn, Arrays.asList(fields));
    }
    
    default EntityConfiguration<Entity> newConfiguration(Cnn cnn, Collection<String> fields) {
        List<String> list = new ArrayList<>(fields);
        return new EntityConfiguration<Entity>() {
            @Override
            public EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException {
                return EntityAttributes.this.newInitializer(resultSet, cnn, list);
            }
            @Override
            public String toString() {
                return EntityAttributes.this.toString() + list;
            }
        };
    }
    
    EntityInitializer<Entity> newInitializer(MiResultSet rs, Cnn cnn) throws MiException;
    
    EntityInitializer<Entity> newInitializer(MiResultSet rs, Cnn cnn, List<String> fields) throws MiException;
    
    static <E, Cnn> AttributesConfiguration<E, Cnn> build() {
        return build(null);
    }
    
    static <E, Cnn> AttributesConfiguration<E, Cnn> build(Class<E> clazz) {
        return new AttributesConfiguration<>(clazz);
    }
    
//    default EntityConfiguration<Entity> star() {
//        return new Star<>(this);
//    }
//    
//    class Star<E> implements EntityConfiguration<E> {
//
//        final EntityAttributes<E> aCfg;
//
//        public Star(EntityAttributes<E> aCfg) {
//            this.aCfg = aCfg;
//        }
//
//        @Override
//        public EntityInitializer<E> newInitializer(MiResultSet resultSet) throws MiException {
//            return aCfg.newInitializer(resultSet, resultSet.listColumns());
//        }
//    }
}
