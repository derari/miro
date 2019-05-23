package org.cthul.miro.composer.node;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cthul.miro.composer.node.Copyable;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.composer.node.MultiKeyValueNode;
import org.cthul.miro.composer.node.StatementPart;

public abstract class MultiKeyValueMapRecorder<K, V> implements MultiKeyValueNode<K, V>, Copyable<Object>, StatementPart<MultiKeyValueNode<K, V>> {
    
    private final MultiKeyValueMapRecorder<K,V> parent;
    private final List<K[]> keys = new ArrayList<>();
    private final List<V[]> arguments = new ArrayList<>();

    public MultiKeyValueMapRecorder() {
        this.parent = null;
    }

    public MultiKeyValueMapRecorder(MultiKeyValueMapRecorder source) {
        this.parent = source;
    }

    @Override
    public ListNode<V[]> forKeys(K... keys) {
        return (args) -> put(keys, args);
    }

    @Override
    public void put(K key, V value) {
        put(asArray(key), asArray(value));
    }

    @Override
    public void put(Map<? extends K, ? extends V> map) {
        K[] newKeys = getKeyArray(map);
        V[] newValues = newValueArray(map.size());
        for (int i = 0; i < newKeys.length; i++) {
            newValues[i] = map.get(newKeys[i]);
        }
        put(newKeys, newValues);
    }
    
    protected void put(K[] keys, V[] values) {
        this.keys.add(keys);
        this.arguments.add(values);
    }
    
    protected abstract K[] getKeyArray(Map<? extends K, ? extends V> map);
    
    protected abstract V[] newValueArray(int size);
    
    private <T> T[] asArray(T value) {
        if (value == null) {
            return null;
        }
        T[] array = (T[]) Array.newInstance(value.getClass(), 1);
        array[0] = value;
        return array;
    }


    @Override
    public void addTo(MultiKeyValueNode<K, V> builder) {
        if (parent != null) {
            parent.addTo(builder);
        }
        K[] lastKey = null;
        ListNode<V[]> lastList = null;
        for (int i = 0; i < keys.size(); i++) {
            if (lastKey != keys.get(i)) {
                lastKey = keys.get(i);
                lastList = builder.forKeys(lastKey);
            }
            lastList.add(arguments.get(i));
        }
    }
}
