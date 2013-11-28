package org.cthul.miro.query.template;

import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.query.api.*;
import org.cthul.miro.query.parts.QueryPart;

public class SimpleQueryTemplate implements QueryTemplate {
    
    private final Map<String, QueryTemplatePart> parts = new HashMap<>();

    public SimpleQueryTemplate() {
    }
    
    protected synchronized void addTemplate(String key, QueryTemplatePart part) {
        parts.put(key, part);
    }
    
    protected synchronized QueryTemplatePart getPart(String key) {
        return parts.get(key);
    }
    
    protected void addTemplateAs(QueryTemplatePart part, String... keys) {
        for (String k: keys) {
            addTemplate(k, part);
        }
    }
    
    @Override
    public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
        QueryTemplatePart tmplPart = getPart(key);
        if (tmplPart == null) {
            tmplPart = autoPart(key);
            if (tmplPart == null) return null;
            addTemplate(key, tmplPart);
        }
        return tmplPart.addPart(key, queryBuilder);
    }
    
    protected QueryTemplatePart autoPart(String key) {
        return null;
    }
}
