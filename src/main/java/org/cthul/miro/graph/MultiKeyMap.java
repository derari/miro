package org.cthul.miro.graph;

import java.util.Arrays;

public class MultiKeyMap<E extends Exception> extends KeyMap<Object[], MultiKeyMap.MultiKey, E> {

    public MultiKeyMap(Fetch<Object[], E> fetch) {
        super(fetch);
    }

    @Override
    protected MultiKey prepareInternKey() {
        return new MultiKey();
    }

    @Override
    protected MultiKey internKey(MultiKey prepared, Object[] key) {
        prepared.become(key);
        return prepared;
    }

    @Override
    protected MultiKey internKey(Object[] key) {
        return new MultiKey(key);
    }

    protected static class MultiKey {

        private Object[] keys;
        private int hash = -1;

        public MultiKey(Object[] ids) {
            this.keys = ids.clone();
        }

        public MultiKey() {
            keys = null;
        }

        private void become(Object key) {
            this.keys = (Object[]) key;
            hash = -1;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MultiKey)) {
                return false;
            }
            final MultiKey o = (MultiKey) obj;
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
