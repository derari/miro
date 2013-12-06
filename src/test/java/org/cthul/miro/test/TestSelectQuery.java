package org.cthul.miro.test;

import org.cthul.miro.doc.MultiValue;
import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.AbstractQuery;
import org.cthul.miro.query.sql.AnsiSql;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.SelectBuilder;
import org.cthul.miro.dml.DataQueryKey;
import org.cthul.miro.query.template.QueryTemplateProvider;

public class TestSelectQuery extends AbstractQuery {

    public TestSelectQuery(QueryTemplateProvider template) {
        super(DataQuery.SELECT, template);
    }
    
    public QueryString<SelectBuilder<?>> getQuery() {
        return getAdapter(AnsiSql.getInstance());
    }
    
    public String getQueryString() {
        return getQuery().getQueryString();
    }
    
    public TestSelectQuery select(@MultiValue String... select) {
        for (String s: select) {
            put(DataQueryKey.SELECT, splitKeys(s));
        }
        return this;
    }
    
    public TestSelectQuery orderBy(@MultiValue String... attributes) {
        for (String a: attributes) {
            put(DataQueryKey.ORDER_BY, splitKeys(a));
        }
        return this;
    }
    
    static Object[] splitKeys(String keys) {
        String[] result = keys.split(",");
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].trim();
        }
        return result;
    }
}
