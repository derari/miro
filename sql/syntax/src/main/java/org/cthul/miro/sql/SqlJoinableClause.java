package org.cthul.miro.sql;

/**
 *
 */
public interface SqlJoinableClause extends SqlClause {

    Join<?> join();
    
    default Join<?> join(JoinType jt) {
        return join().as(jt);
    }
    
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
//        
//        Join<This> left();
//        
//        Join<This> right();
//        
//        Join<This> outer();
        
        This as(JoinType jt);
        
        default This left() {
            return as(JoinType.LEFT);
        }
        
        default This right() {
            return as(JoinType.RIGHT);
        }
        
        default This outer() {
            return as(JoinType.OUTER);
        }
        
        SqlJoinableClause.Where<?> on();
    }
    
    enum JoinType {
        INNER,
        LEFT,
        RIGHT,
        OUTER;
        
        public static JoinType parse(String s) {
            if (s == null) return INNER;
            s = s.trim().toUpperCase();
            if (s.isEmpty()) return INNER;
            if (s.startsWith("LEFT")) return LEFT;
            if (s.startsWith("OUTER")) return OUTER;
            if (s.startsWith("RIGHT")) return RIGHT;
            if (s.startsWith("INNER")) return INNER;
            throw new IllegalArgumentException(s);
        }
    }
    
    interface Where<This extends Where<This>> extends SqlJoinableClause, SqlClause.Where<This> {
    }
}
