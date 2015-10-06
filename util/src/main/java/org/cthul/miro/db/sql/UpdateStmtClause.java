package org.cthul.miro.db.sql;

import org.cthul.miro.db.syntax.QlBuilder;


/**
 *
 */
public interface UpdateStmtClause extends SqlJoinableClause {
    
    Select select();
    
    From from();
    
    @Override
    Join join();
    
    @Override
    default Join leftJoin() {
        return join().left();
    }
    
    @Override
    default Join rightJoin() {
        return join().right();
    }
    
    @Override
    default Join outerJoin() {
        return join().right();
    }
    
    Where where();
    
    GroupBy groupBy();
    
    Having having();
    
    OrderBy orderBy();
    
    interface Composite<This extends Composite<This>> extends UpdateStmtClause, SqlJoinableClause.Composite<This> {
    }
    
    interface Select extends Composite<Select> {
    }
    
    interface From extends UpdateStmtClause, QlBuilder<From> {
    }
    
    interface Join extends Composite<Join>, SqlJoinableClause.Join<Join> {

        @Override
        UpdateStmtClause.Join left();
        
        @Override
        UpdateStmtClause.Join right();
        
        @Override
        UpdateStmtClause.Join outer();
        
        @Override
        Where on();
    }
    
    interface Where extends Composite<Where>, SqlJoinableClause.Where<Where> {
    }
    
    interface GroupBy extends Composite<GroupBy> {
    }
    
    interface Having extends Composite<Having> {
    }
    
    interface OrderBy extends Composite<OrderBy> {
    }
}
