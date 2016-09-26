package org.cthul.miro.sql.set;

import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.sql.template.SqlTemplatesBuilder;
import org.cthul.miro.graph.TypeBuilder;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public interface MappedSqlBuilder<Entity, This extends MappedSqlBuilder<Entity, This>>
                 extends SqlTemplatesBuilder<This>, TypeBuilder<Entity, GraphApi, This> {

}
