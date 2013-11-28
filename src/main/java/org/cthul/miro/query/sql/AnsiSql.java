package org.cthul.miro.query.sql;

import java.sql.*;
import java.util.ArrayList;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.parts.QueryPart;
import java.util.List;
import org.cthul.miro.query.adapter.*;
import org.cthul.miro.query.parts.*;

public class AnsiSql implements QuerySyntax, JdbcAdapter {
    
    private static final AnsiSql INSTANCE = new AnsiSql();
    
    public static AnsiSql getInstance() {
        return INSTANCE;
    }
    
    public static SelectQuery newSelectQuery() {
        return getInstance().newQueryBuilder(DataQuery.SELECT);
    }

    public static UpdateQuery newUpdateQuery() {
        return getInstance().newQueryBuilder(DataQuery.UPDATE);
    }

    public static InsertQuery newInsertQuery() {
        return getInstance().newQueryBuilder(DataQuery.INSERT);
    }

    @Override
    public <Builder extends QueryBuilder<? extends Builder>> Builder newQueryAdapter(QueryType<Builder> queryType) {
        return newQueryBuilder(queryType);
    }

    @Override
    public <Builder extends QueryBuilder<? extends Builder>> QueryString<Builder> newQueryString(QueryType<? super Builder> queryType) {
        return newQueryBuilder(queryType);
    }

    @Override
    public <Builder extends QueryBuilder<? extends Builder>> JdbcQuery<Builder> newJdbcQuery(QueryType<? super Builder> queryType) {
        return newQueryBuilder(queryType);
    }
    
    protected <T> T newQueryBuilder(QueryType<?> queryType) {
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
    
    public static class SelectQuery extends AbstractQueryBuilder<SelectBuilder<?>> implements SelectBuilder<SelectBuilder<?>> {
        
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
        public QueryType<SelectBuilder<?>> getQueryType() {
            return DataQuery.SELECT;
        }

        @Override
        protected SelectQuery addPart(DataQueryPart type, QueryPart part) {
            switch (type) {
                case SELECT:
                case ATTRIBUTE:
                    return select(part);
                case TABLE:
                case SUBQUERY:
                    return from(part);
                case JOIN:
                    return join(part);
                case WHERE:
                    return where(part);
                case GROUP_BY:
                    return groupBy(part);
                case HAVING:
                    return having(part);
                case ORDER_BY:
                    return orderBy(part);
            }
            return null;
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
            ensureNotEmpty(T_SELECT, "SELECT");
            ensureNotEmpty(T_FROM, "FROM");
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
    
    public static class InsertQuery extends AbstractQueryBuilder<InsertBuilder<?>> implements InsertBuilder<InsertBuilder<?>> {
        
        private static final int T_INTO =  0;
        private static final int T_VALUES = 1;
        private static final int T_FROM =  2;
        
        private final List<SelectableQueryPart> source = new ArrayList<>();
        private final List<AttributeQueryPart> attributes = new ArrayList<>();
        private int sourceType = -1;

        public InsertQuery() {
            super(1);
        }

        @Override
        public QueryType<InsertBuilder<?>> getQueryType() {
            return DataQuery.INSERT;
        }

        @Override
        protected InsertQuery addPart(DataQueryPart type, QueryPart part) {
            if (part instanceof AttributeQueryPart) {
                switch (type) {
                    case ATTRIBUTE:
                    case VALUES:
                        return attribute(asAttribute(part));
                }
            } else {
                switch (type) {
                    case TABLE:
                        return into(part);
                    case VALUES:
                        return values(asSelectable(part));
                    case SUBQUERY:
                        return from(asSelectable(part));
                }
            }
            return null;
        }
        
        @Override
        public InsertQuery into(QueryPart part) {
            addPart(T_INTO, part);
            return this;
        }

        @Override
        public synchronized InsertQuery attribute(AttributeQueryPart part) {
            for (SelectableQueryPart s: source) {
                s.selectAttribute(part.getAttributeKey(), part.getSqlName());
            }
            attributes.add(part);
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
            for (AttributeQueryPart a: attributes) {
                part.selectAttribute(a.getAttributeKey(), a.getSqlName());
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
            for (AttributeQueryPart a: attributes) {
                if (first) first = false;
                else sql.append(',');
                sql.append(a.getSqlName());
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
    
    public static class UpdateQuery extends AbstractQueryBuilder<UpdateBuilder<?>> implements UpdateBuilder<UpdateBuilder<?>> {

        private static final int T_UPDATE = 0;
        private static final int T_JOIN =   1;
        private static final int T_SET =    2;
        private static final int T_WHERE =  3;
        
        private List<AttributeQueryPart> setAttributes = null;
        private List<AttributeQueryPart> filterAttributes = null;
        private List<ValuesQueryPart> values = null;

        public UpdateQuery() {
            super(4);
        }
        
        @Override
        public QueryType<UpdateBuilder<?>> getQueryType() {
            return DataQuery.UPDATE;
        }

        @Override
        protected UpdateQuery addPart(DataQueryPart type, QueryPart part) {
            if (part instanceof AttributeQueryPart) {
                switch (type) {
                    case ATTRIBUTE:
                    case SET:
                        return set(asAttribute(part));
                    case WHERE:
                        return where(asAttribute(part));
                }
            } else {
                switch (type) {
                    case TABLE:
                        return update(part);
                    case SET:
                        return set(part);
                    case WHERE:
                        return where(part);
                    case VALUES:
                        return values(asValues(part));
                }
            }
            return null;
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
        public synchronized UpdateQuery set(AttributeQueryPart part) {
            if (setAttributes == null) {
                setAttributes = new ArrayList<>();
            }
            setAttributes.add(part);
            if (values != null) {
                for (ValuesQueryPart v: values) {
                    v.selectAttribute(part.getAttributeKey(), part.getSqlName());
                }
            }
            return this;
        }

        @Override
        public synchronized UpdateQuery where(AttributeQueryPart part) {
            if (filterAttributes == null) {
                filterAttributes = new ArrayList<>();
            }
            filterAttributes.add(part);
            if (values != null) {
                for (ValuesQueryPart v: values) {
                    v.selectAttribute(part.getAttributeKey(), part.getSqlName());
                }
            }
            return this;
        }

        @Override
        public synchronized UpdateQuery values(ValuesQueryPart tuple) {
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(tuple);
            if (setAttributes != null) {
                for (AttributeQueryPart a: setAttributes) {
                    tuple.selectAttribute(a.getAttributeKey(), a.getSqlName());
                }
            }
            if (filterAttributes != null) {
                for (AttributeQueryPart f: filterAttributes) {
                    tuple.selectAttribute(f.getAttributeKey(), f.getSqlName());
                }
            }
            return this;
        }
        
        @Override
        protected void buildQuery(StringBuilder sql) {
            ensureNotEmpty(T_UPDATE, "UPDATE");
            if (setAttributes != null || filterAttributes != null) {
                if (values == null) {
                    throw new IllegalStateException(
                            "VALUES required");
                }
            }
            if (setAttributes == null) {
                ensureNotEmpty(T_SET, "SET");
            }
            buildQuery(T_UPDATE, "UPDATE ", ", ", sql);
            buildQuery(T_JOIN, " ", " ", sql);
            
            String moreSet = " SET ";
            if (setAttributes != null) {
                sql.append(" SET ");
                moreSet = ", ";
                boolean first = true;
                for (AttributeQueryPart a: setAttributes) {
                    if (first) first = false;
                    else sql.append(", ");
                    sql.append(a.getSqlName()).append(" = ?");
                }
            }
            buildQuery(T_SET, moreSet, ", ", sql);
            
            String moreWhere = " WHERE ";
            if (filterAttributes != null) {
                sql.append(" WHERE ");
                moreWhere = " AND ";
                boolean first = true;
                for (AttributeQueryPart a: filterAttributes) {
                    if (first) first = false;
                    else sql.append(" AND ");
                    sql.append(a.getSqlName()).append(" = ?");
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
//    private static class UpdateQuery extends AbstractQueryBuilder {
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
//    private static class DeleteQuery extends AbstractQueryBuilder {
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
