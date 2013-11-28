package org.cthul.miro.query.api;

import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.parts.QueryPart;
import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.query.adapter.*;
import org.cthul.miro.query.template.QueryTemplateProvider;

/**
 *
 */
public class AbstractQuery implements Query {
    
    private final Internal internal = new Internal();
    private final Map<String, QueryPart> parts = new HashMap<>();
    private final QueryAdapter<?> adapter;
    private final QueryBuilder<?> builder;
    private final QueryTemplate template;

    public AbstractQuery(QueryAdapter<?> adapter) {
        this(adapter, null);
    }
    
    public AbstractQuery(QueryAdapter<?> adapter, QueryTemplate template) {
        this.adapter = adapter;
        this.builder = adapter.getBuilder();
        this.template = template;
    }
    
    public AbstractQuery(QueryType<?> type, DBAdapter adapter) {
        this(adapter.newQueryAdapter((QueryType) type), null);
    }
    
    public AbstractQuery(QueryType<?> type, DBAdapter adapter, QueryTemplateProvider templateProvider) {
        this(adapter.newQueryAdapter((QueryType) type), 
                templateProvider == null ? null : templateProvider.getTemplate(type));
    }

    public QueryAdapter<?> getAdapter() {
        return adapter;
    }

    protected QueryBuilder<?> getBuilder() {
        return builder;
    }
    
    protected QueryType<?> getQueryType() {
        return builder.getQueryType();
    }
    
    protected InternalQueryBuilder internal() {
        return internal;
    }
    
    protected QueryPart addPartFromTemplate(String key) {
        if (template == null) return null;
        return template.addPart(key, internal());
    }
    
    protected QueryPart addUnknownPart(String key) {
        return addPartFromTemplate(key);
    }
    
    protected synchronized void addPart(QueryPartType partType, QueryPart part) {
        partType.addPartTo(part, builder);
        parts.put(part.getKey(), part);
    }
    
    protected synchronized QueryPart part(String key) {
        QueryPart part = parts.get(key);
        if (part == null) {
            part = addUnknownPart(key);
            if (part == null) {
                throw new IllegalArgumentException("Unknown key " + key);
            }
        }
        return part;
    }
    
    @Override
    public void put(String key) {
        part(key);
    }

    @Override
    public void put(String key, Object... args) {
        put2(key, "", args);
    }

    @Override
    public void put2(String key, String subkey, Object... args) {
        part(key).put(subkey, args);
    }
    
    private class Internal implements InternalQueryBuilder {

        @Override
        public QueryType<?> getQueryType() {
            return builder.getQueryType();
        }

        @Override
        public void require(String key) {
            AbstractQuery.this.put(key);
        }

        @Override
        public String newKey(String hint) {
            hint = hint + "$$";
            if (!parts.keySet().contains(hint)) {
                return hint;
            }
            int i = 1;
            while (parts.keySet().contains(hint + i)) {
                i++;
            }
            return hint + i;
        }

        @Override
        public void addPart(QueryPartType partType, QueryPart part) {
            AbstractQuery.this.addPart(partType, part);
        }
    }
}
