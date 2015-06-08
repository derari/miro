package org.cthul.miro.db.sql;

import org.cthul.miro.db.syntax.RequestBuilder;

/**
 *
 */
public interface SqlClause {
    
    Select select();
    
    From from();
    
    Join join();
    
    default Join leftJoin() {
        return join().left();
    }
    
    default Join rightJoin() {
        return join().right();
    }
    
    default Join outerJoin() {
        return join().right();
    }
    
    Where where();
    
    GroupBy groupBy();
    
    Having having();
    
    OrderBy orderBy();
    
    interface Composite<This extends Composite<This>> extends SqlClause, RequestBuilder<This> {
        
        This and();
    }
    
    interface Select extends Composite<Select> {
    }
    
    interface From extends SqlClause, RequestBuilder<From> {
    }
    
    interface Join extends Composite<Join> {
        
        Join left();
        
        Join right();
        
        Join outer();
        
        Where on();
    }
    
    interface Where extends Composite<Where> {
    }
    
    interface GroupBy extends Composite<GroupBy> {
    }
    
    interface Having extends Composite<Having> {
    }
    
    interface OrderBy extends Composite<OrderBy> {
    }
    
}
