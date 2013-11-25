package org.cthul.miro.query.sql;

import org.cthul.miro.query.parts.AttributeQueryPart;
import org.cthul.miro.query.parts.SelectableQueryPart;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.cthul.miro.query.api.*;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.jdbc.JdbcAdapter;
import org.cthul.miro.query.jdbc.JdbcQueryBuilder;
import org.cthul.miro.query.parts.ValuesQueryPart;
import org.cthul.miro.query.syntax.QueryStringBuilder;
import org.cthul.miro.query.syntax.QuerySyntax;
import static org.cthul.miro.query.sql.DataQueryPartType.WHERE;

public class AnsiSql implements QuerySyntax, JdbcAdapter {

    @Override
    public QueryStringBuilder newQueryStringBuilder(QueryType queryType) {
        return newQuery(queryType);
    }

    @Override
    public JdbcQueryBuilder newJdbcQueryBuilder(QueryType queryType) {
        return newQuery(queryType);
    }
    
    protected AbstractQuery newQuery(QueryType queryType) {
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
    
    protected abstract static class AbstractQuery implements QueryStringBuilder, JdbcQueryBuilder {

        private final Map<QueryPartType, List<QueryPart>> parts = new HashMap<>();

        public AbstractQuery() {
        }
        
        protected synchronized List<QueryPart> getParts(QueryPartType type) {
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
            if (isEmpty(type)) {
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

        @Override
        public String getQueryString() {
            StringBuilder sb = new StringBuilder();
            buildQuery(sb);
            return sb.toString();
        }
        
        protected abstract void buildQuery(StringBuilder sql);

        protected void buildQuery(QueryPartType type, String first, String sep, StringBuilder sql) {
            buildQuery(type, first, sep, "", sql);
        }
        
        protected void buildQuery(QueryPartType type, String begin, String sep, String end, StringBuilder sql) {
            List<QueryPart> list = parts.get(type);
            if (list == null || list.isEmpty()) return;
            boolean first = true;
            sql.append(begin);
            for (QueryPart qp: list) {
                if (first) first = false;
                else sql.append(sep);
                qp.appendSqlTo(sql);
            }
            sql.append(end);
        }

        @Override
        public int getBatchCount() {
            return 0;
        }

        @Override
        public List<Object> getArguments(int batch) {
            List<Object> args = new ArrayList<>();
            collectArguments(args, batch);
            return args;
        }

        protected abstract void collectArguments(List<Object> args, int batch);
        
        protected void collectArguments(List<Object> args, QueryPartType... types) {
            for (QueryPartType t: types) {
                if (isEmpty(t)) continue;
                for (QueryPart qp: getParts(t)) {
                    qp.appendArgsTo(args);
                }
            }
        }

        @Override
        public ResultSet execute(Connection connection) throws SQLException {
            PreparedStatement ps = newPreparedStatement(connection);
            int batchCount = getBatchCount();
            if (batchCount == 0) {
                List<Object> args = getArguments(0);
                setArgs(ps, args);
                ps.execute();
            } else {
                for (int i = 0; i < batchCount; i++) {
                    List<Object> args = getArguments(i);
                    setArgs(ps, args);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            return result(ps);
        }
        
        protected PreparedStatement newPreparedStatement(Connection connection) throws SQLException {
            return connection.prepareStatement(getQueryString());
        }
        
        protected void setArgs(PreparedStatement stmt, List<Object> args) throws SQLException {
            int i = 0;
            for (Object o: args) {
                stmt.setObject(++i, o);
            }
        }
        
        protected ResultSet result(PreparedStatement stmt) throws SQLException {
            return stmt.getResultSet();
        }
    }
    
    private static class SelectQuery extends AbstractQuery {
        @Override
        public QueryType getQueryType() {
            return DataQueryType.SELECT;
        }
        
        @Override
        public void addPart(QueryPart part) {
            QueryPartType t = part.getPartType();
            if (t instanceof DataQueryPartType) {
                switch ((DataQueryPartType) t) {
                    case SELECT:
                    case ATTRIBUTE:
                        addPart(DataQueryPartType.SELECT, part);
                        return;
                    case TABLE:
                    case JOIN:
                    case WHERE:
                    case GROUP_BY:
                    case HAVING:
                    case ORDER_BY:
                        addPart(t, part);
                        return;
                }
            }
            if (t == OtherQueryPartType.VIRTUAL) {
                return;
            }
            throw new IllegalArgumentException("Unsupported part type: " + t);
        }

        @Override
        protected void buildQuery(StringBuilder sql) {
            ensureNotEmpty(DataQueryPartType.SELECT);
            ensureNotEmpty(DataQueryPartType.TABLE);
            buildQuery(DataQueryPartType.SELECT, "SELECT ", ", ", sql);
            buildQuery(DataQueryPartType.TABLE, " FROM ", ", ", sql);
            buildQuery(DataQueryPartType.JOIN, " ", " ", sql);
            buildQuery(DataQueryPartType.WHERE, " WHERE ", " AND ", sql);
            buildQuery(DataQueryPartType.GROUP_BY, " GROUP BY ", ", ", sql);
            buildQuery(DataQueryPartType.HAVING, " HAVING ", " AND ", sql);
            buildQuery(DataQueryPartType.ORDER_BY, " ORDER BY ", ", ", sql);
        }

        @Override
        protected void collectArguments(List<Object> args, int batch) {
            collectArguments(args, 
                    DataQueryPartType.SELECT,
                    DataQueryPartType.TABLE,
                    DataQueryPartType.JOIN,
                    DataQueryPartType.WHERE,
                    DataQueryPartType.GROUP_BY,
                    DataQueryPartType.HAVING,
                    DataQueryPartType.ORDER_BY);
        }
    }
    
    private static class InsertQuery extends AbstractQuery {
        private final List<String> attributes = new ArrayList<>();
        private final List<SelectableQueryPart> values = new ArrayList<>();
        @Override
        public QueryType getQueryType() {
            return DataQueryType.INSERT;
        }
        
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
                        return;
                    case VALUES:
                    case SUBQUERY:
                        SelectableQueryPart sqp = (SelectableQueryPart) part;
                        values.add(sqp);
                        for (String a: attributes) {
                            sqp.selectAttribute(a);
                        }
                        addPart(t, part);
                        return;
                    case TABLE:
                    case JOIN:
                        addPart(t, part);
                        return;
                }
            }
            if (t == OtherQueryPartType.VIRTUAL) {
                return;
            }
            throw new IllegalArgumentException("Unsupported part type: " + t);
        }

        @Override
        protected void buildQuery(StringBuilder sql) {
            ensureNotEmpty(DataQueryPartType.ATTRIBUTE);
            ensureExactlyOne(DataQueryPartType.TABLE);
            buildQuery(DataQueryPartType.TABLE, "INSERT INTO ", ", ", sql);
            buildQuery(DataQueryPartType.ATTRIBUTE, "(", ",", ")", sql);

            int cValues = getCount(DataQueryPartType.VALUES);
            int cSubQry = getCount(DataQueryPartType.SUBQUERY);
            if ((cValues == 0 && cSubQry == 0) || (cValues > 0 && cSubQry > 0)) {
                throw new IllegalStateException(
                        "Expected only one of " + DataQueryPartType.VALUES +
                        " and " + DataQueryPartType.SUBQUERY);
            }
            if (cValues > 0) {
                buildQuery(DataQueryPartType.VALUES, " VALUES ", ", ", sql);
            } else {
                ensureExactlyOne(DataQueryPartType.SUBQUERY);
                buildQuery(DataQueryPartType.SUBQUERY, " ", "--", sql);
            }
        }

        @Override
        protected void collectArguments(List<Object> args, int batch) {
            collectArguments(args, 
                    DataQueryPartType.TABLE,
                    DataQueryPartType.ATTRIBUTE,
                    DataQueryPartType.VALUES,
                    DataQueryPartType.SUBQUERY);
        }
        
        @Override
        protected PreparedStatement newPreparedStatement(Connection connection) throws SQLException {
            return connection.prepareStatement(getQueryString(), Statement.RETURN_GENERATED_KEYS);
        }

        @Override
        protected ResultSet result(PreparedStatement stmt) throws SQLException {
            return stmt.getGeneratedKeys();
        }
    }
    
    private static class UpdateQuery extends AbstractQuery {
        private final List<String> setAttributes = new ArrayList<>();
        private final List<String> filterAttributes = new ArrayList<>();
        private final List<ValuesQueryPart> values = new ArrayList<>();
        @Override
        public QueryType getQueryType() {
            return DataQueryType.UPDATE;
        }
        
        @Override
        public synchronized void addPart(QueryPart part) {
            QueryPartType t = part.getPartType();
            if (t instanceof DataQueryPartType) {
                switch ((DataQueryPartType) t) {
                    case TABLE:
                    case JOIN:
                        addPart(t, part);
                        return;
                    case VALUES:
                        ValuesQueryPart v = (ValuesQueryPart) part;
                        for (String s: setAttributes) {
                            v.selectAttribute(s);
                        }
                        for (String f: filterAttributes) {
                            v.selectFilterValue(f);
                        }
                        values.add(v);
                        addPart(t, part);
                        return;
                    case SET:
                        if (t instanceof AttributeQueryPart) {
                            String a = ((AttributeQueryPart) t).getAttribute();
                            for (ValuesQueryPart vp: values) {
                                vp.selectAttribute(a);
                            }
                            setAttributes.add(a);
                        }
                        addPart(t, part);
                        return;
                    case WHERE:
                        if (t instanceof AttributeQueryPart) {
                            String a = ((AttributeQueryPart) t).getAttribute();
                            for (ValuesQueryPart vp: values) {
                                vp.selectAttribute(a);
                            }
                            setAttributes.add(a);
                        }
                        addPart(t, part);
                        return;
                }
            }
            if (t == OtherQueryPartType.VIRTUAL) {
                return;
            }
            throw new IllegalArgumentException("Unsupported part type: " + t);
        }

        @Override
        protected void buildQuery(StringBuilder sql) {
            ensureNotEmpty(DataQueryPartType.TABLE);
            ensureNotEmpty(DataQueryPartType.SET);
            buildQuery(DataQueryPartType.TABLE, "UPDATE ", ", ", sql);
            buildQuery(DataQueryPartType.JOIN, " ", " ", sql);
            buildQuery(DataQueryPartType.SET, " SET ", ", ", sql);
            buildQuery(DataQueryPartType.WHERE, " WHERE ", " AND ", sql);
        }

        @Override
        public int getBatchCount() {
            return values.size();
        }

        @Override
        protected void collectArguments(List<Object> args, int batch) {
            if (getBatchCount() == 0) {
                collectArguments(args,
                        DataQueryPartType.TABLE,
                        DataQueryPartType.JOIN,
                        DataQueryPartType.SET,
                        DataQueryPartType.WHERE);
            } else {
                collectArguments(args,
                        DataQueryPartType.TABLE,
                        DataQueryPartType.JOIN);
                List<Object> tuple = new ArrayList<>();
                values.get(batch).appendArgsTo(tuple);
                Iterator<Object> itTuple = tuple.iterator();
                for (QueryPart setPart: getParts(DataQueryPartType.SET)) {
                    if (setPart instanceof AttributeQueryPart) {
                        args.add(itTuple.next());
                    } else {
                        setPart.appendArgsTo(args);
                    }
                }
                assert !itTuple.hasNext();
                List<Object> filter = new ArrayList<>();
                values.get(batch).appendFilterValuesTo(filter);
                Iterator<Object> itFilter = filter.iterator();
                for (QueryPart wPart: getParts(DataQueryPartType.WHERE)) {
                    if (wPart instanceof AttributeQueryPart) {
                        args.add(itFilter.next());
                    } else {
                        wPart.appendArgsTo(args);
                    }
                }
                assert !itFilter.hasNext();
            }
        }
    }
    
    private static class DeleteQuery extends AbstractQuery {
        @Override
        public QueryType getQueryType() {
            return DataQueryType.DELETE;
        }

        @Override
        public void addPart(QueryPart part) {
            QueryPartType t = part.getPartType();
            if (t instanceof DataQueryPartType) {
                switch ((DataQueryPartType) t) {
                    case TABLE:
                    case JOIN:
                    case WHERE:
                        addPart(t, part);
                        return;
                }
            }
            if (t == OtherQueryPartType.VIRTUAL) {
                return;
            }
            throw new IllegalArgumentException("Unsupported part type: " + t);
        }

        @Override
        protected void buildQuery(StringBuilder sql) {
            ensureNotEmpty(DataQueryPartType.TABLE);
            buildQuery(DataQueryPartType.TABLE, "DELETE FROM ", ", ", sql);
            buildQuery(DataQueryPartType.JOIN, " ", " ", sql);
            buildQuery(DataQueryPartType.WHERE, " WHERE ", " AND ", sql);
        }

        @Override
        protected void collectArguments(List<Object> args, int batch) {
            collectArguments(args,
                    DataQueryPartType.TABLE,
                    DataQueryPartType.JOIN,
                    DataQueryPartType.WHERE);
        }
    }
}
