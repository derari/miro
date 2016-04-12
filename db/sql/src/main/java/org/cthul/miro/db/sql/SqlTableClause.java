package org.cthul.miro.db.sql;

/**
 *
 */
public interface SqlTableClause {
    
    Table<?> table();
    
    interface Table<This extends Table<This>> extends SqlJoinableClause, SqlBuilder<This> {
    }
}
