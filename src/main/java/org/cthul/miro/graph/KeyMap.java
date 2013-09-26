package org.cthul.miro.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class KeyMap<Key, InternKey, E extends Exception> {

    private final Object NULL = new Object();
    private final Map<InternKey, Object> map = new HashMap<>();
    private final Fetch<Key, E> fetch;

    public KeyMap(Fetch<Key, E> fetch) {
        this.fetch = fetch;
    }

    /**
     * Returns objects for all keys.
     * @param keys
     * @return objects
     * @throws E 
     */
    public Object[] getAll(final Key[] keys) throws E {
        final Object[] result = new Object[keys.length];

        int missingCount = fill(result, keys);
        if (missingCount > 0) {
            fillMissing(result, keys, missingCount);
        }
        
        return result;
    }

    private int fill(final Object[] values, final Key[] keys) {
        int missingCount = 0;
        InternKey k = prepareInternKey();
        for (int i = 0; i < keys.length; i++) {
            if (values[i] == null) {
                final Object o;
                k = internKey(k, keys[i]);
                o = get(k);
                if (o == null) {
                    missingCount++;
                } else if (o != NULL) {
                    values[i] = o;
                }
            }
        }
        return missingCount;
    }

    private void fillMissing(final Object[] result, final Key[] keys, int missingCount) throws E {
        Set<Key> missingKeySet = new HashSet<>();
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) {
                missingKeySet.add(keys[i]);
            }
        }

        final Key[] missingKeys = (Key[]) missingKeySet.toArray();
        final Object[] missingValues = fetchValues(missingKeys);

        for (int i = 0; i < missingKeys.length; i++) {
            Object o = missingValues[i];
            if (o == null) {
                o = NULL;
            }
            map.put(internKey(missingKeys[i]), o);
        }

        missingCount = fill(result, keys);
        assert missingCount == 0;
    }

    protected abstract InternKey prepareInternKey();
    
    protected abstract InternKey internKey(InternKey prepared, Key key);
    
    protected abstract InternKey internKey(Key key);

    protected Object[] fetchValues(Object[] keys) throws E {
        return fetch.fetchValues(keys);
    }

    protected Object get(InternKey key) {
        return map.get(key);
    }

    public static interface Fetch<Key, E extends Exception> {
        
        Object[] fetchValues(Object[] keys) throws E;
    }
}
