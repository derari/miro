package org.cthul.miro.sql.template;

import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.part.MapNode;
import org.cthul.miro.util.Key;
import org.cthul.miro.request.part.Parameterized;

/**
 *
 * @param <Value>
 */
public interface SqlComposerKey<Value> extends Key<Value> {
    
    /** Adds attributes to the query result. */
    static final SqlComposerKey<ListNode<String>> ATTRIBUTES = SQKey.ATTRIBUTES;
    
    static final SqlComposerKey<MapNode<String,Parameterized>> SNIPPETS = SQKey.SNIPPETS;
    
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
}
