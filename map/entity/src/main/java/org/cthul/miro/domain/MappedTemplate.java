package org.cthul.miro.domain;

import java.util.Collection;
import org.cthul.miro.entity.EntityTemplate;
import static java.util.Arrays.asList;

/**
 *
 */
public interface MappedTemplate<Entity> extends EntityTemplate<Entity> {
    
    MappedTemplate<Entity> andLoad(Collection<?> properties);
    
    default MappedTemplate<Entity> andLoad(Object... properties) {
        return andLoad(asList(properties));
    }
    
    MappedTemplate<Entity> andRead(Collection<?> properties);
    
    default MappedTemplate<Entity> andRead(Object... properties) {
        return andRead(asList(properties));
    }
}
