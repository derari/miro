package org.cthul.miro.entity.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;

/**
 *
 * @param <Entity>
 */
public interface EntityAttributes<Entity> {
    
    default EntityConfiguration<Entity> newConfiguration(Collection<String> fields) {
        List<String> list = new ArrayList<>(fields);
        return new EntityConfiguration<Entity>() {
            @Override
            public EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException {
                return EntityAttributes.this.newInitializer(resultSet, list);
            }
            @Override
            public String toString() {
                return EntityAttributes.this.toString() + list;
            }
        };
    }
    
    EntityInitializer<Entity> newInitializer(MiResultSet rs, List<String> fields) throws MiException;
    
    static <E> AttributesConfiguration<E> build() {
        return build(null);
    }
    
    static <E> AttributesConfiguration<E> build(Class<E> clazz) {
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
