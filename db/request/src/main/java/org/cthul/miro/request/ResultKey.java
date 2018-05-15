package org.cthul.miro.request;

import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Value>
 */
public interface ResultKey<Value> extends Key<Value> {
    
    static ResultKey<ListNode<String>> RESULT = RKey.RESULT;
    
    static ResultKey<?> DEFAULT = RKey.DEFAULT;
    
    static ResultKey<?> OPTIONAL = RKey.OPTIONAL;
    
    static RKey key(Object o) {
        return Key.castDefault(o, RKey.NIL);
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    enum RKey implements ResultKey {
        
        RESULT,
        
        DEFAULT,
        
        OPTIONAL,
        
        NIL;
    }
}
