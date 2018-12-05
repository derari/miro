package org.cthul.miro.domain;

import java.util.Collection;
import org.cthul.miro.entity.*;
import static java.util.Arrays.asList;

/**
 *
 * @param <Entity>
 */
public interface EntitySet<Entity> {
    
    MappedTemplate<Entity> getLookUp();
    
    MappedSelector<Entity> getSelector();

    EntityConfiguration<Entity> readProperties(Collection<?> properties);

    void loadProperties(Collection<?> properties, InitializationBuilder<Entity> initBuilder);
    
//    default EntitySelector<Entity> getSelector(Collection<?> properties) {
//        return getSelector().andLoad(properties);
//    }
//    
//    default EntitySelector<Entity> getSelector(Object... properties) {
//        return getSelector(asList(properties));
//    }
//    
//    default EntityTemplate<Entity> getLookUp(Collection<?> properties) {
//        return getLookUp().andLoad(properties);
//    }
//    
//    default EntityTemplate<Entity> getLookUp(Object... properties) {
//        return getLookUp(asList(properties));
//    }
}
