package org.cthul.miro.sql.template;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.cthul.miro.request.part.Configurable;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.part.MapNode;
import org.cthul.miro.request.impl.ValueKey;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Value>
 */
public interface SqlComposerKey<Value> extends Key<Value> {
    
    /** Adds attributes to the query. */
    static final SqlComposerKey<ListNode<String>> ATTRIBUTES = SQKey.ATTRIBUTES;
    
    static final SqlComposerKey<MapNode<String,Configurable>> SNIPPETS = SQKey.SNIPPETS;
    
    static final SqlComposerKey<AttributeFilter> ATTRIBUTE_FILTER = SQKey.ATTRIBUTE_FILTER;
    
//    /** Selects records by key values. */
//    static final SqlQueryKey<ListNode<Object[]>> FIND_BY_KEYS = SQKey.FIND_BY_KEYS;
    
    static SQKey key(Object o) {
        return Key.castDefault(o, SQKey.NIL);
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    enum SQKey implements SqlComposerKey {
        
        ATTRIBUTES,
        
        ATTRIBUTE_FILTER,
        
        SNIPPETS,
        
        NIL;
    }
    
    interface AttributeFilter {
        
        ListNode<Object[]> forAttributes(String... attributeKeys);
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
