package org.cthul.miro.query;

/**
 *
 */
public class SqlQueryBuilder extends ParsingQueryBuilder {

    public SqlQueryBuilder() {
    }

    public SqlQueryBuilder select(String... selectClause) {
        sql_select(selectClause);
        return this;
    }
    
    public SqlQueryBuilder select(String selectClause) {
        sql_select(selectClause);
        return this;
    }
    
    public SqlQueryBuilder from(String from) {
        sql_from(from);
        return this;
    }
    
    public SqlQueryBuilder join(String join) {
        sql_join(join);
        return this;
    }
    
    public SqlQueryBuilder join(String join, Object... args) {
        sql_join(join).put("", args);
        return this;
    }
    
    public SqlQueryBuilder where(String where) {
        sql_where(where);
        return this;
    }

    public SqlQueryBuilder where(String where, Object... args) {
        sql_where(where).put("", args);
        return this;
    }

    public SqlQueryBuilder groupBy(String groupBy) {
        sql_groupBy(groupBy);
        return this;
    }

    public SqlQueryBuilder having(String having) {
        sql_having(having);
        return this;
    }

    public SqlQueryBuilder having(String having, Object... args) {
        sql_having(having).put("", args);
        return this;
    }

    public SqlQueryBuilder orderBy(String order) {
        sql_orderBy(order);
        return this;
    }
}
