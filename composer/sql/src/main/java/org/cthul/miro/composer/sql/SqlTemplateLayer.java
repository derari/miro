package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.StatementHolder;
import static org.cthul.miro.composer.sql.SqlTemplates.TEMPLATE_ADAPTER;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.view.composer.LayerBuilder;
import org.cthul.miro.view.composer.SimpleLayerBuilder;

/**
 *
 * @param <This>
 */
public interface SqlTemplateLayer<This extends SqlTemplateLayer<This>>
                extends SqlTemplatesBuilder<This>,
                        SimpleLayerBuilder<SqlFilterableClause, SelectQueryBuilder, SqlFilterableClause, SqlFilterableClause> {
    
    default LayerBuilder<
            StatementHolder<? extends SqlFilterableClause>,
            StatementHolder<? extends SelectQueryBuilder>,
            StatementHolder<? extends SqlFilterableClause>,
            StatementHolder<? extends SqlFilterableClause>,
            SqlFilterableClause,
            SelectQueryBuilder,
            SqlFilterableClause,
            SqlFilterableClause> forStatementHolder() {
        return () -> TEMPLATE_ADAPTER.wrap(SqlTemplateLayer.this.asLayer());
    }
}
