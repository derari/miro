package org.cthul.miro.query;

import java.util.*;
import org.cthul.miro.dml.DataQueryKey;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.adapter.QueryAdapter;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.adapter.*;
import org.cthul.miro.query.sql.DataQueryPart;
import org.cthul.miro.query.template.QueryTemplateProvider;

public class AbstractQuery {
    
    private final Map<Object, QueryPart> parts = new HashMap<>();
    private final List<QueryPart> partList = new ArrayList<>();
    private final List<QueryPartType> typeList = new ArrayList<>();
    private final LinkedHashSet<String> resultAttributes = new LinkedHashSet<>();
    private final QueryType<?> queryType;
    private final QueryTemplate template;
    private Internal internal = null;
    private int initialized = -1;
    private boolean hasAttributes = false;

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
//        if (resultAttributes.isEmpty()) {
        if (!hasAttributes) {
            hasAttributes = true;
            put("*");
        }
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
        if (internal == null) {
            internal = newInternal();
        }
        return internal;
    }

    protected List<String> getResultAttributes() {
        return new ArrayList<>(resultAttributes);
    }
    
    protected List<QueryPart> getParts() {
        return partList;
    }
    
    protected QueryPart addPartFromTemplate(Object key) {
        if (template == null) return null;
        return template.addPart(key, internal());
    }
    
    protected QueryPart addUnknownPart(Object key) {
        return addPartFromTemplate(key);
    }
    
    protected void addResultAttribute(String key) {
        resultAttributes.add(key);
    }
    
    protected synchronized void addPart(QueryPartType partType, QueryPart part) {
        if (partType == DataQueryPart.ATTRIBUTE) {
            hasAttributes = true;
        }
        typeList.add(partType);
        partList.add(part);
        parts.put(part.getKey(), part);
    }
    
    protected synchronized QueryPart part(Object key) {
        ensureInitialized();
        QueryPart part = parts.get(key);
        if (part == null) {
            part = addUnknownPart(key);
            if (part == null) {
                throw new IllegalArgumentException("Unknown key " + key);
            }
        }
        return part;
    }
    
    protected void put(Object key) {
        part(key);
//        int dot = key.indexOf('.');
//        if (dot < 0) {
//            part(key);
//        } else {
//            put2(key.substring(0, dot), key.substring(dot+1), NO_ARGS);
//        }
    }

    protected void put(Object key, Object... args) {
        put2(key, null, args);
    }

    protected void put2(Object key, Object subkey, Object... args) {
//        int dot = key.indexOf('.');
//        if (dot > 0) {
//            if (subkey.isEmpty()) {
//                subkey = key.substring(dot+1);
//            } else {
//                subkey = key.substring(dot+1) + "." + subkey;
//            }
//            key = key.substring(0, dot);
//        }
        part(key).put(subkey, args);
    }
    
    protected void putAll(String... keys) {
        for (String k: keys) {
            put(k);
        }
    }
    
    protected void ensureInitialized() {
        if (initialized > 0) return;
        synchedEnsureInitialized();
    }
    
    private synchronized void synchedEnsureInitialized() {
        if (initialized >= 0) return;
        initialized = 0;
        initialize();
        initialized = 1;
    }
    
    protected void initialize() {
        put(DataQueryKey.PUT_ALWAYS);
    }
    
    protected Internal newInternal() {
        return new Internal();
    }
    
//    private static final Object[] NO_ARGS = {};
    
    protected class Internal implements InternalQueryBuilder {

        @Override
        public QueryType<?> getQueryType() {
            return AbstractQuery.this.getQueryType();
        }

        @Override
        public void put(Object key) {
            AbstractQuery.this.put(key);
        }

        @Override
        public void put(Object key, Object... args) {
            AbstractQuery.this.put(key, args);
        }

        @Override
        public void put2(Object key, Object subkey, Object... args) {
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
        public boolean hasPart(Object key) {
            return AbstractQuery.this.parts.containsKey(key);
        }

        @Override
        public void addResultAttribute(String key) {
            AbstractQuery.this.addResultAttribute(key);
        }

        @Override
        public void addPart(QueryPartType partType, QueryPart part) {
            AbstractQuery.this.addPart(partType, part);
        }
    }
}
