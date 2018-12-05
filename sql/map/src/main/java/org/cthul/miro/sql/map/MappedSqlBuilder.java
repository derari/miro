package org.cthul.miro.sql.map;

import org.cthul.miro.entity.map.TypeBuilder;
import org.cthul.miro.sql.composer.SqlTemplatesBuilder;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public interface MappedSqlBuilder<Entity, This extends MappedSqlBuilder<Entity, This>>
                 extends SqlTemplatesBuilder<This>, TypeBuilder<Entity, This> {

}
