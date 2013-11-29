package org.cthul.miro;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Future;

import org.cthul.miro.util.FutureBase;

/**
 * Will create a {@link PreparedStatement java.sql.PreparedStatement} that
 * will execute using a {@link MiConnection}.
 */
public class MiPreparedStatement {

    private final MiConnection cnn;
    //private final jdbc
    private final String sql;

    public MiPreparedStatement(MiConnection cnn, String sql) {
        this.cnn = cnn;
        this.sql = sql;
    }

    public MiFuture<ResultSet> submitQuery(Object[] args) {
        QueryRun query = new QueryRun(this, args);
        query.setFutureDelegate(cnn.submitQuery(query));
        return query.getResult();
    }

    public ResultSet executeQuery(Object[] args) throws SQLException {
        PreparedStatement stmt = cnn.preparedStatement(sql);
        synchronized (stmt) {
            fillArgs(stmt, args);
            return stmt.executeQuery();
        }
    }

    private static abstract class StmtRun<T> implements Runnable {

        protected final MiPreparedStatement stmt;
        protected final Object[] args;
        private final QueryResult<T> result = new QueryResult<>();

        public StmtRun(MiPreparedStatement stmt, Object[] args) {
            this.stmt = stmt;
            this.args = args;
        }

        void setFutureDelegate(Future<?> f) {
            result.cancelDelegate = f;
        }
        
        QueryResult getResult() {
            return result;
        }

        @Override
        public void run() {
            final T value;
            try {
                value = execute();
            } catch (Throwable t) {
                result.setException(t);
                if (t instanceof Error) {
                    throw (Error) t;
                }
                if (t instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                return;
            }
            result.setValue(value);
        }
        
        protected abstract T execute() throws Throwable;
    }
    
    private static class QueryRun extends StmtRun<ResultSet> {

        public QueryRun(MiPreparedStatement stmt, Object[] args) {
            super(stmt, args);
        }

        @Override
        protected ResultSet execute() throws Throwable {
            return stmt.executeQuery(args);
        }
    }

    private static class QueryResult<T> extends FutureBase<T> {
        
        Future<?> cancelDelegate;
        
        public QueryResult() {
            super(null);
        }
        
        @Override
        protected Future<?> getCancelDelegate() {
            return cancelDelegate;
        }
        
        @Override
        protected void setException(Throwable exception) {
            super.setException(exception);
        }
        
        @Override
        protected void setValue(T value) {
            super.setValue(value);
        }
    }

    private static void fillArgs(PreparedStatement stmt, Object[] args) throws SQLException {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            setArg(stmt, i + 1, args[i]);
        }
    }

    private static void setArg(PreparedStatement stmt, int i, Object arg) throws SQLException {
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
}
