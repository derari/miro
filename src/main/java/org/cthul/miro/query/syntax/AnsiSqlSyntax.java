package org.cthul.miro.query.syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.query.api.AttributeQueryPart;
import org.cthul.miro.query.api.DataQueryPartType;
import org.cthul.miro.query.api.QueryPart;
import org.cthul.miro.query.api.QueryPartType;
import org.cthul.miro.query.api.SelectableQueryPart;
import static org.cthul.miro.query.api.DataQueryPartType.WHERE;

public class AnsiSqlSyntax implements SqlSyntax {

    @Override
    public SqlBuilder newQuery(QueryType queryType) {
        if (queryType instanceof DataQueryType) {
            switch ((DataQueryType) queryType) {
                case INSERT:
                    return new InsertQuery();
                case SELECT:
                    return new SelectQuery();
                case UPDATE:
                    return new UpdateQuery();
                case DELETE:
                    return new DeleteQuery();
            }
        }
        throw new IllegalArgumentException("Unsupported query type: " + queryType);
    }
    
    private abstract static class AbstractQuery implements SqlBuilder {

        private final Map<QueryPartType, List<QueryPart>> parts = new HashMap<>();

        public AbstractQuery() {
        }
        
        private List<QueryPart> getParts(QueryPartType type) {
            List<QueryPart> list = parts.get(type);
            if (list == null) {
                list = new ArrayList<>();
                parts.put(type, list);
            }
            return list;
        }
        
        protected synchronized void addPart(QueryPartType type, QueryPart part) {
            getParts(type).add(part);
        }
        
        protected int getCount(QueryPartType type) {
            List<QueryPart> list = parts.get(type);
            return list == null ? 0 : list.size();
        }
        
        protected boolean isEmpty(QueryPartType type) {
            return getCount(type) == 0;
        }
        
        protected void ensureNotEmpty(QueryPartType type) {
            if (!isEmpty(type)) {
                throw new IllegalStateException(
                        type + " part must not be empty");
            }
        }
        
        protected void ensureExactlyOne(QueryPartType type) {
            if (getCount(type) != 1) {
                throw new IllegalStateException(
                        "Expected exactly one " + type + " part");
            }
        }

        protected void buildQuery(QueryPartType type, String first, String sep, StringBuilder sql, List<Object> args) {
            buildQuery(type, first, sep, "", sql, args);
        }
        
        protected void buildQuery(QueryPartType type, String begin, String sep, String end, StringBuilder sql, List<Object> args) {
            List<QueryPart> list = parts.get(type);
            if (list == null || list.isEmpty()) return;
            boolean first = true;
            sql.append(begin);
            for (QueryPart qp: list) {
                if (first) first = true;
                else sql.append(sep);
                qp.appendSqlTo(sql);
                qp.appendArgsTo(args);
            }
            sql.append(end);
        }
    }
    
    private static class SelectQuery extends AbstractQuery {
        @Override
        public void addPart(QueryPart part) {
            QueryPartType t = part.getPartType();
            if (t instanceof DataQueryPartType) {
                switch ((DataQueryPartType) t) {
                    case SELECT:
                    case ATTRIBUTE:
                        addPart(DataQueryPartType.SELECT, part);
                        break;
                    case TABLE:
                    case JOIN:
                    case WHERE:
                    case GROUP_BY:
                    case HAVING:
                    case ORDER_BY:
                        addPart(t, part);
                        break;
                }
            }
            throw new IllegalArgumentException("Unsupported part type: " + t);
        }

        @Override
        public void buildQuery(StringBuilder sql, List<Object> args) {
            ensureNotEmpty(DataQueryPartType.SELECT);
            ensureNotEmpty(DataQueryPartType.TABLE);
            buildQuery(DataQueryPartType.SELECT, "SELECT ", ", ", sql, args);
            buildQuery(DataQueryPartType.TABLE, " FROM ", ", ", sql, args);
            buildQuery(DataQueryPartType.JOIN, " ", " ", sql, args);
            buildQuery(DataQueryPartType.WHERE, " WHERE ", " AND ", sql, args);
            buildQuery(DataQueryPartType.GROUP_BY, " GROUP BY ", ", ", sql, args);
            buildQuery(DataQueryPartType.HAVING, " HAVING ", " AND ", sql, args);
            buildQuery(DataQueryPartType.ORDER_BY, " ORDER BY ", ", ", sql, args);
        }
    }
    
    private static class InsertQuery extends AbstractQuery {
        private final List<String> attributes = new ArrayList<>();
        private final List<SelectableQueryPart> values = new ArrayList<>();
        @Override
        public synchronized void addPart(QueryPart part) {
            QueryPartType t = part.getPartType();
            if (t instanceof DataQueryPartType) {
                switch ((DataQueryPartType) t) {
                    case ATTRIBUTE:
                        AttributeQueryPart aqp = (AttributeQueryPart) part;
                        String attribute = aqp.getAttribute();
                        attributes.add(attribute);
                        for (SelectableQueryPart v: values) {
                            v.selectAttribute(attribute);
                        }
                        addPart(t, part);
                        break;
                    case VALUES:
                    case SUBQUERY:
                        SelectableQueryPart sqp = (SelectableQueryPart) part;
                        values.add(sqp);
                        for (String a: attributes) {
                            sqp.selectAttribute(a);
                        }
                        addPart(t, part);
                        break;
                    case TABLE:
                    case JOIN:
                        addPart(t, part);
                        break;
                }
            }
            throw new IllegalArgumentException("Unsupported part type: " + t);
        }

        @Override
        public void buildQuery(StringBuilder sql, List<Object> args) {
            ensureNotEmpty(DataQueryPartType.ATTRIBUTE);
            ensureExactlyOne(DataQueryPartType.TABLE);
            buildQuery(DataQueryPartType.TABLE, "INSERT INTO ", ", ", sql, args);
            buildQuery(DataQueryPartType.ATTRIBUTE, "(", ",", ")", sql, args);

            int cValues = getCount(DataQueryPartType.VALUES);
            int cSubQry = getCount(DataQueryPartType.SUBQUERY);
            if ((cValues == 0 && cSubQry == 0) || (cValues > 0 && cValues > 0)) {
                throw new IllegalStateException(
                        "Expected only one of " + DataQueryPartType.VALUES +
                        " and " + DataQueryPartType.SUBQUERY);
            }
            if (cValues > 0) {
                buildQuery(DataQueryPartType.VALUES, " VALUES ", ", ", sql, args);
            } else {
                ensureExactlyOne(DataQueryPartType.SUBQUERY);
                buildQuery(DataQueryPartType.SUBQUERY, " ", "--", sql, args);
            }
        }
    }
    
    private static class UpdateQuery extends AbstractQuery {
        @Override
        public void addPart(QueryPart part) {
            QueryPartType t = part.getPartType();
            if (t instanceof DataQueryPartType) {
                switch ((DataQueryPartType) t) {
                    case TABLE:
                    case JOIN:
                    case WHERE:
                    case SET:
                        addPart(t, part);
                        break;
                }
            }
            throw new IllegalArgumentException("Unsupported part type: " + t);
        }

        @Override
        public void buildQuery(StringBuilder sql, List<Object> args) {
            ensureNotEmpty(DataQueryPartType.TABLE);
            ensureNotEmpty(DataQueryPartType.SET);
            buildQuery(DataQueryPartType.TABLE, "UPDATE ", ", ", sql, args);
            buildQuery(DataQueryPartType.JOIN, " ", " ", sql, args);
            buildQuery(DataQueryPartType.SET, " SET ", ", ", sql, args);
            buildQuery(DataQueryPartType.WHERE, " WHERE ", " AND ", sql, args);
        }
    }
    
    private static class DeleteQuery extends AbstractQuery {
        @Override
        public void addPart(QueryPart part) {
            QueryPartType t = part.getPartType();
            if (t instanceof DataQueryPartType) {
                switch ((DataQueryPartType) t) {
                    case TABLE:
                    case JOIN:
                    case WHERE:
                        addPart(t, part);
                        break;
                }
            }
            throw new IllegalArgumentException("Unsupported part type: " + t);
        }

        @Override
        public void buildQuery(StringBuilder sql, List<Object> args) {
            ensureNotEmpty(DataQueryPartType.TABLE);
            buildQuery(DataQueryPartType.TABLE, "DELETE FROM ", ", ", sql, args);
            buildQuery(DataQueryPartType.JOIN, " ", " ", sql, args);
            buildQuery(DataQueryPartType.WHERE, " WHERE ", " AND ", sql, args);
        }
    }
}
