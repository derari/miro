package org.cthul.miro.sql.composer;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.util.ValueKey;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.composer.node.MultiKeyValueNode;
import org.cthul.miro.sql.composer.Comparison.Op;

/**
 *
 */
public interface AttributeFilter extends MultiKeyValueNode<String, Object> {

    ListNode<Object[]> forAttributes(String... attributeKeys);
    
    default ListNode<Object> forAttribute(String attributeKey) {
        ListNode<Object[]> filter = forAttributes(attributeKey);
        return value -> filter.add(new Object[]{value});
    }

    @Override
    default ListNode<Object[]> forKeys(String... keys) {
        return forAttributes(keys);
    }
    
    @Override
    default void put(Map<? extends String, ? extends Object> filters) {
        String[] keys = filters.keySet().toArray(new String[filters.size()]);
        Arrays.sort(keys);
        Object[] values = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = filters.get(keys[i]);
        }
        forAttributes(keys).add(values);
    }

    @Override
    public default void put(String key, Object value) {
        forAttributes(key).add(new Object[]{value});
    }
    
    static AttributeFilterKey key(String[] attributeKeys) {
        return new AttributeFilterKey(attributeKeys);
    }
    
    class AttributeFilterKey extends ValueKey<ListNode<Object[]>> {
        private final String[] attributeKeys;
        public AttributeFilterKey(String... attributeKeys) {
            super(Arrays.stream(attributeKeys).collect(Collectors.joining(",")));
            this.attributeKeys = attributeKeys;
        }

        public String[] getAttributeKeys() {
            return attributeKeys;
        }
    }
}
