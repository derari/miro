package org.cthul.miro.query.sql;

import java.sql.*;
import java.util.*;
import org.cthul.miro.query.adapter.JdbcQuery;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.adapter.QueryString;

abstract class AbstractQueryAdapter<Builder>
                implements QueryString<Builder>, JdbcQuery<Builder> {
    
    private final List<QueryPart>[] parts;

    public AbstractQueryAdapter(int partCount) {
        parts = new List[partCount];
    }
    
    @Override
    public Builder getBuilder() {
        return (Builder) this;
    }

    protected synchronized List<QueryPart> getParts(int type) {
        List<QueryPart> list = parts[type];
        if (list == null) {
            list = new ArrayList<>();
            parts[type] = list;
        }
        return list;
    }

    protected synchronized void addPart(int type, QueryPart part) {
        getParts(type).add(part);
    }

    protected int getCount(int type) {
        List<QueryPart> list = parts[type];
        return list == null ? 0 : list.size();
    }

    protected boolean isEmpty(int type) {
        return getCount(type) == 0;
    }

    protected void ensureNotEmpty(int type) {
        if (isEmpty(type)) {
            throw new IllegalStateException(type + " part must not be empty");
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
    
    protected void buildQuery(List<? extends QueryPart> list, String begin, String sep, StringBuilder sql) {
        buildQuery(list, begin, sep, "", sql);
    }
    
    protected void buildQuery(List<? extends QueryPart> list, String begin, String sep, String end, StringBuilder sql) {
        if (list == null || list.isEmpty()) {
            return;
        }
        boolean first = true;
        sql.append(begin);
        for (QueryPart qp : list) {
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
            for (QueryPart qp : getParts(t)) {
                qp.appendArgsTo(args);
            }
        }
    }
    
    protected void collectArgumentsOfParts(List<Object> args, List<? extends QueryPart>... parts) {
        for (List<? extends QueryPart> l: parts) {
            for (QueryPart qp : l) {
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
        for (Object o : args) {
            stmt.setObject(++i, o);
        }
    }

    protected ResultSet result(PreparedStatement stmt) throws SQLException {
        return stmt.getResultSet();
    }
}
