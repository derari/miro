package org.cthul.miro.entity.map;

import org.cthul.miro.entity.base.ResultColumns;

/**
 * Base class for configuring a mapping from result columns to entity attributes.
 * @param <Entity>
 * @param <Cnn>
 * @param <This>
 */
public interface EntityAttributesBuilder<Entity, Cnn, This> extends EntityAttributeBuilder<Entity, Cnn, This> {
    
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
    This add(EntityAttribute<Entity, Cnn> attribute);
    
    @Override
    default EntityAttributeBuilder<Entity, Cnn, This> as(String key) {
        return new NewField<>(this, entityClass(), key);
    }

    @Override
    default Single<Entity, Cnn, This> column(ResultColumns.ColumnRule rule, String column) {
        return as(null).column(rule, column);
    }

    @Override
    default Group<Entity, Cnn, This> columns(ResultColumns.ColumnRule allRule, ResultColumns.ColumnRule eachRule, String... columns) {
        return as(null).columns(allRule, eachRule, columns);
    }

    public class NewField<Entity, Cnn, Mapping> extends SimpleAttributeBuilder<Entity, Cnn, Mapping> {
        
        private final EntityAttributesBuilder<Entity, Cnn, Mapping> mapping;

        public NewField(EntityAttributesBuilder<Entity, Cnn, Mapping> mapping, Class<Entity> clazz, String key) {
            super(clazz, key);
            this.mapping = mapping;
        }

        @Override
        protected Mapping build(EntityAttribute<Entity, Cnn> field) {
            return mapping.add(field);
        }
    }

    interface Delegator<Entity, Cnn, This extends EntityAttributesBuilder<Entity, Cnn, This>> extends EntityAttributesBuilder<Entity, Cnn, This> {

        EntityAttributesBuilder<Entity, Cnn, ?> internalEntityFieldsBuilder();

        @Override
        default Class<Entity> entityClass() {
            return internalEntityFieldsBuilder().entityClass();
        }

        @Override
        public default This add(EntityAttribute<Entity, Cnn> field) {
            internalEntityFieldsBuilder().add(field);
            return (This) this;
        }
    }
}
