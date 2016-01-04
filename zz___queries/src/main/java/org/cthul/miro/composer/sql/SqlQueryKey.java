package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.QueryComposerKey;
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
    
    public static SQKey key(Object o) {
        return Key.castDefault(o, SQKey.NIL);
    }
    
    public static interface AttributeList extends QueryComposerKey.PhaseListener {
        
        default void addAttributes(Iterable<?> attributeKeys) {
            attributeKeys.forEach(this::addAttribute);
        }
        
        void addAttribute(Object attributeKey);

        @Override
        default AttributeList and(QueryComposerKey.PhaseListener other) {
            AttributeList al = (other instanceof AttributeList) ? (AttributeList) other : null;
            return new AttributeList() {
                @Override
                public void addAttribute(Object attributeKey) {
                    AttributeList.this.addAttribute(attributeKey);
                    if (al != null) al.addAttribute(attributeKey);
                }
                @Override
                public void enter(QueryComposerKey.Phase phase) {
                    AttributeList.this.enter(phase);
                    other.enter(phase);
                }
            };
        }
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public static enum SQKey implements SqlQueryKey {
        
        DEFAULT_ATTRIBUTES,
        ATTRIBUTE,
        TABLE,
        NIL;
    }
}
