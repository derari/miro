package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.Configurable;
import org.cthul.miro.composer.ListNode;
import org.cthul.miro.composer.MapNode;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Value>
 */
public interface SqlComposerKey<Value> extends Key<Value> {
    
//    /** Adds all default attributes to the result. */
//    static final SqlQueryKey<?> DEFAULT_ATTRIBUTES = SQKey.DEFAULT_ATTRIBUTES;
//    
//    /** Adds all default attributes to the result. */
//    static final SqlQueryKey<?> OPTIONAL_ATTRIBUTES = SQKey.OPTIONAL_ATTRIBUTES;
    
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
        
//        DEFAULT_ATTRIBUTES,
//        OPTIONAL_ATTRIBUTES,
//        DEFAULT_ATTRIBUTES,
//        OPTIONAL_ATTRIBUTES,
        
        ATTRIBUTES,
        
        ATTRIBUTE_FILTER,
        
        SNIPPETS,
        
//        FIND_BY_KEYS,
        
        NIL;
    }
    
    interface AttributeFilter {
        
        ListNode<Object[]> forAttributes(String... attributeKeys);
    }
}
