package org.cthul.miro.test;

import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.api.AbstractQuery;
import org.cthul.miro.query.sql.AnsiSql;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.InsertBuilder;
import org.cthul.miro.query.template.QueryTemplateProvider;

public class TestInsertQuery extends AbstractQuery {

    public TestInsertQuery(QueryTemplateProvider template) {
        super(AnsiSql.newInsertQuery(), template.getTemplate(DataQuery.INSERT));
    }
    
    public QueryString<InsertBuilder<?>> getQuery() {
        return (QueryString) getBuilder();
    }
    
    public String getQueryString() {
        return getQuery().getQueryString();
    }
    
    public TestInsertQuery insert(String... update) {
        for (String s: update) {
            put(s);
        }
        return this;
    }
    
    public TestInsertQuery tuple(Object... values) {
        put2("insert-values", "add", values);
        return this;
    }
}
