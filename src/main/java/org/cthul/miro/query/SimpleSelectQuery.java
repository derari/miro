package org.cthul.miro.query;

import java.util.List;
import org.cthul.miro.query.adapter.DBAdapter;
import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.adapter.QuerySyntax;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.sql.SelectBuilder;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.template.QueryTemplateProvider;

public class SimpleSelectQuery extends AbstractSelectQuery implements QueryString<SelectBuilder<?>> {

    public SimpleSelectQuery(QueryAdapter<?> adapter) {
        super(adapter);
    }

    public SimpleSelectQuery(QueryAdapter<?> adapter, QueryTemplate template) {
        super(adapter, template);
    }

    public SimpleSelectQuery(QuerySyntax adapter, QueryTemplateProvider templateProvider) {
        super(adapter, templateProvider);
    }

    public SimpleSelectQuery(QueryType<?> type, QuerySyntax adapter, QueryTemplateProvider templateProvider) {
        super(type, adapter, templateProvider);
    }
    
    @Override
    public QueryString<SelectBuilder<?>> getAdapter() {
        return (QueryString) super.getAdapter();
    }

    @Override
    public String getQueryString() {
        return getAdapter().getQueryString();
    }

    @Override
    public int getBatchCount() {
        return getAdapter().getBatchCount();
    }

    @Override
    public List<Object> getArguments(int batch) {
        return getAdapter().getArguments(batch);
    }

    @Override
    public SelectBuilder<?> getBuilder() {
        return getAdapter().getBuilder();
    }

    @Override
    public QueryType<SelectBuilder<?>> getQueryType() {
        return (QueryType) super.getQueryType();
    }
}
