package org.cthul.miro.domain.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.domain.Domain;
import org.cthul.miro.domain.EntityType;
import org.cthul.miro.domain.Repository;

/**
 *
 */
public class DomainBuilder implements Domain {

    private final Map<Object, EntityType<?>> typeMap = new HashMap<>();
    private final Function<Object, EntityType<?>> lookUp = this::getType;
    
    public DomainBuilder put(Object key, EntityType<?> type) {
        typeMap.put(key, type);
        return this;
    }

    @Override
    public <E> EntityType<E> getEntityType(Object key) {
        return (EntityType) getType(key);
    }
    
    protected EntityType<?> getType(Object key) {
        EntityType<?> type = typeMap.get(key);
        if (type != null) return type;
        return addMissingType(key);
    }
    
    protected EntityType<?> addMissingType(Object key) {
        return null;
    }
    
    @Override
    public Repository newRepository(MiConnection cnn) {
        return new CachingRepository(cnn, lookUp);
    }

    @Override
    public Repository newUncachedRepository(MiConnection cnn) {
        return new SimpleRepository(cnn, lookUp);
    }
}
