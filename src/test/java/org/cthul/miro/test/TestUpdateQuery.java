package org.cthul.miro.test;

import org.cthul.miro.dml.DataQuerySubkey;
import org.cthul.miro.dml.DataQueryKey;
import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.AbstractQuery;
import org.cthul.miro.query.sql.AnsiSql;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.UpdateBuilder;
import org.cthul.miro.query.template.*;

public class TestUpdateQuery extends AbstractQuery {
    
    public TestUpdateQuery(QueryTemplateProvider template) {
        super(DataQuery.UPDATE, template);
    }
    
    public QueryString<UpdateBuilder<?>> getQuery() {
        return getAdapter(AnsiSql.getInstance());
    }
    
    public String getQueryString() {
        return getQuery().getQueryString();
    }
    
    public TestUpdateQuery update(String... update) {
        for (String s: update) {
            put(DataQueryKey.UPDATE, TestSelectQuery.splitKeys(s));
        }
        return this;
    }
    
    public TestUpdateQuery tuple(Object... values) {
        put2(DataQueryKey.UPDATE_VALUES, DataQuerySubkey.ADD, values);
        return this;
    }
}
