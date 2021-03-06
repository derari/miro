package org.cthul.miro.sql;

import org.cthul.miro.sql.SqlBuilder.Code;
import org.cthul.miro.sql.syntax.MiSqlParser;

/**
 *
 */
public interface SelectBuilder extends SqlTableClause, SqlJoinableClause, SqlFilterableClause {
    
    Select select();
    
    default Select select(String sql, Object... args) {
        return select().sql(sql, args);
    }

    @Override
    default From table() {
        return from();
    }
    
    From from();
    
    default From from(String sql, Object... args) {
        return from().sql(sql, args);
    }
    
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
        return join().outer();
    }
    
    default Join join(String sql, Object... args) {
        return join().sql(sql, args);
    }
    
    default Join leftJoin(String sql, Object... args) {
        return join().left().sql(sql, args);
    }
    
    default Join rightJoin(String sql, Object... args) {
        return join().right().sql(sql, args);
    }
    
    default Join outerJoin(String sql, Object... args) {
        return join().outer().sql(sql, args);
    }
    
    @Override
    Where where();
    
    default Where where(String sql, Object... args) {
        return where().sql(sql, args);
    }
    
    GroupBy groupBy();
    
    default GroupBy groupBy(String sql, Object... args) {
        return groupBy().sql(sql, args);
    }
    
    Having having();
    
    default Having having(String sql, Object... args) {
        return having().sql(sql, args);
    }
    
    OrderBy orderBy();
    
    default OrderBy orderBy(String sql, Object... args) {
        return orderBy().sql(sql, args);
    }
    
    default SelectBuilder include(Code<? super SelectBuilder> code) {
        code.appendTo(this);
        return this;
    }
    
    default SelectBuilder sql(String sql) {
        return include(MiSqlParser.parsePartialSelect(sql));
    }
    
    default SelectBuilder sql(String sql, Object... args) {
        return include(MiSqlParser.parsePartialSelect(sql, args));
    }
    
    interface Composite<This extends Composite<This>> extends SelectBuilder, SqlJoinableClause.Composite<This> {

        @Override
        default This sql(String sql) {
            return sql(sql, (Object[]) null);
        }

        @Override
        This sql(String sql, Object... args);
//        {
//            include(MiSqlParser.parsePartialSelect(sql));
//            return (This) this;
//        }
    }
    
    interface Select extends Composite<Select> {

        @Override
        default Select sql(String sql, Object... args) {
            MiSqlParser.parsePartialSelectOrCode(sql, args, "SELECT", this, this);
            return this;
        }
    }
    
    interface From extends SelectBuilder, SqlTableClause.Table<From> {

        @Override
        default From sql(String sql) {
            return sql(sql, (Object[]) null);
        }

        @Override
        default From sql(String sql, Object... args) {
            MiSqlParser.parsePartialSelectOrCode(sql, args, "FROM", this, this);
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
        
        boolean isEmpty();
        
        JoinType getJoinType();
        
        @Override
        Where on();

        @Override
        default SelectBuilder.Join sql(String sql, Object... args) {
            String key = isEmpty() ? getJoinType().toString() : "";
            MiSqlParser.parsePartialSelectOrCode(sql, args, key, this, this);
            return this;
        }
    }
    
    interface Where extends Composite<Where>, SqlJoinableClause.Where<Where> {

        boolean isOpen();
        
        @Override
        default Where sql(String sql, Object... args) {
            String key = isOpen() ? "" : "WHERE";
            MiSqlParser.parsePartialSelectOrCode(sql, args, key, this, this);
            return this;
        }
    }
    
    interface GroupBy extends Composite<GroupBy> {

        @Override
        public default GroupBy sql(String sql, Object... args) {
            MiSqlParser.parsePartialSelectOrCode(sql, args, "GROUP", this, this);
            return this;
        }
    }
    
    interface Having extends Composite<Having> {

        @Override
        public default Having sql(String sql, Object... args) {
            MiSqlParser.parsePartialSelectOrCode(sql, args, "HAVING", this, this);
            return this;
        }
    }
    
    interface OrderBy extends Composite<OrderBy> {

        @Override
        public default OrderBy sql(String sql, Object... args) {
            MiSqlParser.parsePartialSelectOrCode(sql, args, "ORDER", this, this);
            return this;
        }
    }
}
