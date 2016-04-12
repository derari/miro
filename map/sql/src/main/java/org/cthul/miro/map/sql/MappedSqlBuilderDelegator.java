package org.cthul.miro.map.sql;

import org.cthul.miro.composer.sql.SqlTemplatesBuilder;
import org.cthul.miro.composer.sql.SqlTemplatesBuilderDelegator;
import org.cthul.miro.map.MappingBuilder;
import org.cthul.miro.map.MappingBuilderDelegator;

/**
 *
 */
public interface MappedSqlBuilderDelegator<Entity, This extends MappedSqlBuilder<Entity, This>> 
                 extends MappedSqlBuilder<Entity, This>,
                         SqlTemplatesBuilderDelegator<This>,
                         MappingBuilderDelegator<Entity, This> {
    
    MappedSqlBuilder<Entity,?> internalMappedSqlTemplatesBuilder();

    @Override
    default MappingBuilder<Entity, ?> internalMappingBuilder() {
        return internalMappedSqlTemplatesBuilder();
    }

    @Override
    default SqlTemplatesBuilder<?> internalSqlTemplatesBuilder() {
        return internalMappedSqlTemplatesBuilder();
    }
}
