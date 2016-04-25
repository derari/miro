package org.cthul.miro.sql;

/**
 *
 */
public interface SqlFilterableClause extends SqlClause {
    
    Where<?> where();
}
