package org.cthul.miro;

import java.nio.channels.AsynchronousFileChannel;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import org.cthul.miro.query.adapter.JdbcAdapter;
import org.cthul.miro.query.adapter.JdbcQuery;
import org.cthul.miro.query.sql.AnsiSql;
import org.cthul.miro.util.FutureBase;

/**
 * Decorates a {@link Connection java.sql.Connection} with MiRO features.
 */
public class MiConnection implements AutoCloseable {

    private final JdbcAdapter jdbcAdapter;
    private final Connection connection;
    private final ExecutorService actionExecutor;
    private final ExecutorService queryExecutor;
    private final Set<Thread> execThreads = Collections.synchronizedSet(new HashSet<Thread>());
    private final List<QueryPreProcessor> preProcessors = new ArrayList<>();
    private boolean closed = false;

    public MiConnection(JdbcAdapter jdbcAdapter, Connection connection) {
        this.jdbcAdapter = jdbcAdapter == null ? AnsiSql.getInstance() : jdbcAdapter;
        this.connection = connection;
        openConnections.put(this, true);
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(){
                    @Override
                    public void run() {
                        execThreads.add(this);
                        try {
                            r.run();
                        } finally {
                            execThreads.remove(this);
                        }
                    }
                };
            }
        };
        queryExecutor = Executors.newFixedThreadPool(3, tf);
        actionExecutor = Executors.newFixedThreadPool(3, tf);//newDynamicThreadPool(8, tf);
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public MiConnection(Connection connection) {
        this(null, connection);
    }
    
    private ExecutorService newDynamicThreadPool(int max, ThreadFactory tf) {
        return new ThreadPoolExecutor(1, max, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), tf);
    }
    
    public void addPreProcessor(QueryPreProcessor qpp) {
        preProcessors.add(qpp);
    }
    
    public String preProcess(String sql) {
        for (QueryPreProcessor qpp: preProcessors) {
            sql = qpp.apply(sql);
        }
        return sql;
    }
    
    public JdbcAdapter getJdbcAdapter() {
        return jdbcAdapter;
    }
    
    public ResultSet execute(JdbcQuery<?> query) throws SQLException {
        return query.execute(connection);
    }

    public MiPreparedStatement prepare(String sql) throws SQLException {
        return new MiPreparedStatement(this, sql);
    }

    /* MiPreparedStatement */ 
    PreparedStatement preparedStatement(String sql) throws SQLException {
        return connection.prepareStatement(preProcess(sql));
    }

    /* MiPreparedStatement */
    protected Future<?> submitQuery(Runnable queryCommand) {
        return actionExecutor.submit(queryCommand);
    }
    
    private Future<?> submitAction(Runnable queryCommand) {
        return actionExecutor.submit(queryCommand);
    }

    public <P, R> MiFuture<R> submit(P arg, MiFutureAction<P, R> action) {
        ActionResult<P, R> result = new ActionResult<>(action, arg);
        result.cancelDelegate = submitAction(result);
        return result;
    }

    /**
     * Terminates all running queries and 
     * closes the underlying {@link Connection java.sql.Connection}.
     * @throws SQLException 
     */
    @Override
    public void close() throws SQLException {
        synchronized (this) {
            if (closed) return;
            closed = true;
        }
        try {
            boolean terminated = false;
            try {
                terminated = actionExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (!terminated) {
                actionExecutor.shutdownNow();
            }
        } finally {
            connection.close();
            openConnections.remove(this);
        }
    }
    
    public boolean isClosed() throws SQLException {
        return closed || connection.isClosed();
    }

    private static class ActionResult<P, R>
                        extends FutureBase<R>
                        implements Runnable {

        Future<?> cancelDelegate = null;
        private final MiFutureAction<P, R> action;
        private final P param;

        public ActionResult(MiFutureAction<P, R> action, P param) {
            super(null);
            this.action = action;
            this.param = param;
        }

        @Override
        protected Future<?> getCancelDelegate() {
            return cancelDelegate;
        }

        @Override
        public void run() {
            try {
                setValue(action.call(param));
            } catch (Throwable e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                setException(e);
            }
        }
    }
    
    public static interface QueryPreProcessor {
        String apply(String sql);
    }
    
    // Executor management =====================================================
    
    private static final ConcurrentMap<MiConnection, Boolean> openConnections = new ConcurrentHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (MiConnection cnn : openConnections.keySet()) {
                    try {
                        cnn.close();
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                    }
                }
            }
        });
    }
    
    // DSL =====================================================================
//    
//    private Select selectAll = null;
//
//    public Select select() {
//        if (selectAll == null) {
//            selectAll = new Select(this);
//        }
//        return selectAll;
//    }
//
//    public Select select(String... fields) {
//        return new Select(this, fields);
//    }
}
