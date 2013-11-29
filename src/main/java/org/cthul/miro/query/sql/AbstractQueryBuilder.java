package org.cthul.miro.query.sql;

import java.sql.*;
import java.util.*;
import org.cthul.miro.query.adapter.JdbcQuery;
import org.cthul.miro.query.adapter.QueryBuilder;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.api.OtherQueryPart;
import org.cthul.miro.query.api.QueryPartType;
import org.cthul.miro.query.parts.*;

public abstract class AbstractQueryBuilder<Builder extends QueryBuilder<? extends Builder>>
                implements QueryString<Builder>, JdbcQuery<Builder>, QueryBuilder<Builder> {
    
    private final List<SqlQueryPart>[] parts;

    public AbstractQueryBuilder(int partCount) {
        parts = new List[partCount];
    }
    
    @Override
    public Builder getBuilder() {
        return (Builder) this;
    }

    @Override
    public Builder add(QueryPartType type, QueryPart part) {
        if (type == OtherQueryPart.VIRTUAL) {
            return (Builder) this;
        }
        if (type instanceof DataQueryPart && 
                addPart(DataQueryPart.get(type), part) != null) {
            return (Builder) this;
        }
        throw new IllegalArgumentException(
                "Unexpected type " + type + ": " + part);
    }
    
    protected abstract Builder addPart(DataQueryPart type, QueryPart part);

    protected <T> T cast(Class<T> clazz, Object o) {
        if (clazz.isInstance(o)) {
            return clazz.cast(o);
        }
        throw new IllegalArgumentException(
                "Expected " + clazz + ", got " + o);
    }
        
    protected SqlQueryPart asSql(QueryPart part) {
        return cast(SqlQueryPart.class, part);
    }

    protected SelectableQueryPart asSelectable(QueryPart part) {
        return cast(SelectableQueryPart.class, part);
    }

    protected ValuesQueryPart asValues(QueryPart part) {
        return cast(ValuesQueryPart.class, part);
    }

    protected AttributeQueryPart asAttribute(QueryPart part) {
        return cast(AttributeQueryPart.class, part);
    }
    
    protected synchronized List<SqlQueryPart> getParts(int type) {
        List<SqlQueryPart> list = parts[type];
        if (list == null) {
            list = new ArrayList<>();
            parts[type] = list;
        }
        return list;
    }

    protected synchronized void addPart(int type, SqlQueryPart part) {
        getParts(type).add(part);
    }

    protected int getCount(int type) {
        List<SqlQueryPart> list = parts[type];
        return list == null ? 0 : list.size();
    }

    protected boolean isEmpty(int type) {
        return getCount(type) == 0;
    }

    protected void ensureNotEmpty(int type, String name) {
        if (isEmpty(type)) {
            throw new IllegalStateException(name + " part must not be empty");
        }
    }
    
    protected void ensureEmpty(int type) {
        if (!isEmpty(type)) {
            throw new IllegalStateException(type + " part must be empty");
        }
    }

    protected void ensureExactlyOne(int type) {
        if (getCount(type) != 1) {
            throw new IllegalStateException("Expected exactly one " + type + " part");
        }
    }

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder();
        buildQuery(sb);
        return sb.toString();
    }

    protected abstract void buildQuery(StringBuilder sql);

    protected void buildQuery(int type, String first, String sep, StringBuilder sql) {
        buildQuery(type, first, sep, "", sql);
    }

    protected void buildQuery(int type, String begin, String sep, String end, StringBuilder sql) {
        buildQuery(parts[type], begin, sep, end, sql);
    }
    
    protected void buildQuery(List<? extends SqlQueryPart> list, String begin, String sep, StringBuilder sql) {
        buildQuery(list, begin, sep, "", sql);
    }
    
    protected void buildQuery(List<? extends SqlQueryPart> list, String begin, String sep, String end, StringBuilder sql) {
        if (list == null || list.isEmpty()) {
            return;
        }
        boolean first = true;
        sql.append(begin);
        for (SqlQueryPart qp : list) {
            if (first) {
                first = false;
            } else {
                sql.append(sep);
            }
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

    protected void collectArgumentsOfParts(List<Object> args, int... types) {
        for (int t : types) {
            if (isEmpty(t)) {
                continue;
            }
            for (SqlQueryPart qp : getParts(t)) {
                qp.appendArgsTo(args);
            }
        }
    }
    
    protected void collectArgumentsOfParts(List<Object> args, List<? extends SqlQueryPart>... parts) {
        for (List<? extends SqlQueryPart> l: parts) {
            for (SqlQueryPart qp : l) {
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
        if (args == null) return;
        int length = args.size();
        for (int i = 0; i < length; i++) {
            setArg(stmt, i + 1, args.get(i));
        }
    }

    protected void setArg(PreparedStatement stmt, int i, Object arg) throws SQLException {
        if (arg == null) {
            stmt.setObject(i, null);
        } else if (arg instanceof String) {
            stmt.setString(i, (String) arg);
        } else if (arg instanceof Number) {
            Number n = (Number) arg;
            if (n instanceof Integer) {
                stmt.setInt(i, n.intValue());
            } else if (n instanceof Long) {
                stmt.setLong(i, n.longValue());
            } else {
                throw new IllegalArgumentException(
                        arg.getClass().getCanonicalName() + " "
                        + String.valueOf(arg));
            }
        } else {
            throw new IllegalArgumentException(
                    arg.getClass().getCanonicalName() + " "
                    + String.valueOf(arg));
        }
    }

    protected ResultSet result(PreparedStatement stmt) throws SQLException {
        return stmt.getResultSet();
    }
}
