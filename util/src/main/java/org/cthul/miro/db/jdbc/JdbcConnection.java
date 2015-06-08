package org.cthul.miro.db.jdbc;

import org.cthul.miro.db.syntax.RequestString;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.MiQueryString;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.syntax.MiStatementString;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFunction;
import org.cthul.miro.futures.MiSupplier;
import org.cthul.miro.util.Closables;

/**
 *
 */
public class JdbcConnection implements MiConnection {
    
    private final MiFunction<MiSupplier<PreparedStatement>, ResultSet> fExecuteQuery = this::executeQuery;
    private final MiFunction<MiSupplier<PreparedStatement>, Long> fExecuteStmt = this::executeStatement;
    private final Supplier<Connection> connectionSupplier;
    private final Supplier<RequestString> queryDialect;

    public JdbcConnection(Supplier<Connection> connectionSupplier, Supplier<RequestString> queryDialect) {
        this.connectionSupplier = connectionSupplier;
        this.queryDialect = queryDialect;
    }
    
    public Connection getConnection() {
        return connectionSupplier.get();
    }

    @Override
    public MiQueryString newQuery() {
        return new JdbcQuery(this, queryDialect.get());
    }

    @Override
    public MiStatementString newStatement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public PreparedStatement prepareStatement(String sql) throws MiException {
        try {
            return getConnection()
                    .prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }

    public ResultSet executeQuery(PreparedStatement stmt) throws MiException {
        try {
            ResultSet rs = stmt.executeQuery();
            stmt.closeOnCompletion();
            return rs;
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }
    
    private ResultSet executeQuery(MiSupplier<PreparedStatement> stmt) throws MiException {
        try {
            return executeQuery(stmt.call());
        } catch (Throwable t) {
            throw Closables.exceptionAs(t, MiException.class);
        }
    }

    public MiAction<ResultSet> queryAction(MiSupplier<PreparedStatement> stmt) {
        return fExecuteQuery.asAction(QUERY_EXECUTOR, stmt);
    }
    
    public long executeStatement(PreparedStatement stmt) throws MiException {
        try {
            return stmt.executeLargeUpdate();
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }
    
    private long executeStatement(MiSupplier<PreparedStatement> stmt) throws MiException {
        try {
            return executeStatement(stmt.call());
        } catch (Throwable t) {
            throw Closables.exceptionAs(t, MiException.class);
        }
    }

    public MiAction<Long> stmtAction(MiSupplier<PreparedStatement> stmt) {
        return fExecuteStmt.asAction(QUERY_EXECUTOR, stmt);
    }
    
    private static final ExecutorService QUERY_EXECUTOR = Executors.newFixedThreadPool(3);
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    QUERY_EXECUTOR.shutdown();
                    QUERY_EXECUTOR.awaitTermination(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    QUERY_EXECUTOR.shutdownNow();
                }
            }
        });
    }
}
