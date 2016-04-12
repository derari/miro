package org.cthul.miro.map;

import java.util.function.Supplier;
import org.cthul.miro.composer.ListNode;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface MappingKey<V> extends Key<V> {
    
    /** Allows to load field values from the result set. */
    static final MappingKey<ListNode<String>> LOAD_FIELD = MKey.LOAD_FIELD;
    
    /** Allows to set fields to given values. */
    static final MappingKey<SetAttribute> SET_FIELD = MKey.SET_FIELD;
    
    static final MappingKey<?> LOAD_ALL = MKey.LOAD_ALL;
    
    static MKey key(Object o) {
        return Key.castDefault(o, MKey.NIL);
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    static enum MKey implements MappingKey {
        
        LOAD_FIELD,
        SET_FIELD,
        
        LOAD_ALL,
        
        NIL;
    }
    
    static interface SetAttribute {
        default void set(String key, Object value) {
            set(key, () -> value);
        }
        void set(String key, Supplier<?> value);
    }
}
