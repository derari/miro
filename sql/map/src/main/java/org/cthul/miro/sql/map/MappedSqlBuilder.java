package org.cthul.miro.sql.map;

import org.cthul.miro.sql.template.SqlTemplatesBuilder;
import org.cthul.miro.entity.map.EntityAttributesBuilder;
import org.cthul.miro.graph.TypeBuilder;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public interface MappedSqlBuilder<Entity, This extends MappedSqlBuilder<Entity, This>>
                 extends SqlTemplatesBuilder<This>, TypeBuilder<Entity, This> {

}
