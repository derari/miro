package org.cthul.miro.domain;

import java.util.Collection;
import org.cthul.miro.entity.EntitySelector;
import static java.util.Arrays.asList;

/**
 *
 */
public interface MappedSelector<Entity> extends EntitySelector<Entity> {
    
    MappedSelector<Entity> andLoad(Collection<?> properties);
    
    default MappedSelector<Entity> andLoad(Object... properties) {
        return andLoad(asList(properties));
    }
    
    MappedSelector<Entity> andRead(Collection<?> properties);
    
    default MappedSelector<Entity> andRead(Object... properties) {
        return andLoad(asList(properties));
    }
}
