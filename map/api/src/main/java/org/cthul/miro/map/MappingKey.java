package org.cthul.miro.map;

import java.util.function.Supplier;
import org.cthul.miro.composer.ComposerKey;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface MappingKey<V> extends Key<V> {
    
    static final MappingKey<ComposerKey.ResultAttributes> LOAD_FIELD = MKey.LOAD_FIELD;
    static final MappingKey<SetAttribute> SET_FIELD = MKey.SET_FIELD;
    
    static MKey key(Object o) {
        return Key.castDefault(o, MKey.NIL);
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    static enum MKey implements MappingKey {
        
        LOAD_FIELD,
        SET_FIELD,
        
        NIL;
    }
    
    static interface SetAttribute {
        default void set(String key, Object value) {
            set(key, () -> value);
        }
        void set(String key, Supplier<?> value);
    }
}
