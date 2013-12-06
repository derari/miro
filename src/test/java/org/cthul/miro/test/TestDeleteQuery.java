package org.cthul.miro.test;

import org.cthul.miro.dml.DataQuerySubkey;
import org.cthul.miro.dml.DataQueryKey;
import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.AbstractQuery;
import org.cthul.miro.query.sql.AnsiSql;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.DeleteBuilder;
import org.cthul.miro.query.template.*;

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
        put2(DataQueryKey.DELETE_VALUES, DataQuerySubkey.ADD, values);
        return this;
    }
}
