package org.cthul.miro.entity.map;

import org.cthul.miro.entity.base.ResultColumns;

/**
 * Base class for configuring a mapping from result columns to entity attributes.
 * @param <Entity>
 * @param <This>
 */
public interface EntityAttributesBuilder<Entity, This> extends EntityAttributeBuilder<Entity, This> {
    
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
    This add(EntityAttribute<Entity> attribute);
    
    @Override
    default EntityAttributeBuilder<Entity, This> as(String key) {
        return new NewField<>(this, entityClass(), key);
    }

    @Override
    default Single<Entity, This> column(ResultColumns.ColumnRule rule, String column) {
        return as(null).column(rule, column);
    }

    @Override
    default Group<Entity, This> columns(ResultColumns.ColumnRule allRule, ResultColumns.ColumnRule eachRule, String... columns) {
        return as(null).columns(allRule, eachRule, columns);
    }

    public class NewField<Entity, Mapping> extends SimpleAttributeBuilder<Entity, Mapping> {
        
        private final EntityAttributesBuilder<Entity, Mapping> mapping;

        public NewField(EntityAttributesBuilder<Entity, Mapping> mapping, Class<Entity> clazz, String key) {
            super(clazz, key);
            this.mapping = mapping;
        }

        @Override
        protected Mapping build(EntityAttribute<Entity> field) {
            return mapping.add(field);
        }
    }

    interface Delegator<Entity, This extends EntityAttributesBuilder<Entity, This>> extends EntityAttributesBuilder<Entity, This> {

        EntityAttributesBuilder<Entity, ?> internalEntityFieldsBuilder();

        @Override
        default Class<Entity> entityClass() {
            return internalEntityFieldsBuilder().entityClass();
        }

        @Override
        public default This add(EntityAttribute<Entity> field) {
            internalEntityFieldsBuilder().add(field);
            return (This) this;
        }
    }
}
