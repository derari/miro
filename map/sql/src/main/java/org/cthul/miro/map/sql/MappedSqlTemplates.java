package org.cthul.miro.map.sql;

import org.cthul.miro.composer.sql.SqlTemplatesBuilder;
import org.cthul.miro.composer.sql.SqlTemplatesBuilderDelegator;
import org.cthul.miro.composer.sql.template.SqlTemplates;
import org.cthul.miro.map.MappingBuilder;
import org.cthul.miro.map.MappingBuilderDelegator;
import org.cthul.miro.map.impl.MappedTemplates;

/**
 *
 * @param <Entity>
 */
public class MappedSqlTemplates<Entity>
        implements MappedSqlBuilder<Entity, MappedSqlTemplates<Entity>>,
                   SqlTemplatesBuilderDelegator<MappedSqlTemplates<Entity>>,
                   MappingBuilderDelegator<Entity, MappedSqlTemplates<Entity>> {

    private final MappedTemplates<Entity> mappingBuilder;
    private final SqlTemplates sqlBuilder;
    
    public MappedSqlTemplates() {
        this("MAIN");
    }
    
    public MappedSqlTemplates(String name) {
        this(new MappedTemplates<>(), new SqlTemplates(name));
    }

    public MappedSqlTemplates(MappedTemplates<Entity> mappingBuilder, SqlTemplates sqlBuilder) {
        this.mappingBuilder = mappingBuilder;
        this.sqlBuilder = sqlBuilder;
    }

    @Override
    public SqlTemplatesBuilder<?> internalSqlTemplatesBuilder() {
        return sqlBuilder;
    }

    @Override
    public MappingBuilder<Entity, ?> internalMappingBuilder() {
        return mappingBuilder;
    }

    public MappedTemplates<Entity> getMappingBuilder() {
        return mappingBuilder;
    }

    public SqlTemplates getSqlBuilder() {
        return sqlBuilder;
    }
}
