package org.cthul.miro.domain.impl;

import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.domain.EntitySet;
import org.cthul.miro.domain.EntityType;
import org.cthul.miro.domain.KeyMap;

/**
 *
 */
public class CachingRepository extends SimpleRepository {

    public CachingRepository(MiConnection connection, Function<Object, EntityType<?>> typeLookUp) {
        super(connection, typeLookUp);
    }

    @Override
    protected <E> EntitySet<E> newEntitySet(EntityType<E> type) {
        return new CachingSet<>(type);
    }
    
    protected class CachingSet<E> extends SimpleSet<E> {
        
        private final KeyMap.MultiKey<E> map = new KeyMap.MultiKey<>();
        
        public CachingSet(EntityType<E> type) {
            super(type);
        }

        @Override
        public E get(Object... key) throws MiException {
            E e = map.get(key);
            if (e != null) return e;
            e = super.get(key);
            map.put(key, e);
            return e;
        }
    }
}
