package org.cthul.miro.domain;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.domain.impl.DomainBuilder;

/**
 *
 */
public interface Domain {
    
    <E> EntityType<E> getEntityType(Object key);
    
    Repository newRepository(MiConnection cnn);
    
    Repository newUncachedRepository(MiConnection cnn);
    
    static DomainBuilder build() {
        return new DomainBuilder();
    }
}
