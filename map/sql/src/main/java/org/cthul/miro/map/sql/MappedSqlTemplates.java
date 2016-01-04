package org.cthul.miro.map.sql;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.sql.SqlTemplates;
import org.cthul.miro.composer.sql.SqlTemplateLayer;
import org.cthul.miro.composer.sql.SqlTemplatesBuilderDelegator;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.map.impl.MappingTemplates;
import org.cthul.miro.view.composer.CRUDTemplateLayer;

/**
 *
 * @param <Entity>
 */
public class MappedSqlTemplates<Entity>
                extends SqlTemplatesBuilderDelegator<MappedSqlTemplatesBuilder<Entity>>
                implements MappedSqlTemplatesBuilder<Entity> {

    private final MappingTemplates<Entity> mappingBuilder;
    private final SqlTemplateLayer sqlBuilder;
    
    public MappedSqlTemplates() {
        this(new MappingTemplates<>(), new SqlTemplates());
    }

    public MappedSqlTemplates(MappingTemplates<Entity> mappingBuilder, SqlTemplateLayer sqlBuilder) {
        super(sqlBuilder);
        this.mappingBuilder = mappingBuilder;
        this.sqlBuilder = sqlBuilder;
    }

    public SqlTemplateLayer getSqlBuilder() {
        return sqlBuilder;
    }

    public MappingTemplates<Entity> getMappingBuilder() {
        return mappingBuilder;
    }

    @Override
    public CRUDTemplateLayer<MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>> asLayer() {
        return mappingBuilder.asLayer().wrap(sqlBuilder.forStatementHolder().asLayer());
    }

    @Override
    public <F> MappedSqlTemplatesBuilder<Entity> field(String id, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
        mappingBuilder.field(id, getter, setter);
        return this;
    }
}
