package org.cthul.miro.query;

/**
 *
 */
public class ZSqlQueryBuilder extends ZParsingQueryBuilder {

    public ZSqlQueryBuilder() {
    }

    public ZSqlQueryBuilder select(String... selectClause) {
        sql_select(selectClause);
        return this;
    }
    
    public ZSqlQueryBuilder select(String selectClause) {
        sql_select(selectClause);
        return this;
    }
    
    public ZSqlQueryBuilder from(String from) {
        sql_from(from);
        return this;
    }
    
    public ZSqlQueryBuilder join(String join) {
        sql_join(join);
        return this;
    }
    
    public ZSqlQueryBuilder join(String join, Object... args) {
        sql_join(join).put("", args);
        return this;
    }
    
    public ZSqlQueryBuilder where(String where) {
        sql_where(where);
        return this;
    }

    public ZSqlQueryBuilder where(String where, Object... args) {
        sql_where(where).put("", args);
        return this;
    }

    public ZSqlQueryBuilder groupBy(String groupBy) {
        sql_groupBy(groupBy);
        return this;
    }

    public ZSqlQueryBuilder having(String having) {
        sql_having(having);
        return this;
    }

    public ZSqlQueryBuilder having(String having, Object... args) {
        sql_having(having).put("", args);
        return this;
    }

    public ZSqlQueryBuilder orderBy(String order) {
        sql_orderBy(order);
        return this;
    }
}
