package org.cthul.miro.db.sql;

/**
 *
 */
public interface SqlFilterableClause extends SqlClause {
    
    Where<?> where();
}
