package org.cthul.miro.query.sql;

import java.sql.*;
import java.util.ArrayList;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.parts.QueryPart;
import java.util.List;
import org.cthul.miro.query.adapter.JdbcAdapter;
import org.cthul.miro.query.adapter.JdbcQuery;
import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.adapter.QuerySyntax;
import org.cthul.miro.query.parts.*;

public class AnsiSql implements QuerySyntax, JdbcAdapter {

    @Override
    public <Builder> QueryString<Builder> newQueryString(QueryType<Builder> queryType) {
        return newQuery(queryType);
    }

    @Override
    public <Builder> JdbcQuery<Builder> newJdbcQuery(QueryType<Builder> queryType) {
        return newQuery(queryType);
    }
    
    protected <T> T newQuery(QueryType<?> queryType) {
        if (queryType instanceof DataQuery.Type) {
            switch ((DataQuery.Type) queryType) {
                case INSERT:
                    return (T) new InsertQuery();
                case SELECT:
                    return (T) new SelectQuery();
                case UPDATE:
                    return (T) new UpdateQuery();
            }
        }
        throw new IllegalArgumentException("Unsupported query type: " + queryType);
    }
    
    public static class SelectQuery extends AbstractQueryAdapter<SelectQueryBuilder> implements SelectQueryBuilder {
        
        private static final int T_SELECT = 0;
        private static final int T_FROM =   1;
        private static final int T_JOIN =   2;
        private static final int T_WHERE =  3;
        private static final int T_GROUP_BY = 4;
        private static final int T_HAVING = 5;
        private static final int T_ORDER_BY = 6;

        public SelectQuery() {
            super(7);
        }

        @Override
        public QueryType<SelectQueryBuilder> getQueryType() {
            return DataQuery.SELECT;
        }
        
        @Override
        public SelectQuery select(QueryPart part) {
            addPart(T_SELECT, part);
            return this;
        }

        @Override
        public SelectQuery from(QueryPart part) {
            addPart(T_FROM, part);
            return this;
        }

        @Override
        public SelectQuery join(QueryPart part) {
            addPart(T_JOIN, part);
            return this;
        }

        @Override
        public SelectQuery where(QueryPart part) {
            addPart(T_WHERE, part);
            return this;
        }

        @Override
        public SelectQuery groupBy(QueryPart part) {
            addPart(T_GROUP_BY, part);
            return this;
        }

        @Override
        public SelectQuery having(QueryPart part) {
            addPart(T_HAVING, part);
            return this;
        }

        @Override
        public SelectQuery orderBy(QueryPart part) {
            addPart(T_ORDER_BY, part);
            return this;
        }
        
        @Override
        protected void buildQuery(StringBuilder sql) {
            ensureNotEmpty(T_SELECT);
            ensureNotEmpty(T_FROM);
            buildQuery(T_SELECT, "SELECT ", ", ", sql);
            buildQuery(T_FROM, " FROM ", ", ", sql);
            buildQuery(T_JOIN, " ", " ", sql);
            buildQuery(T_WHERE, " WHERE ", " AND ", sql);
            buildQuery(T_GROUP_BY, " GROUP BY ", ", ", sql);
            buildQuery(T_HAVING, " HAVING ", " AND ", sql);
            buildQuery(T_ORDER_BY, " ORDER BY ", ", ", sql);
        }

        @Override
        protected void collectArguments(List<Object> args, int batch) {
            collectArgumentsOfParts(args, 
                    T_SELECT,
                    T_FROM,
                    T_JOIN,
                    T_WHERE,
                    T_GROUP_BY,
                    T_HAVING,
                    T_ORDER_BY);
        }
    }
    
    public static class InsertQuery extends AbstractQueryAdapter<InsertQueryBuilder> implements InsertQueryBuilder {
        
        private static final int T_INTO =  0;
        private static final int T_VALUES = 1;
        private static final int T_FROM =  2;
        
        private final List<SelectableQueryPart> source = new ArrayList<>();
        private final List<String> attributes = new ArrayList<>();
        private int sourceType = -1;

        public InsertQuery() {
            super(1);
        }

        @Override
        public QueryType<InsertQueryBuilder> getQueryType() {
            return DataQuery.INSERT;
        }

        @Override
        public InsertQuery into(QueryPart part) {
            addPart(T_INTO, part);
            return this;
        }

        @Override
        public synchronized InsertQueryBuilder attribute(String attribute) {
            for (SelectableQueryPart s: source) {
                s.selectAttribute(attribute);
            }
            attributes.add(attribute);
            return this;
        }
        
        private void ensureSourceType(int type) {
            if (sourceType == -1) {
                sourceType = type;
                return;
            }
            if (sourceType != type) {
                throw new IllegalStateException(
                        "Expected on one of 'VALUES' or 'FROM <subquery>'");
            }
        }
        
        private synchronized void addSource(int type, SelectableQueryPart part) {
            ensureSourceType(type);
            source.add(part);
            for (String a: attributes) {
                part.selectAttribute(a);
            }
        }

        @Override
        public InsertQuery values(SelectableQueryPart part) {
            addSource(T_VALUES, part);
            return this;
        }

        @Override
        public InsertQuery from(SelectableQueryPart part) {
            if (!source.isEmpty()) {
                throw new IllegalStateException(
                        "Only one source query allowed");
            }
            addSource(T_FROM, part);
            return this;
        }
        
        @Override
        protected void buildQuery(StringBuilder sql) {
            ensureExactlyOne(T_INTO);
            buildQuery(T_INTO, "INSERT INTO ", ", ", sql);
            
            boolean first = true;
            sql.append('(');
            for (String a: attributes) {
                if (first) first = false;
                else sql.append(',');
                sql.append(a);
            }
            sql.append(')');
            if (sourceType == T_VALUES) {
                buildQuery(source, " VALUES ", ", ", sql);
            } else {
                buildQuery(source, " FROM ", "--", sql);
            }
        }

        @Override
        protected void collectArguments(List<Object> args, int batch) {
            collectArgumentsOfParts(args, T_INTO);
            collectArgumentsOfParts(args, source);
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
    
    private static class UpdateQuery extends AbstractQueryAdapter<UpdateQueryBuilder> implements UpdateQueryBuilder {

        private static final int T_UPDATE = 0;
        private static final int T_JOIN =   1;
        private static final int T_SET =    2;
        private static final int T_WHERE =  3;
        
        private List<String> setAttributes = null;
        private List<String> filterAttributes = null;
        private List<ValuesQueryPart> values = null;

        public UpdateQuery() {
            super(4);
        }
        
        @Override
        public QueryType<UpdateQueryBuilder> getQueryType() {
            return DataQuery.UPDATE;
        }

        @Override
        public UpdateQuery update(QueryPart part) {
            addPart(T_UPDATE, part);
            return this;
        }

        @Override
        public UpdateQuery join(QueryPart part) {
            addPart(T_JOIN, part);
            return this;
        }

        @Override
        public UpdateQuery set(QueryPart part) {
            addPart(T_SET, part);
            return this;
        }

        @Override
        public UpdateQuery where(QueryPart part) {
            addPart(T_WHERE, part);
            return this;
        }

        @Override
        public synchronized UpdateQuery set(String attribute) {
            if (setAttributes == null) {
                setAttributes = new ArrayList<>();
            }
            setAttributes.add(attribute);
            if (values != null) {
                for (ValuesQueryPart v: values) {
                    v.selectAttribute(attribute);
                }
            }
            return this;
        }

        @Override
        public synchronized UpdateQuery where(String attribute) {
            if (filterAttributes == null) {
                filterAttributes = new ArrayList<>();
            }
            filterAttributes.add(attribute);
            if (values != null) {
                for (ValuesQueryPart v: values) {
                    v.selectFilterValue(attribute);
                }
            }
            return this;
        }

        @Override
        public synchronized UpdateQueryBuilder values(ValuesQueryPart tuple) {
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(tuple);
            if (setAttributes != null) {
                for (String a: setAttributes) {
                    tuple.selectAttribute(a);
                }
            }
            if (filterAttributes != null) {
                for (String f: filterAttributes) {
                    tuple.selectFilterValue(f);
                }
            }
            return this;
        }
        
        @Override
        protected void buildQuery(StringBuilder sql) {
            ensureNotEmpty(T_UPDATE);
            if (setAttributes != null || filterAttributes != null) {
                if (values == null) {
                    throw new IllegalStateException(
                            "VALUES part required");
                }
            }
            if (setAttributes == null) {
                ensureNotEmpty(T_SET);
            }
            buildQuery(T_UPDATE, "UPDATE ", ", ", sql);
            buildQuery(T_JOIN, " ", " ", sql);
            
            String moreSet = " SET ";
            if (setAttributes != null) {
                sql.append(" SET ");
                moreSet = ", ";
                boolean first = true;
                for (String a: setAttributes) {
                    if (first) first = false;
                    else sql.append(", ");
                    sql.append(a).append(" = ?");
                }
            }
            buildQuery(T_SET, moreSet, ", ", sql);
            
            String moreWhere = " WHERE ";
            if (filterAttributes != null) {
                sql.append(" WHERE ");
                moreWhere = " AND ";
                boolean first = true;
                for (String a: filterAttributes) {
                    if (first) first = false;
                    else sql.append(" AND ");
                    sql.append(a).append(" = ?");
                }
            }
            buildQuery(T_WHERE, moreWhere, " AND ", sql);
        }

        @Override
        public int getBatchCount() {
            return values == null ? 0 : values.size();
        }

        @Override
        protected void collectArguments(List<Object> args, int batch) {
            collectArgumentsOfParts(args, T_UPDATE);
            collectArgumentsOfParts(args, T_JOIN);
            if (values != null) {
                values.get(batch).appendArgsTo(args);
            }
            collectArgumentsOfParts(args, T_SET);
            if (values != null) {
                values.get(batch).appendFilterValuesTo(args);
            }
            collectArgumentsOfParts(args, T_WHERE);
        }
    }
//    
//    private static class UpdateQuery extends AbstractQueryAdapter {
//        private final List<String> setAttributes = new ArrayList<>();
//        private final List<String> filterAttributes = new ArrayList<>();
//        private final List<ValuesQueryPart> values = new ArrayList<>();
//        @Override
//        public QueryType getQueryType() {
//            return DataQuery.UPDATE;
//        }
//        
//        @Override
//        public synchronized void addPart(QueryPart part) {
//            QueryPartType t = part.getPartType();
//            if (t instanceof DataQueryPartType) {
//                switch ((DataQueryPartType) t) {
//                    case TABLE:
//                    case JOIN:
//                        addPart(t, part);
//                        return;
//                    case VALUES:
//                        ValuesQueryPart v = (ValuesQueryPart) part;
//                        for (String s: setAttributes) {
//                            v.selectAttribute(s);
//                        }
//                        for (String f: filterAttributes) {
//                            v.selectFilterValue(f);
//                        }
//                        values.add(v);
//                        addPart(t, part);
//                        return;
//                    case SET:
//                        if (t instanceof AttributeQueryPart) {
//                            String a = ((AttributeQueryPart) t).getAttribute();
//                            for (ValuesQueryPart vp: values) {
//                                vp.selectAttribute(a);
//                            }
//                            setAttributes.add(a);
//                        }
//                        addPart(t, part);
//                        return;
//                    case WHERE:
//                        if (t instanceof AttributeQueryPart) {
//                            String a = ((AttributeQueryPart) t).getAttribute();
//                            for (ValuesQueryPart vp: values) {
//                                vp.selectAttribute(a);
//                            }
//                            setAttributes.add(a);
//                        }
//                        addPart(t, part);
//                        return;
//                }
//            }
//            if (t == OtherQueryPartType.VIRTUAL) {
//                return;
//            }
//            throw new IllegalArgumentException("Unsupported part type: " + t);
//        }
//
//        @Override
//        protected void buildQuery(StringBuilder sql) {
//            ensureNotEmpty(DataQueryPartType.TABLE);
//            ensureNotEmpty(DataQueryPartType.SET);
//            buildQuery(DataQueryPartType.TABLE, "UPDATE ", ", ", sql);
//            buildQuery(DataQueryPartType.JOIN, " ", " ", sql);
//            buildQuery(DataQueryPartType.SET, " SET ", ", ", sql);
//            buildQuery(DataQueryPartType.WHERE, " WHERE ", " AND ", sql);
//        }
//
//        @Override
//        public int getBatchCount() {
//            return values.size();
//        }
//
//        @Override
//        protected void collectArgumentsOfParts(List<Object> args, int batch) {
//            if (getBatchCount() == 0) {
//                collectArgumentsOfParts(args,
//                        DataQueryPartType.TABLE,
//                        DataQueryPartType.JOIN,
//                        DataQueryPartType.SET,
//                        DataQueryPartType.WHERE);
//            } else {
//                collectArgumentsOfParts(args,
//                        DataQueryPartType.TABLE,
//                        DataQueryPartType.JOIN);
//                List<Object> tuple = new ArrayList<>();
//                values.get(batch).appendArgsTo(tuple);
//                Iterator<Object> itTuple = tuple.iterator();
//                for (QueryPart setPart: getParts(DataQueryPartType.SET)) {
//                    if (setPart instanceof AttributeQueryPart) {
//                        args.add(itTuple.next());
//                    } else {
//                        setPart.appendArgsTo(args);
//                    }
//                }
//                assert !itTuple.hasNext();
//                List<Object> filter = new ArrayList<>();
//                values.get(batch).appendFilterValuesTo(filter);
//                Iterator<Object> itFilter = filter.iterator();
//                for (QueryPart wPart: getParts(DataQueryPartType.WHERE)) {
//                    if (wPart instanceof AttributeQueryPart) {
//                        args.add(itFilter.next());
//                    } else {
//                        wPart.appendArgsTo(args);
//                    }
//                }
//                assert !itFilter.hasNext();
//            }
//        }
//    }
//    
//    private static class DeleteQuery extends AbstractQueryAdapter {
//        @Override
//        public QueryType getQueryType() {
//            return DataQuery.DELETE;
//        }
//
//        @Override
//        public void addPart(QueryPart part) {
//            QueryPartType t = part.getPartType();
//            if (t instanceof DataQueryPartType) {
//                switch ((DataQueryPartType) t) {
//                    case TABLE:
//                    case JOIN:
//                    case WHERE:
//                        addPart(t, part);
//                        return;
//                }
//            }
//            if (t == OtherQueryPartType.VIRTUAL) {
//                return;
//            }
//            throw new IllegalArgumentException("Unsupported part type: " + t);
//        }
//
//        @Override
//        protected void buildQuery(StringBuilder sql) {
//            ensureNotEmpty(DataQueryPartType.TABLE);
//            buildQuery(DataQueryPartType.TABLE, "DELETE FROM ", ", ", sql);
//            buildQuery(DataQueryPartType.JOIN, " ", " ", sql);
//            buildQuery(DataQueryPartType.WHERE, " WHERE ", " AND ", sql);
//        }
//
//        @Override
//        protected void collectArgumentsOfParts(List<Object> args, int batch) {
//            collectArgumentsOfParts(args,
//                    DataQueryPartType.TABLE,
//                    DataQueryPartType.JOIN,
//                    DataQueryPartType.WHERE);
//        }
//    }
}
