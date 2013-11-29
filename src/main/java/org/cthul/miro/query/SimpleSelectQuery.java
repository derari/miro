package org.cthul.miro.query;

import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.adapter.QuerySyntax;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.template.QueryTemplateProvider;

public class SimpleSelectQuery extends AbstractSelectQuery {

    public SimpleSelectQuery(QueryType queryType) {
        super(queryType);
    }

    public SimpleSelectQuery(QueryType queryType, QueryTemplate template) {
        super(queryType, template);
    }

    public SimpleSelectQuery(QueryType type, QueryTemplateProvider templateProvider) {
        super(type, templateProvider);
    }

    public SimpleSelectQuery() {
    }

    public SimpleSelectQuery(QueryTemplate template) {
        super(template);
    }

    public SimpleSelectQuery(QueryTemplateProvider templateProvider) {
        super(templateProvider);
    }

    public QueryString<?> getQueryString(QuerySyntax syntax) {
        return getAdapter(syntax);
    }
}
