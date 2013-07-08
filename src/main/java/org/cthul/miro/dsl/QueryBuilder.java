package org.cthul.miro.dsl;

import java.io.Serializable;
import java.util.*;
import org.cthul.miro.MiConnection;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.MultiValueAdapter;
import org.cthul.miro.map.ResultBuilder.ValueAdapter;
import org.cthul.miro.util.SqlUtils;
import org.cthul.miro.util.ValueAdapterInstance;

/**
 *
 */
public class QueryBuilder<Entity> extends MappedStatement<Entity> {
    
    private final List<String> selectedFields = new ArrayList<>();
    private final List<QueryPart<Entity>> selectParts = new ArrayList<>();
    private QueryPart<Entity> fromPart = null;
    private final List<QueryPart<Entity>> joinParts = new ArrayList<>();
    private final List<QueryPart<Entity>> whereParts = new ArrayList<>();
    private final List<QueryPart<Entity>> groupParts = new ArrayList<>();
    private final List<QueryPart<Entity>> havingParts = new ArrayList<>();
    private final List<QueryPart<Entity>> orderParts = new ArrayList<>();
    private final List<QueryPart<Entity>> valueAdapters = new ArrayList<>();
    protected final Map<String, QueryPart<Entity>> parts = new HashMap<>();

    public QueryBuilder(MiConnection cnn, Mapping<Entity> mapping) {
        super(cnn, mapping);
    }
    
    protected void select(String... selectClause) {
        for (String s: selectClause) {
            select(s);
        }
    }
    
    protected void select(String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            QueryPart<Entity> sp = new CustomPart<>(part[0], part[1]);
            select(sp);
        }
    }
    
    protected void select(QueryPart<Entity>... selectParts) {
        for (QueryPart<Entity> s: selectParts) {
            select(s);
        }
    }
    
    protected void select(QueryPart<Entity> selectPart) {
        selectedFields.add(selectPart.key);
        internalSelect(selectPart);
    }
    
    protected void internalSelect(QueryPart<Entity> selectPart) {
        parts.put(selectPart.key, selectPart);
        selectParts.add(selectPart);
        valueAdapters.add(selectPart);
    }
    
    protected void from(String from) {
        QueryPart<Entity> fp = new CustomPart<>("$$from", from);
        from(fp);
    }
    
    protected void from(QueryPart<Entity> fp) {
        parts.put(fp.key, fp);
        fromPart = fp;
        valueAdapters.add(fp);
    }
    
    protected void join(String join) {
        String id = "$$join" + joinParts.size();
        QueryPart<Entity> jp = new CustomPart<>(id, join);
        join(jp);
    }
    
    protected void join(QueryPart<Entity> jp) {
        parts.put(jp.key, jp);
        joinParts.add(jp);
        valueAdapters.add(jp);
    }
    
    protected void where(String where) {
        String id = "$$where" + whereParts.size();
        QueryPart<Entity> jp = new CustomPart<>(id, where);
        where(jp);
    }
    
    protected void where(QueryPart<Entity> wp) {
        parts.put(wp.key, wp);
        whereParts.add(wp);
        valueAdapters.add(wp);
    }

    protected void groupBy(String groupBy) {
        String id = "$$group" + groupParts.size();
        QueryPart<Entity> jp = new CustomPart<>(id, groupBy);
        groupBy(jp);
    }
    
    protected void groupBy(QueryPart<Entity> gp) {
        parts.put(gp.key, gp);
        groupParts.add(gp);
        valueAdapters.add(gp);
    }

    protected void having(String having) {
        String id = "$$having" + havingParts.size();
        QueryPart<Entity> jp = new CustomPart<>(id, having);
        having(jp);
    }
    
    protected void having(QueryPart<Entity> hp) {
        parts.put(hp.key, hp);
        havingParts.add(hp);
        valueAdapters.add(hp);
    }

    protected void orderBy(String order) {
        String id = "$$order" + orderParts.size();
        QueryPart<Entity> jp = new CustomPart<>(id, order);
        orderBy(jp);
    }
    
    protected void orderBy(QueryPart<Entity> op) {
        parts.put(op.key, op);
        orderParts.add(op);
        valueAdapters.add(op);
    }
    
    protected void adapter(ValueAdapter<? super Entity> va) {
        adapter(ValueAdapterInstance.asFactory(va));
    }
    
    protected void adapter(ValueAdapterFactory<? super Entity> vaf) {
        String id = "$$adapter" + orderParts.size();
        adapter(new ValueAdapterPart<>(id, vaf));
    }
    
    protected void adapter(Object va) {
        if (va instanceof QueryPart) {
            adapter((QueryPart<Entity>) va);
        } else if (va instanceof ValueAdapterFactory) {
            adapter((ValueAdapterFactory<? super Entity>) va);
        } else if (va instanceof ValueAdapter) {
            adapter((ValueAdapter<? super Entity>) va);
        } else {
            throw new IllegalArgumentException(String.valueOf(va));
        }
    }
    
    protected void adapter(QueryPart<Entity> ap) {
        parts.put(ap.key, ap);
        valueAdapters.add(ap);
    }
    
    protected void addPart(QueryPart qp) throws IllegalArgumentException {
        if (qp == null) return;
        switch (qp.getPartType()) {
            case SELECT:
                select(qp);
                break;
            case SELECT_INTERNAL:
                internalSelect(qp);
                break;
            case FROM:
                from(qp);
                break;
            case JOIN:
                join(qp);
                break;
            case WHERE:
                where(qp);
                break;
            case GROUP:
                groupBy(qp);
                break;
            case HAVING:
                having(qp);
                break;
            case ORDER:
                orderBy(qp);
                break;
            case VALUE_ADAPTER:
                adapter(qp);
                break;
            case VIRTUAL:
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(qp.getPartType()));
        }
    }
    
    @Override
    public void put(String key, String subKey, Object... args) {
        QueryPart<Entity> qp = parts.get(key);
        if (qp == null) {
            putUnknownKey(key, subKey, args);
        } else {
            if (subKey == null) subKey = "";
            qp.put(subKey, args);
        }
    }

    protected void putUnknownKey(String key, String subKey, Object[] args) {
        throw new IllegalArgumentException("Unknown key: " + key);
    }
    
    @Override
    protected String[] selectedFields() {
        return selectedFields.toArray(new String[selectedFields.size()]);
    }

    @Override
    protected String queryString() {
        final StringBuilder query = new StringBuilder();
        appendParts(query, selectParts, "SELECT ", ", ");
        query.append(" FROM ");
        fromPart.appendTo(query);
        appendParts(query, joinParts, " ", " ");
        appendParts(query, whereParts, " WHERE ", " AND ");
        appendParts(query, groupParts, " GROUP BY ", ", ");
        appendParts(query, havingParts, " HAVING ", " AND ");
        appendParts(query, orderParts, " ORDER BY ", ", ");
        return query.toString();
    }
    
    private void appendParts(StringBuilder query, List<QueryPart<Entity>> parts, String initial, String sep) {
        if (!parts.isEmpty()) {
            query.append(initial);
            boolean first = true;
            for (QueryPart qp: parts) {
                if (first) first = false;
                else query.append(sep);
                qp.appendTo(query);
            }
        }
    }

    @Override
    protected Object[] arguments() {
        final List<Object> result = new ArrayList<>();
        addArgs(result, selectParts);
        fromPart.addArguments(result);
        addArgs(result, joinParts);
        addArgs(result, whereParts);
        addArgs(result, groupParts);
        addArgs(result, havingParts);
        addArgs(result, orderParts);
        return result.toArray();
    }
    
    private void addArgs(List<Object> result, List<QueryPart<Entity>> parts) {
        for (QueryPart qp: parts) {
            qp.addArguments(result);
        }
    }

    @Override
    protected ValueAdapter<? super Entity> moreValueAdapters(MiConnection cnn, ValueAdapter<? super Entity> entityAdapter) {
        final List<ValueAdapter<? super Entity>> resultList = new ArrayList<>();
        resultList.add(entityAdapter);
        addAdapters(resultList, cnn, valueAdapters); // list of all parts
        if (resultList.size() > 1) {
            return MultiValueAdapter.join(resultList);
        }
        return entityAdapter;
    }
    
    private void addAdapters(List<ValueAdapter<? super Entity>> adapters, MiConnection cnn, List<QueryPart<Entity>> parts) {
        for (QueryPart<Entity> qp: parts) {
            qp.addValueAdapters(adapters, mapping, cnn);
        }
    }

    public static abstract class QueryPart<Entity> {
        
        protected final String key;
        protected Object[] arguments = null;
        
        public QueryPart(String key) {
            this.key = key;
        }
        
        public void appendTo(StringBuilder sb) {
            throw new UnsupportedOperationException();
        }

        public void put(String subKey, Object[] args) {
            if (subKey.isEmpty()) {
                arguments = args;
            } else {
                throw new IllegalArgumentException(
                        key + " has no sub-keys: " + subKey);
            }
        }
        
        public void addArguments(List<Object> args) {
            if (arguments != null) {
                args.addAll(Arrays.asList(arguments));
            }
        }
        
        public void addValueAdapters(List<ValueAdapter<? super Entity>> adapters, Mapping<Entity> m, MiConnection cnn) {
        }
        
        public PartType getPartType() {
            return PartType.UNKNOWN;
        }
    }
    
    public static class CustomPart<Entity> extends QueryPart<Entity> {
        private final String definition;
        private final PartType type;
        public CustomPart(String key, PartType type, String definition) {
            super(key);
            this.definition = definition;
            this.type = type;
        }
        public CustomPart(String key, String definition) {
            this(key, PartType.UNKNOWN, definition);
        }
        @Override
        public void appendTo(StringBuilder sb) {
            sb.append(definition);
        }

        @Override
        public PartType getPartType() {
            return type;
        }
    }
    
    public static class ValueAdapterPart<Entity> extends QueryPart<Entity> {
        private final List<String> attributes = new ArrayList<>();
        private final ValueAdapterFactory<? super Entity> f;

        public ValueAdapterPart(String key, ValueAdapterFactory<? super Entity> f) {
            super(key);
            this.f = f;
        }

        @Override
        public void put(String subKey, Object[] args) {
            if (!subKey.isEmpty() && args == null) {
                attributes.add(subKey);
            }
        }

        @Override
        public void addValueAdapters(List<ValueAdapter<? super Entity>> adapters, Mapping<Entity> m, MiConnection cnn) {
            ValueAdapter<? super Entity> va = f.newAdapter(m, cnn, attributes);
            adapters.add(va);
        }

        @Override
        public PartType getPartType() {
            return PartType.VALUE_ADAPTER;
        }
    }
    
    public static enum PartType {
        SELECT  (100),
        SELECT_INTERNAL (100),
        FROM    (200),
        JOIN    (300),
        WHERE   (400),
        GROUP   (500),
        HAVING  (600),
        ORDER   (700),
        VALUE_ADAPTER (-1),
        VIRTUAL (-1),
        UNKNOWN (-1);
        
        public final int position;

        private PartType(int position) {
            this.position = position;
        }
    }

}
