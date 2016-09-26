package org.cthul.miro.map;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.cthul.miro.request.impl.ValueKey;
import org.cthul.miro.request.part.KeyValueNode;
import org.cthul.miro.request.part.ListNode;

/**
 *
 */
public interface PropertyFilter extends KeyValueNode<String, Object> {

    ListNode<Object[]> forProperties(String... propertyKeys);

    @Override
    public default void put(String key, Object value) {
        forProperties(key).add(new Object[]{value});
    }
    
    @Override
    public default void put(Map<? extends String, ? extends Object> filters) {
        String[] keys = filters.keySet().toArray(new String[filters.size()]);
        Arrays.sort(keys);
        Object[] values = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = filters.get(keys[i]);
        }
        forProperties(keys).add(values);
    }

    public static PropertyFilterKey key(String... propertyKeys) {
        return new PropertyFilterKey(propertyKeys);
    }
    
    class PropertyFilterKey extends ValueKey<ListNode<Object[]>> {
        private final String[] propertyKeys;
        public PropertyFilterKey(String... propertyKeys) {
            super(Arrays.stream(propertyKeys).collect(Collectors.joining(",")));
            this.propertyKeys = propertyKeys;
        }

        public String[] getAttributeKeys() {
            return propertyKeys;
        }
    }
}
