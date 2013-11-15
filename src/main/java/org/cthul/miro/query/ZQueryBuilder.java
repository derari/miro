package org.cthul.miro.query;

import java.util.*;

/**
 * Composes an SQL query string from query parts.
 */
public abstract class ZQueryBuilder {
    
    private final List<String> selectedFields = new ArrayList<>();
    private final List<QueryPart> selectParts = new ArrayList<>();
    private QueryPart fromPart = null;
    private List<QueryPart> joinParts = null;
    private List<QueryPart> whereParts = null;
    private List<QueryPart> groupParts = null;
    private List<QueryPart> havingParts = null;
    private List<QueryPart> orderParts = null;
    private final List<QueryPart> allParts = new ArrayList<>();
    private final Map<String, QueryPart> parts = new HashMap<>();
    
    private List<QueryPart> publicAllParts = null;
    private List<String> publicSelectedFields = null;
    

    public ZQueryBuilder() {
    }
    
    protected List<String> selectedFields() {
        return selectedFields;
    }
    
    protected List<QueryPart> selectParts() {
        return selectParts;
    }
    
    protected List<QueryPart> joinParts() {
        if (joinParts == null) {
            joinParts = new ArrayList<>();
        }
        return joinParts;
    }
    
    protected List<QueryPart> whereParts() {
        if (whereParts == null) {
            whereParts = new ArrayList<>();
        }
        return whereParts;
    }
    
    protected List<QueryPart> groupParts() {
        if (groupParts == null) {
            groupParts = new ArrayList<>();
        }
        return groupParts;
    }
    
    protected List<QueryPart> havingParts() {
        if (havingParts == null) {
            havingParts = new ArrayList<>();
        }
        return havingParts;
    }
    
    protected List<QueryPart> orderParts() {
        if (orderParts == null) {
            orderParts = new ArrayList<>();
        }
        return orderParts;
    }

    protected List<QueryPart> allParts() {
        return allParts;
    }
    
    protected Map<String, QueryPart> parts() {
        return parts;
    }

    public List<QueryPart> getAllParts() {
        if (publicAllParts == null) {
            publicAllParts = Collections.unmodifiableList(allParts());
        }
        return publicAllParts;
    }

    public List<String> getSelectedFields() {
        if (publicSelectedFields == null) {
            publicSelectedFields = Collections.unmodifiableList(selectedFields());
        }
        return publicSelectedFields;
    }
    
    protected boolean hasJoinParts() {
        return joinParts != null && joinParts.size() > 0;
    }
    
    protected boolean hasWhereParts() {
        return whereParts != null && whereParts.size() > 0;
    }
    
    protected boolean hasGroupParts() {
        return groupParts != null && groupParts.size() > 0;
    }
    
    protected boolean hasHavingParts() {
        return havingParts != null && havingParts.size() > 0;
    }
    
    protected boolean hasOrderParts() {
        return orderParts != null && orderParts.size() > 0;
    }
    
    protected void select(QueryPart... selectParts) {
        for (QueryPart s: selectParts) {
            select(s);
        }
    }
    
    protected QueryPart select(QueryPart sp) {
        selectedFields.add(sp.key);
        return internalSelect(sp);
    }
    
    protected QueryPart internalSelect(QueryPart sp) {
        selectParts.add(sp);
        return other(sp);
    }
    
    protected QueryPart from(QueryPart fp) {
        fromPart = fp;
        return other(fp);
    }
    
    protected QueryPart join(QueryPart jp) {
        joinParts().add(jp);
        return other(jp);
    }
    
    protected QueryPart where(QueryPart wp) {
        whereParts().add(wp);
        return other(wp);
    }

    protected QueryPart groupBy(QueryPart gp) {
        groupParts().add(gp);
        return other(gp);
    }
    
    protected QueryPart having(QueryPart hp) {
        havingParts().add(hp);
        return other(hp);
    }

    protected QueryPart orderBy(QueryPart op) {
        orderParts().add(op);
        return other(op);
    }
    
    protected QueryPart other(QueryPart ap) {
        parts.put(ap.key, ap);
        allParts.add(ap);
        return ap;
    }
    
    protected QueryPart addPart(QueryPart qp) throws IllegalArgumentException {
        if (qp == null) return null;
        switch (qp.getPartType()) {
            case SELECT:
                return select(qp);
            case SELECT_INTERNAL:
                return internalSelect(qp);
            case FROM:
                return from(qp);
            case JOIN:
                return join(qp);
            case WHERE:
                return where(qp);
            case GROUP:
                return groupBy(qp);
            case HAVING:
                return having(qp);
            case ORDER:
                return orderBy(qp);
            case OTHER:
                return other(qp);
            case VIRTUAL:
                return qp;
            default:
                throw new IllegalArgumentException(String.valueOf(qp.getPartType()));
        }
    }
    
    public void put(String key) {
        put(key, (Object[]) null);
    }
    
    public void put(String key, Object... args) {
        String subKey;
        int dot = key.indexOf('.');
        if (dot < 0) {
            subKey = null;
        } else {
            subKey = key.substring(dot+1);
            key = key.substring(0, dot);
        }
        put2(key, subKey, args);
    }
    
    public void put2(String key, String subKey, Object... args) {
        QueryPart qp = parts.get(key);
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
    
    // SELECT f FROM t JOIN t2 WHERE w
    // UPDATE t JOIN t2 SET f=x, g=y WHERE w
    // INSERT INTO t(f,g) VALUES x,y
    // DELETE FROM t WHERE w
    
    public String getQueryString() {
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

    private void appendParts(StringBuilder query, List<QueryPart> parts, String initial, String sep) {
        if (parts == null || parts.isEmpty()) {
            return;
        }
        query.append(initial);
        boolean first = true;
        for (QueryPart qp: parts) {
            if (first) first = false;
            else query.append(sep);
            qp.appendTo(query);
        }
    }

    public List<Object> getArguments() {
        final List<Object> result = new ArrayList<>();
        addArgs(result, selectParts);
        fromPart.addArgumentsTo(result);
        addArgs(result, joinParts);
        addArgs(result, whereParts);
        addArgs(result, groupParts);
        addArgs(result, havingParts);
        addArgs(result, orderParts);
        return result;
    }
    
    private void addArgs(List<Object> result, List<QueryPart> parts) {
        if (parts == null || parts.isEmpty()) {
            return;
        }
        for (QueryPart qp: parts) {
            qp.addArgumentsTo(result);
        }
    }

    @Override
    public String toString() {
        return getQueryString();
    }
    
    public static abstract class QueryPart {
        
        protected final String key;
        protected Object[] arguments = null;
        
        public QueryPart(String key) {
            this.key = key;
        }
        
        public void appendTo(StringBuilder query) {
            throw new UnsupportedOperationException();
        }

        public void put(String subKey, Object[] args) {
            if (subKey.isEmpty()) {
                if (args != null) {
                    arguments = args;
                }
            } else {
                throw new IllegalArgumentException(
                        key + " has no sub-keys: " + subKey);
            }
        }
        
        public void addArgumentsTo(List<Object> args) {
            if (arguments != null) {
                args.addAll(Arrays.asList(arguments));
            }
        }
        
        public PartType getPartType() {
            return PartType.UNKNOWN;
        }

        @Override
        public String toString() {
            return key + ": " + getPartType();
        }
    }
    
    public static class CustomPart extends QueryPart {
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
        public void appendTo(StringBuilder query) {
            query.append(definition);
        }

        @Override
        public PartType getPartType() {
            return type;
        }

        @Override
        public String toString() {
            return super.toString() + " " + definition;
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
        OTHER   (-1),
        VIRTUAL (-1),
        UNKNOWN (-1);
        
        public final int position;

        private PartType(int position) {
            this.position = position;
        }
    }
}
