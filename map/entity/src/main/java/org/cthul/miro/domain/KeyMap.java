package org.cthul.miro.domain;

import java.util.*;

public abstract class KeyMap<InternKey, Entity> {

    private final Map<InternKey, Entity> map = new HashMap<>();

    public KeyMap() {
    }

    public Entity get(Object[] key) {
        return getIntern(internKey(key, false));
    }

    protected Entity getIntern(InternKey key) {
        return map.get(key);
    }
    
    public void put(Object[] key, Entity value) {
        putIntern(internKey(key, true), value);
    }
    
    protected void putIntern(InternKey key, Entity value) {
        map.put(key, value);
    }
    
    protected abstract InternKey prepareInternKey();
    
    protected abstract InternKey internKey(InternKey prepared, Object[] key);
    
    protected abstract InternKey internKey(Object[] key, boolean permanent);
    
    public static class SingleKey<Entity> extends KeyMap<Object, Entity> {
        @Override
        protected Object prepareInternKey() {
            return null;
        }

        @Override
        protected Object internKey(Object prepared, Object[] key) {
            return key[0];
        }

        @Override
        protected Object internKey(Object[] key, boolean permanent) {
            return key[0];
        }
    }
    
    public static class MultiKey<Entity> extends KeyMap<KeyArray, Entity> {
        @Override
        protected KeyArray prepareInternKey() {
            return new KeyArray();
        }

        @Override
        protected KeyArray internKey(KeyArray prepared, Object[] key) {
            prepared.become(key);
            return prepared;
        }

        @Override
        protected KeyArray internKey(Object[] key, boolean permanent) {
            return new KeyArray(key, permanent);
        }
    }
    
    public static class KeyArray {

        private Object[] keys;
        private int hash = -1;

        public KeyArray(Object[] ids, boolean clone) {
            this.keys = clone ? ids.clone() : ids;
        }

        public KeyArray() {
            keys = null;
        }

        private void become(Object key) {
            this.keys = (Object[]) key;
            hash = -1;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeyArray)) {
                return false;
            }
            final KeyArray o = (KeyArray) obj;
            return Arrays.equals(keys, o.keys);
        }

        @Override
        public int hashCode() {
            if (hash == -1) {
                hash = Arrays.hashCode(keys);
                if (hash == -1) {
                    hash = 0;
                }
            }
            return hash;
        }
    }
}
