package org.cthul.miro.query.template;

import org.cthul.miro.query.QueryType;

public interface QueryTemplateProvider {

    QueryTemplate getTemplate(QueryType<?> queryType);
}
