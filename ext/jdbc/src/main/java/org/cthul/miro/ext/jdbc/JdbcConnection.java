package org.cthul.miro.ext.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.db.syntax.Syntax;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.function.MiFunction;
import org.cthul.miro.function.MiSupplier;
import org.cthul.miro.futures.MiFutures;
import org.cthul.miro.util.Closeables;
import org.cthul.miro.db.request.MiRequest;
import org.cthul.miro.db.request.MiQueryBuilder;
import org.cthul.miro.db.request.MiUpdateBuilder;

/**
 *
 */
public class JdbcConnection implements MiConnection {
    
    private final MiFunction<MiSupplier<PreparedStatement>, ResultSet> fExecuteQuery = this::executeQuery;
    private final MiFunction<MiSupplier<PreparedStatement>, Long> fExecuteUpdate = this::executeUpdate;
    private final ConnectionProvider connectionSupplier;
    private final Syntax syntax;

    public JdbcConnection(ConnectionProvider connectionSupplier, Syntax syntax) {
        this.connectionSupplier = connectionSupplier;
        this.syntax = syntax;
    }
    
    public Connection getConnection() throws SQLException {
        return connectionSupplier.get();
    }

    Syntax getSyntax() {
        return syntax;
    }

    @Override
    public MiQueryBuilder newQuery() {
        return new JdbcQuery(this);
    }

    @Override
    public MiUpdateBuilder newUpdate() {
        return new JdbcUpdate(this);
    }

    @Override
    public void close() throws MiException {
    }

    @Override
    public <Req extends MiRequest<?>> Req newRequest(RequestType<Req> type) {
        return syntax.newRequest(this, type);
    }

    public PreparedStatement prepareStatement(String sql) throws MiException {
        try {
            Connection c = getConnection();
            return c.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException e) {
            throw new MiException(e.getMessage() + "\n" + sql, e);
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
            throw Closeables.exceptionAs(t, MiException.class);
        }
    }

    MiAction<ResultSet> queryAction(MiSupplier<PreparedStatement> stmt) {
        return MiFutures.build()
                .notResettable()
                .executor(QUERY_EXECUTOR)
                .defaultExecutor(MiFutures.defaultExecutor())
                .action(fExecuteQuery, stmt);
    }
    
    long executeStatement(PreparedStatement stmt) throws MiException {
        try {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new MiException(e);
        }
    }
    
    private long executeUpdate(MiSupplier<PreparedStatement> stmt) throws MiException {
        try {
            return executeStatement(stmt.call());
        } catch (Throwable t) {
            throw Closeables.exceptionAs(t, MiException.class);
        }
    }

    public MiAction<Long> stmtAction(MiSupplier<PreparedStatement> stmt) {
        return MiFutures.build()
                .notResettable()
                .executor(QUERY_EXECUTOR)
                .defaultExecutor(MiFutures.defaultExecutor())
                .action(fExecuteUpdate, stmt);
    }
    
    public static interface ConnectionProvider {
        
        Connection get() throws SQLException;
        
        default ConnectionProvider cached() {
            return cacheConnection(this);
        }
    }
    
    public static ConnectionProvider cacheConnection(ConnectionProvider factory) {
        return new ConnectionProvider() {
            Connection cnn = null;
            @Override
            public Connection get() throws SQLException {
                if (cnn == null || cnn.isClosed()) {
                    cnn = factory.get();
                }
                return cnn;
            }
        };
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
