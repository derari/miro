package org.cthul.miro.composer.sql;

/**
 *
 */
public enum InternalSqlQueryKey {
    
    NIL;
    
    public static enum Select {
        
        /** Adds an attribute to the SELECT clause */
        WRITE_ATTRIBUTE_SELECT,
        
        NIL;
    }
}
