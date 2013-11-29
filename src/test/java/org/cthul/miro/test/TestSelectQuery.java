package org.cthul.miro.test;

import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.AbstractQuery;
import org.cthul.miro.query.sql.AnsiSql;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.SelectBuilder;
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
    
    public TestSelectQuery select(String... select) {
        for (String s: select) {
            for (String s2: s.split(",")) {
                put(s2.trim());
            }
        }
        return this;
    }
    
    public TestSelectQuery orderBy(String... select) {
        for (String s: select) {
            for (String s2: s.split(",")) {
                put("orderBy-" + s2.trim());
            }
        }
        return this;
    }
}
