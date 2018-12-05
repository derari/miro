package org.cthul.miro.util;

import java.util.*;

/**
 *
 */
public class ArrayMap<K, V> extends AbstractMap<K, V> {
    
    private final Object[] data;
    private EntrySet entrySet;
    private ItemSet<K> keySet;
    private ItemSet<V> valueSet;

    public ArrayMap(Object... data) {
        if (data.length % 2 != 0) throw new IllegalArgumentException("Expected key-value pairs");
        this.data = data;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet != null ? entrySet : (entrySet = new EntrySet());
    }

    @Override
    public Set<K> keySet() {
        return keySet != null ? keySet : (keySet = new ItemSet<>(0));
    }

    @Override
    public Collection<V> values() {
        return valueSet != null ? valueSet : (valueSet = new ItemSet<>(1));
    }

    @Override
    public int size() {
        return data.length / 2;
    }

    @Override
    public V get(Object key) {
        for (int i = 0; i < data.length; i += 2) {
            if (key.equals(data[i])) {
                return (V) data[i+1];
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean containsKey(Object key) {
        return get(key) != null;
    }
    
    protected class EntrySet extends AbstractSet<Entry<K, V>> {
        
        private Entry<K, V>[] entries = new Entry[size()];
        
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new Iterator<Entry<K, V>>() {
                int index = 0;
                @Override
                public boolean hasNext() {
                    return index < size();
                }
                @Override
                public Entry<K, V> next() {
                    int i = index++;
                    return entries[i] != null ? entries[i] :
                            (entries[i] = new Entry<K, V>() {
                        @Override
                        public K getKey() {
                            return (K) data[i * 2];
                        }
                        @Override
                        public V getValue() {
                            return (V) data[i * 2 + 1];
                        }
                        @Override
                        public V setValue(V value) {
                            V old = getValue();
                            data[i * 2 + 1] = value;
                            return old;
                        }
                    });
                }
            };
        }
        @Override
        public int size() {
            return data.length / 2;
        }
    }
    
    protected class ItemSet<T> extends AbstractSet<T> {
        private final int offset;

        public ItemSet(int offset) {
            this.offset = offset;
        }
        
        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int index = 0;
                @Override
                public boolean hasNext() {
                    return index + offset < size();
                }
                @Override
                public T next() {
                    return (T) data[(index++) * 2 + offset];
                }
            };
        }

        @Override
        public int size() {
            return data.length / 2;
        }
    }
}
