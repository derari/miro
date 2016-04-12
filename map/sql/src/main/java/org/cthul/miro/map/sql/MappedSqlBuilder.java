package org.cthul.miro.map.sql;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.composer.sql.SqlAttribute;
import org.cthul.miro.composer.sql.SqlTemplatesBuilder;
import org.cthul.miro.map.MappingBuilder;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public interface MappedSqlBuilder<Entity, This extends MappedSqlBuilder<Entity, This>>
                 extends SqlTemplatesBuilder<This>, MappingBuilder<Entity, This> {

    default <F> This attribute(ResultScope scope, boolean key, SqlAttribute attribute, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
        field(attribute.getKey(), getter, setter);
        return attribute(scope, key, attribute);
    }
//
//    default <F> This attribute(ResultScope scope, boolean key, String id, String attribute, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
//        field(id, getter, setter);
//        return attribute(scope, key, id, attribute);
//    }
//    
//    default <F> This attribute(ResultScope scope, String id, String attribute, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
//        return attribute(scope, false, id, attribute, getter, setter);
//    }
//    
//    default <F> This keyAttribute(ResultScope scope, String id, String attribute, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
//        return attribute(scope, true, id, attribute, getter, setter);
//    }
}
