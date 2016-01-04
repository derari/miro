package org.cthul.miro.map.sql;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.composer.sql.SqlTemplatesBuilder;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.map.MappingBuilder;
import org.cthul.miro.view.composer.SimpleLayerBuilder;

/**
 *
 * @param <Entity>
 */
public interface MappedSqlTemplatesBuilder<Entity>
                extends SqlTemplatesBuilder<MappedSqlTemplatesBuilder<Entity>>,
                        MappingBuilder<Entity, MappedSqlTemplatesBuilder<Entity>>,
                        SimpleLayerBuilder<
                                MappedStatementBuilder<Entity, ? extends SqlFilterableClause>,
                                MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>,
                                MappedStatementBuilder<Entity, ? extends SqlFilterableClause>,
                                MappedStatementBuilder<Entity, ? extends SqlFilterableClause>> {

    default <F> MappedSqlTemplatesBuilder<Entity> attribute(ResultScope scope, boolean key, String id, String attribute, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
        field(id, getter, setter);
        return attribute(scope, key, id, attribute);
    }
    
    default <F> MappedSqlTemplatesBuilder<Entity> attribute(ResultScope scope, String id, String attribute, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
        return attribute(scope, false, id, attribute, getter, setter);
    }
    
    default <F> MappedSqlTemplatesBuilder<Entity> keyAttribute(ResultScope scope, String id, String attribute, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
        return attribute(scope, true, id, attribute, getter, setter);
    }
}
