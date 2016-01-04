package org.cthul.miro.composer.sql;

import java.util.List;
import org.cthul.miro.composer.ComposerKey;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Value>
 */
public interface SqlQueryKey<Value> extends Key<Value> {
    
    /** Adds all default attributes. */
    public static final SqlQueryKey<?> DEFAULT_ATTRIBUTES = SQKey.DEFAULT_ATTRIBUTES;
    
    /** Adds attributes to the query.
     * Expects attribute IDs as arguments. */
    public static final SqlQueryKey<AttributeList> ATTRIBUTE = SQKey.ATTRIBUTE;
    
    /** Manages the table clause. Always required. */
    public static final SqlQueryKey<?> TABLE = SQKey.TABLE;
    
    /** . */
    public static final SqlQueryKey<FindByKeys> FIND_BY_KEYS = SQKey.FIND_BY_KEYS;
    
    public static SQKey key(Object o) {
        return Key.castDefault(o, SQKey.NIL);
    }
    
    public static interface AttributeList extends ComposerKey.PhaseListener {
        
        default void addAll(Iterable<? extends Attribute> attributes) {
            attributes.forEach(this::add);
        }
        
        void add(Attribute attribute);
    }
    
    public static interface FindByKeys {
        
        void addAll(List<Object[]> keys);
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public static enum SQKey implements SqlQueryKey {
        
        DEFAULT_ATTRIBUTES,
        ATTRIBUTE,
        TABLE,
        
        FIND_BY_KEYS,
        
        NIL;
    }
}
