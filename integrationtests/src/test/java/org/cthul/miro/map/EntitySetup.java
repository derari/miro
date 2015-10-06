package org.cthul.miro.map;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityTypes;

/**
 *
 */
public interface EntitySetup<Entity> {
    
    void configureWith(EntityConfiguration<? super Entity> config);
    
    default void initializeWith(EntityInitializer<? super Entity> init) {
        configureWith(EntityTypes.asConfiguration(init));
    }
}
