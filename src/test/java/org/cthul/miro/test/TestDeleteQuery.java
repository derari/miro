package org.cthul.miro.test;

import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.AbstractQuery;
import org.cthul.miro.query.sql.AnsiSql;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.DeleteBuilder;
import org.cthul.miro.query.template.QueryTemplateProvider;

public class TestDeleteQuery extends AbstractQuery {
    
    public TestDeleteQuery(QueryTemplateProvider template) {
        super(DataQuery.DELETE, template);
    }
    
    public QueryString<DeleteBuilder<?>> getQuery() {
        return getAdapter(AnsiSql.getInstance());
    }
    
    public String getQueryString() {
        return getQuery().getQueryString();
    }
    
    public TestDeleteQuery tuple(Object... values) {
        put2("delete-values", "add", values);
        return this;
    }
}
