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
        return new SelectQuery();
    }

    public static InsertQuery newInsertQuery() {
        return new InsertQuery();
    }

    public static UpdateQuery newUpdateQuery() {
        return new UpdateQuery();
    }

    public static DeleteQuery newDeleteQuery() {
        return new DeleteQuery();
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
                case SELECT:
                    return (T) new SelectQuery();
                case INSERT:
                    return (T) new InsertQuery();
                case UPDATE:
                    return (T) new UpdateQuery();
                case DELETE:
                    return (T) new DeleteQuery();
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
                    return select(asSql(part));
                case TABLE:
                case SUBQUERY:
                    return from(asSql(part));
                case JOIN:
                    return join(asSql(part));
                case WHERE:
                    return where(asSql(part));
                case GROUP_BY:
                    return groupBy(asSql(part));
                case HAVING:
                    return having(asSql(part));
                case ORDER_BY:
                    return orderBy(asSql(part));
            }
            return null;
        }
        
        @Override
        public SelectQuery select(SqlQueryPart part) {
            addPart(T_SELECT, part);
            return this;
        }

        @Override
        public SelectQuery from(SqlQueryPart part) {
            addPart(T_FROM, part);
            return this;
        }

        @Override
        public SelectQuery join(SqlQueryPart part) {
            addPart(T_JOIN, part);
            return this;
        }

        @Override
        public SelectQuery where(SqlQueryPart part) {
            addPart(T_WHERE, part);
            return this;
        }

        @Override
        public SelectQuery groupBy(SqlQueryPart part) {
            addPart(T_GROUP_BY, part);
            return this;
        }

        @Override
        public SelectQuery having(SqlQueryPart part) {
            addPart(T_HAVING, part);
            return this;
        }

        @Override
        public SelectQuery orderBy(SqlQueryPart part) {
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
        
        private final List<SelectableQueryPart.Selector> source = new ArrayList<>();
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
            switch (type) {
                case TABLE:
                    return into(asSql(part));
                case ATTRIBUTE:
                    return attribute(asAttribute(part));
                case VALUES:
                    return values(asSelectable(part));
                case SUBQUERY:
                    return from(asSelectable(part));
            }
            return null;
        }
        
        @Override
        public InsertQuery into(SqlQueryPart part) {
            addPart(T_INTO, part);
            return this;
        }

        @Override
        public synchronized InsertQuery attribute(AttributeQueryPart part) {
            for (SelectableQueryPart.Selector s: source) {
                s.selectAttribute(part.getAttributeKey(), part.getColumn());
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
                        "Expected only one of 'VALUES' or 'FROM <subquery>'");
            }
        }
        
        private synchronized void addSource(int type, SelectableQueryPart part) {
            SelectableQueryPart.Selector sel = part.selector();
            ensureSourceType(type);
            source.add(sel);
            for (AttributeQueryPart a: attributes) {
                sel.selectAttribute(a.getAttributeKey(), a.getColumn());
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
                sql.append(a.getColumn());
            }
            sql.append(')');
            if (sourceType == T_VALUES) {
                String tuple = tupleSql();
                int count = source.size();
                for (int i = 0; i < count; i++) {
                    if (i == 0) sql.append(" VALUES ");
                    else sql.append(", ");
                    sql.append(tuple);
                }
//                buildQuery(source, " VALUES ", ", ", sql);
            } else {
                buildQuery(source, " FROM ", "--", sql);
            }
        }
        
        protected String tupleSql() {
            int l = attributes.size();
            StringBuilder sb = new StringBuilder(2+2*l);
            sb.append('(');
            for (int i = 0; i < l; i++) {
                if (i != 0) sb.append(',');
                sb.append('?');
            }
            return sb.append(')').toString();
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
        private List<ValuesQueryPart.Selector> values = null;

        public UpdateQuery() {
            super(4);
        }
        
        @Override
        public QueryType<UpdateBuilder<?>> getQueryType() {
            return DataQuery.UPDATE;
        }

        @Override
        protected UpdateQuery addPart(DataQueryPart type, QueryPart part) {
            switch (type) {
                case TABLE:
                    return update(asSql(part));
                case SET:
                    return set(asSql(part));
                case ATTRIBUTE:
                    return set(asAttribute(part));
                case WHERE:
                    return where(asSql(part));
                case FILTER_ATTRIBUTE:
                    return where(asAttribute(part));
                case VALUES:
                    return values(asValues(part));
            }
            return null;
        }

        @Override
        public UpdateQuery update(SqlQueryPart part) {
            addPart(T_UPDATE, part);
            return this;
        }

        @Override
        public UpdateQuery join(SqlQueryPart part) {
            addPart(T_JOIN, part);
            return this;
        }

        @Override
        public UpdateQuery set(SqlQueryPart part) {
            addPart(T_SET, part);
            return this;
        }

        @Override
        public UpdateQuery where(SqlQueryPart part) {
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
                for (ValuesQueryPart.Selector v: values) {
                    v.selectAttribute(part.getAttributeKey(), part.getColumn());
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
                for (ValuesQueryPart.Selector v: values) {
                    v.selectAttribute(part.getAttributeKey(), part.getColumn());
                }
            }
            return this;
        }

        @Override
        public synchronized UpdateQuery values(ValuesQueryPart tuple) {
            ValuesQueryPart.Selector sel = tuple.selector();
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(sel);
            if (setAttributes != null) {
                for (AttributeQueryPart a: setAttributes) {
                    sel.selectAttribute(a.getAttributeKey(), a.getColumn());
                }
            }
            if (filterAttributes != null) {
                for (AttributeQueryPart f: filterAttributes) {
                    sel.selectAttribute(f.getAttributeKey(), f.getColumn());
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
                    sql.append(a.getColumn()).append(" = ?");
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
                    sql.append(a.getColumn()).append(" = ?");
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
    
    public static class DeleteQuery extends AbstractQueryBuilder<DeleteBuilder<?>> implements DeleteBuilder<DeleteBuilder<?>> {

        private static final int T_FROM =   0;
        private static final int T_JOIN =   1;
        private static final int T_WHERE =  2;
        
        private List<AttributeQueryPart> filterAttributes = null;
        private List<ValuesQueryPart.Selector> values = null;

        public DeleteQuery() {
            super(3);
        }
        
        @Override
        public QueryType<DeleteBuilder<?>> getQueryType() {
            return DataQuery.DELETE;
        }

        @Override
        protected DeleteQuery addPart(DataQueryPart type, QueryPart part) {
            switch (type) {
                case TABLE:
                    return from(asSql(part));
                case WHERE:
                    return where(asSql(part));
                case FILTER_ATTRIBUTE:
                    return where(asAttribute(part));
                case VALUES:
                    return values(asValues(part));
            }
            return null;
        }

        @Override
        public DeleteQuery from(SqlQueryPart part) {
            addPart(T_FROM, part);
            return this;
        }

        @Override
        public DeleteQuery join(SqlQueryPart part) {
            addPart(T_JOIN, part);
            return this;
        }

        @Override
        public DeleteQuery where(SqlQueryPart part) {
            addPart(T_WHERE, part);
            return this;
        }

        @Override
        public synchronized DeleteQuery where(AttributeQueryPart part) {
            if (filterAttributes == null) {
                filterAttributes = new ArrayList<>();
            }
            filterAttributes.add(part);
            if (values != null) {
                for (ValuesQueryPart.Selector v: values) {
                    v.selectAttribute(part.getAttributeKey(), part.getColumn());
                }
            }
            return this;
        }

        @Override
        public synchronized DeleteQuery values(ValuesQueryPart tuple) {
            ValuesQueryPart.Selector sel = tuple.selector();
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(sel);
            if (filterAttributes != null) {
                for (AttributeQueryPart f: filterAttributes) {
                    sel.selectAttribute(f.getAttributeKey(), f.getColumn());
                }
            }
            return this;
        }
        
        @Override
        protected void buildQuery(StringBuilder sql) {
            ensureNotEmpty(T_FROM, "FROM");
            if (filterAttributes != null) {
                if (values == null) {
                    throw new IllegalStateException(
                            "VALUES required");
                }
            }
            buildQuery(T_FROM, "DELETE FROM ", ", ", sql);
            buildQuery(T_JOIN, " ", " ", sql);
            
            String moreWhere = " WHERE ";
            if (filterAttributes != null) {
                sql.append(" WHERE ");
                moreWhere = " AND ";
                boolean first = true;
                for (AttributeQueryPart a: filterAttributes) {
                    if (first) first = false;
                    else sql.append(" AND ");
                    sql.append(a.getColumn()).append(" = ?");
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
            collectArgumentsOfParts(args, T_FROM);
            collectArgumentsOfParts(args, T_JOIN);
            if (values != null) {
                values.get(batch).appendFilterValuesTo(args);
            }
            collectArgumentsOfParts(args, T_WHERE);
        }
    }
}
