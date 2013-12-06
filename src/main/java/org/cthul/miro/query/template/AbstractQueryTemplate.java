package org.cthul.miro.query.template;

import org.cthul.miro.query.InternalQueryBuilder;
import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.query.parts.QueryPart;

public class AbstractQueryTemplate implements QueryTemplate {
    
    private final ParentTemplatePart addParentPart;
    private final Map<Object, QueryTemplatePart> parts = new HashMap<>();

    public AbstractQueryTemplate() {
        this(null);
    }

    public AbstractQueryTemplate(QueryTemplate parent) {
        this.addParentPart = parent == null ? null : new ParentTemplatePart(parent);
    }
    
    protected synchronized void addTemplate(Object key, QueryTemplatePart part) {
        parts.put(key, part);
    }
    
    protected synchronized QueryTemplatePart getPart(Object key) {
        return parts.get(key);
    }
    
    protected void addTemplateAs(QueryTemplatePart part, Object... keys) {
        for (Object k: keys) {
            addTemplate(k, part);
        }
    }
    
    @Override
    public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
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
    
    protected QueryTemplatePart autoPart(Object key) {
        return null;
    }
    
    protected QueryPart parentPart(Object key, InternalQueryBuilder queryBuilder) {
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
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            return parent.addPart(key, queryBuilder);
        }
    }
}
