package org.cthul.miro.entity;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;

/**
 *
 * @param <Entity>
 */
public interface AttributeConfiguration<Entity> {
    
    default EntityConfiguration<Entity> forAttributes(List<?> attributes) {
        List<Object> list = new ArrayList<>(attributes);
        return new EntityConfiguration<Entity>() {
            @Override
            public EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException {
                return AttributeConfiguration.this.newInitializer(resultSet, list);
            }
            @Override
            public String toString() {
                return AttributeConfiguration.this.toString() + list;
            }
        };
    }
    
    EntityInitializer<Entity> newInitializer(MiResultSet rs, List<?> attributes) throws MiException;
    
    default EntityConfiguration<Entity> all() {
        return new All<>(this);
    }
    
    class All<E> implements EntityConfiguration<E> {

        final AttributeConfiguration<E> aCfg;

        public All(AttributeConfiguration<E> aCfg) {
            this.aCfg = aCfg;
        }

        @Override
        public EntityInitializer<E> newInitializer(MiResultSet resultSet) throws MiException {
            return aCfg.newInitializer(resultSet, resultSet.listColumns());
        }
    }
}
