package org.cthul.miro.query;

import java.util.ArrayList;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.parts.QueryPart;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.query.adapter.*;
import org.cthul.miro.query.api.InternalQueryBuilder;
import org.cthul.miro.query.api.QueryPartType;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.template.QueryTemplateProvider;

public class AbstractQuery {
    
    private final Internal internal = new Internal();
    private final Map<String, QueryPart> parts = new HashMap<>();
    private final List<QueryPart> partList = new ArrayList<>();
    private final List<QueryPartType> typeList = new ArrayList<>();
    private final QueryType<?> queryType;
    private final QueryTemplate template;

    public AbstractQuery(QueryType<?> queryType) {
        this(queryType, (QueryTemplate) null);
    }
    
    public AbstractQuery(QueryType<?> queryType, QueryTemplate template) {
        this.queryType = queryType;
        this.template = template;
    }
    
    public AbstractQuery(QueryType<?> type, QueryTemplateProvider templateProvider) {
        this(type, 
                templateProvider == null ? null : templateProvider.getTemplate(type));
    }

    protected <T extends QueryAdapter<?>> T getAdapter(DBAdapter dbAdapter) {
        QueryAdapter<?> a = dbAdapter.newQueryAdapter((QueryType) getQueryType());
        QueryBuilder<?> b = a.getBuilder();
        int len = partList.size();
        for (int i = 0; i < len; i++) {
            b.add(typeList.get(i), partList.get(i));
        }
        return (T) a;
    }
    
    protected QueryType<?> getQueryType() {
        return queryType;
    }
    
    protected InternalQueryBuilder internal() {
        return internal;
    }

    public List<QueryPart> getParts() {
        return partList;
    }
    
    protected QueryPart addPartFromTemplate(String key) {
        if (template == null) return null;
        return template.addPart(key, internal());
    }
    
    protected QueryPart addUnknownPart(String key) {
        return addPartFromTemplate(key);
    }
    
    protected synchronized void addPart(QueryPartType partType, QueryPart part) {
        typeList.add(partType);
        partList.add(part);
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
    
    protected void put(String key) {
        int dot = key.indexOf('.');
        if (dot < 0) {
            part(key);
        } else {
            put2(key.substring(0, dot), key.substring(dot+1), NO_ARGS);
        }
    }

    protected void put(String key, Object... args) {
        put2(key, "", args);
    }

    protected void put2(String key, String subkey, Object... args) {
        int dot = key.indexOf('.');
        if (dot > 0) {
            if (subkey.isEmpty()) {
                subkey = key.substring(dot+1);
            } else {
                subkey = key.substring(dot+1) + "." + subkey;
            }
            key = key.substring(0, dot);
        }
        part(key).put(subkey, args);
    }
    
    private static final Object[] NO_ARGS = {};
    
    private class Internal implements InternalQueryBuilder {

        @Override
        public QueryType<?> getQueryType() {
            return AbstractQuery.this.getQueryType();
        }

        @Override
        public void put(String key) {
            AbstractQuery.this.put(key);
        }

        @Override
        public void put(String key, Object... args) {
            AbstractQuery.this.put(key, args);
        }

        @Override
        public void put2(String key, String subkey, Object... args) {
            AbstractQuery.this.put2(key, subkey, args);
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
