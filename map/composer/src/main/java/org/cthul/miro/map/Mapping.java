package org.cthul.miro.map;

import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.entity.EntityTemplate;

/**
 *
 */
public interface Mapping<Entity> {
    
    void setTemplate(EntityTemplate<Entity> type);
    
    void configureWith(EntityConfiguration<? super Entity> config);
    
    default void initializeWith(EntityInitializer<? super Entity> init) {
        configureWith(Entities.asConfiguration(init));
    }
}
