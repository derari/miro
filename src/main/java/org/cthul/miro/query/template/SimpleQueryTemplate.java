package org.cthul.miro.query.template;

import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.query.api.*;
import org.cthul.miro.query.parts.QueryPart;

public class SimpleQueryTemplate implements QueryTemplate {
    
    private final ParentTemplatePart addParentPart;
    private final Map<String, QueryTemplatePart> parts = new HashMap<>();

    public SimpleQueryTemplate() {
        this(null);
    }

    public SimpleQueryTemplate(QueryTemplate parent) {
        this.addParentPart = parent == null ? null : new ParentTemplatePart(parent);
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
            if (tmplPart == null) {
                return parentPart(key, queryBuilder);
            }
            addTemplate(key, tmplPart);
        }
        return tmplPart.addPart(key, queryBuilder);
    }
    
    protected QueryTemplatePart autoPart(String key) {
        return null;
    }
    
    protected QueryPart parentPart(String key, InternalQueryBuilder queryBuilder) {
        if (addParentPart == null) return null;
        QueryPart part = addParentPart.addPart(key, queryBuilder);
        if (part == null) return null;
        addTemplate(key, addParentPart);
        return part;
    }
    
    private static class ParentTemplatePart implements QueryTemplatePart {
        private final QueryTemplate parent;
        public ParentTemplatePart(QueryTemplate parent) {
            this.parent = parent;
        }
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            return parent.addPart(key, queryBuilder);
        }
    }
}
