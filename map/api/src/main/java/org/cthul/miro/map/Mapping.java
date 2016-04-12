package org.cthul.miro.map;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.map.impl.GenericMappingLayer;

/**
 *
 */
public interface Mapping<Entity> {
    
    void configureWith(EntityConfiguration<? super Entity> config);
    
    default void initializeWith(EntityInitializer<? super Entity> init) {
        configureWith(EntityTypes.asConfiguration(init));
    }
    
    class Key<Entity> implements org.cthul.miro.util.Key<Mapping<Entity>> {
        private static final Key INSTANCE = new Key();
    }
    
    static <Entity> Key<Entity> key() {
        return Key.INSTANCE;
    }
    
    static <Entity> GenericMappingLayer<Entity> newLayer() {
        return new GenericMappingLayer<>();
    }
}
