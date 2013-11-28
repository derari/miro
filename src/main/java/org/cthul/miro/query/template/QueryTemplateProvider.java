package org.cthul.miro.query.template;

import org.cthul.miro.query.api.QueryType;

public interface QueryTemplateProvider {

    QueryTemplate getTemplate(QueryType<?> queryType);
}
