package org.cthul.miro.sql.template;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.request.impl.ValueKey;
import org.cthul.miro.request.part.ListNode;

/**
 *
 */
public interface AttributeFilter {

    ListNode<Object[]> forAttributes(String... attributeKeys);
    
    default void add(Map<String, Object> filters) {
        String[] keys = filters.keySet().toArray(new String[filters.size()]);
        Arrays.sort(keys);
        Object[] values = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = filters.get(keys[i]);
        }
        forAttributes(keys).add(values);
    }
    
    default void add(Object... filters) {
        add(map(filters));
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
    
    static Map<String, Object> map(Object... values) {
        if (values.length % 2 != 0) throw new IllegalArgumentException("Expected key-value pairs");
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            String k = (String) values[i];
            Object v = values[i+1];
            map.put(k, v);
        }
        return map;
    }
    
    static Comparative eq(Object val) {
        return cmp(Comparison.EQUAL, val);
    }
    
    static Comparative like(Object val) {
        return cmp(Comparison.LIKE, val);
    }
    
    static Comparative cmp(Comparison op, Object val) {
        return new Comparative() {
            @Override
            public Comparison getOperator() {
                return op;
            }
            @Override
            public Object getValue() {
                return val;
            }
            @Override
            public String toString() {
                return getOperator() + " " + getValue();
            }
        };
    }
    
    static boolean isEqualComparative(Object value) {
        if (value instanceof Comparative) {
            return ((Comparative) value).getOperator() == Comparison.EQUAL;
        }
        return true;
    }
    
    static QlBuilder<?> appendComparative(Object value, QlBuilder<?> target) {
        if (value instanceof Comparative) {
            Comparative c = (Comparative) value;
            return target.append(c.getOperator().op).append(" ?")
                    .pushArgument(c.getValue());
        } else {
            return target.append("= ?").pushArgument(value);
        }
    }
    
    static void appendComparative(Object value, QlBuilder<?> target, BiConsumer<Object, QlBuilder<?>> writeValue) {
        if (value instanceof Comparative) {
            Comparative c = (Comparative) value;
            target.append(c.getOperator().op).append(" ");
            value = c.getValue();
        } else {
            target.append("= ");
        }
        writeValue.accept(value, target);
    }
    
    interface Comparative {
        
        Comparison getOperator();
        
        Object getValue();
    }
    
    enum Comparison {
        
        EQUAL("="),
        NOT_EQUAL("!="),
        LESS("<"),
        LESS_EQUAL("<="),
        GREATER_EQUAL(">="),
        GREATER(">"),
        LIKE("LIKE"),
        ;
        
        private final String op;

        private Comparison(String op) {
            this.op = op;
        }
    }
}
