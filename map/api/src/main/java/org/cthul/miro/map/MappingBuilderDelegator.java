package org.cthul.miro.map;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 *
 */
public interface MappingBuilderDelegator<Entity, This extends MappingBuilder<Entity, This>>
                 extends MappingBuilder<Entity, This> {

    MappingBuilder<Entity, ?> internalMappingBuilder();
    
    @Override
    default <F> This field(String id, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
        internalMappingBuilder().field(id, getter, setter);
        return (This) this;
    }
}
