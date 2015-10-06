package org.cthul.miro.composer.sql;

import org.cthul.miro.util.Key;

/**
 *
 */
public enum SqlQueryKey {
    
    /** Adds all default attributes. */
    DEFAULT_ATTRIBUTES,
    
    /** Adds attributes to the query.
     * Expects attribute IDs as arguments. */
    ATTRIBUTE,
    
    /** Manages the table clause. Always required. */
    TABLE,
    
    NIL;
    
    public static SqlQueryKey key(Object o) {
        return Key.castDefault(o, SqlQueryKey.class, NIL);
    }
    
    public static enum QKey {
        
    }
}
