package org.cthul.miro.map;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public interface MappingBuilder<Entity, This extends MappingBuilder<Entity, This>> {

    <F> This field(String id, Function<Entity, F> getter, BiConsumer<Entity, F> setter);
}
