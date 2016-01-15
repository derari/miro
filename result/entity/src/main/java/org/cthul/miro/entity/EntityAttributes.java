package org.cthul.miro.entity;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;

/**
 *
 * @param <Entity>
 */
public interface EntityAttributes<Entity> {
    
    default EntityConfiguration<Entity> newConfiguration(List<?> attributes) {
        List<Object> list = new ArrayList<>(attributes);
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
    
    EntityInitializer<Entity> newInitializer(MiResultSet rs, List<?> attributes) throws MiException;
    
    default EntityConfiguration<Entity> star() {
        return new Star<>(this);
    }
    
    class Star<E> implements EntityConfiguration<E> {

        final EntityAttributes<E> aCfg;

        public Star(EntityAttributes<E> aCfg) {
            this.aCfg = aCfg;
        }

        @Override
        public EntityInitializer<E> newInitializer(MiResultSet resultSet) throws MiException {
            return aCfg.newInitializer(resultSet, resultSet.listColumns());
        }
    }
}
