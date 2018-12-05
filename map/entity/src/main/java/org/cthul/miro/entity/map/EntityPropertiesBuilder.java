package org.cthul.miro.entity.map;

import org.cthul.miro.entity.map.MappedPropertyBuilder.Group;
import org.cthul.miro.entity.map.MappedPropertyBuilder.Single;

/**
 * Base class for configuring a mapping from result columns to entity properties.
 * @param <Entity>
 * @param <This>
 */
public interface EntityPropertiesBuilder<Entity, This> {
    
    /**
     * Optional operation.
     * @return entity class
     */
    Class<Entity> entityClass();
    
    /**
     * Adds an entry to the mapping.
     * @param attribute
     * @return this 
     */
    This add(MappedProperty<Entity> attribute);
    
    default ColumnMappingBuilder<Entity, Single<Entity, This>, Group<Entity, This>> property(String key) {
        return new NewProperty<>(this, entityClass(), key);
    }
    
//    @Override
//    default MappedPropertyBuilder<Entity, This> property(String key) {
//        return new NewProperty<>(this, entityClass(), key);
//    }

    class NewProperty<Entity, Parent> extends SimpleMappedPropertyBuilder<Entity, Parent> {
        
        private final EntityPropertiesBuilder<Entity, Parent> mapping;

        public NewProperty(EntityPropertiesBuilder<Entity, Parent> mapping, Class<Entity> clazz, String key) {
            super(clazz, key);
            this.mapping = mapping;
        }

        @Override
        protected Parent build(MappedProperty<Entity> field) {
            return mapping.add(field);
        }
    }

    interface Delegator<Entity, This extends EntityPropertiesBuilder<Entity, This>> extends EntityPropertiesBuilder<Entity, This> {

        EntityPropertiesBuilder<Entity, ?> internalPropertiesBuilder();

        @Override
        default Class<Entity> entityClass() {
            return internalPropertiesBuilder().entityClass();
        }

        @Override
        public default This add(MappedProperty<Entity> field) {
            internalPropertiesBuilder().add(field);
            return (This) this;
        }
    }
}
