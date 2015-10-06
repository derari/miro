package org.cthul.miro.db.sql;

/**
 *
 */
public interface SqlJoinableClause extends SqlClause {

    Join<?> join();
    
    default Join<?> leftJoin() {
        return join().left();
    }
    
    default Join<?> rightJoin() {
        return join().right();
    }
    
    default Join<?> outerJoin() {
        return join().right();
    }
       
    interface Composite<This extends Composite<This>> extends SqlJoinableClause, SqlClause.Composite<This> {
    }
    
    interface Join<This extends Join<This>> extends Composite<This> {
        
        Join<This> left();
        
        Join<This> right();
        
        Join<This> outer();
        
        SqlJoinableClause.Where<?> on();
    }
    
    interface Where<This extends Where<This>> extends SqlJoinableClause, SqlClause.Where<This> {
    }
}
