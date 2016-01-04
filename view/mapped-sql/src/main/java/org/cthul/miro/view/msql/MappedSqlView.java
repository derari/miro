package org.cthul.miro.view.msql;

import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.view.composer.CRUDStatementFactory;
import org.cthul.miro.view.composer.CRUDTemplates;
import org.cthul.miro.view.impl.SimpleCrudView;

/**
 *
 */
public class MappedSqlView<Entity, C, R, U, D> 
                extends SimpleCrudView<
                                MappedStatementBuilder<Entity, ? extends SqlFilterableClause>,
                                MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>,
                                MappedStatementBuilder<Entity, ? extends SqlFilterableClause>,
                                MappedStatementBuilder<Entity, ? extends SqlFilterableClause>,
                                C, R, U, D> {

    public MappedSqlView(CRUDStatementFactory<MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, ? extends MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, C, R, U, D> factory, 
                         CRUDTemplates<? super MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, ? super MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, ? super MappedStatementBuilder<Entity, ? extends SqlFilterableClause>, ? super MappedStatementBuilder<Entity, ? extends SqlFilterableClause>> templates) {
        super(factory, templates);
    }
    
    public static <Entity> MappedSqlViewBuilder<Entity, ?, ?, ?, ?> builder(GraphSchema schema, Class<Entity> entityClass) {
        return new MappedSqlViewBuilder<>(schema, entityClass);
    }
    
    public static <Entity> MappedSqlViewBuilder<Entity, ?, ?, ?, ?> builder(Class<Entity> entityClass) {
        return new MappedSqlViewBuilder<>(entityClass);
    }
    
}
