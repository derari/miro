package org.cthul.miro.db.sql;

import org.cthul.miro.db.sql.SqlBuilder.Code;
import org.cthul.miro.db.sql.syntax.MiSqlParser;

/**
 *
 */
public interface SelectBuilder extends SqlTableClause, SqlJoinableClause, SqlFilterableClause {
    
    Select select();

    @Override
    default From table() {
        return from();
    }
    
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
    
    @Override
    Where where();
    
    GroupBy groupBy();
    
    Having having();
    
    OrderBy orderBy();
    
    default SelectBuilder include(Code<? super SelectBuilder> code) {
        code.appendTo(this);
        return this;
    }
    
    default SelectBuilder sql(String sql) {
        return include(MiSqlParser.parsePartialSelect(sql));
    }
    
    interface Composite<This extends Composite<This>> extends SelectBuilder, SqlJoinableClause.Composite<This> {

        @Override
        This sql(String sql);
//        {
//            include(MiSqlParser.parsePartialSelect(sql));
//            return (This) this;
//        }
    }
    
    interface Select extends Composite<Select> {

        @Override
        default Select sql(String sql) {
            MiSqlParser.parsePartialSelectOrCode(sql, "SELECT", this, this);
            return this;
        }
    }
    
    interface From extends SelectBuilder, SqlTableClause.Table<From> {

        @Override
        default From sql(String sql) {
            MiSqlParser.parsePartialSelectOrCode(sql, "FROM", this, this);
            return this;
        }
    }
    
    interface Join extends Composite<Join>, SqlJoinableClause.Join<Join> {

//        @Override
//        SelectBuilder.Join left();
//        
//        @Override
//        SelectBuilder.Join right();
//        
//        @Override
//        SelectBuilder.Join outer();
        
        @Override
        Where on();

        @Override
        default SelectBuilder.Join sql(String sql) {
            MiSqlParser.parsePartialSelectOrCode(sql, "JOIN", this, this);
            return this;
        }
    }
    
    interface Where extends Composite<Where>, SqlJoinableClause.Where<Where> {

        @Override
        default Where sql(String sql) {
            MiSqlParser.parsePartialSelectOrCode(sql, "WHERE", this, this);
            return this;
        }
    }
    
    interface GroupBy extends Composite<GroupBy> {

        @Override
        public default GroupBy sql(String sql) {
            MiSqlParser.parsePartialSelectOrCode(sql, "GROUP", this, this);
            return this;
        }
    }
    
    interface Having extends Composite<Having> {

        @Override
        public default Having sql(String sql) {
            MiSqlParser.parsePartialSelectOrCode(sql, "HAVING", this, this);
            return this;
        }
    }
    
    interface OrderBy extends Composite<OrderBy> {

        @Override
        public default OrderBy sql(String sql) {
            MiSqlParser.parsePartialSelectOrCode(sql, "ORDER", this, this);
            return this;
        }
    }
}
