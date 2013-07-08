package org.cthul.miro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import org.cthul.miro.dsl.Select;
import org.cthul.miro.util.FutureBase;

/**
 * Decorates a {@link Connection java.sql.Connection} with MiRO features.
 */
public class MiConnection implements AutoCloseable {

    private final Connection connection;
    private final Set<Thread> execThreads = Collections.synchronizedSet(new HashSet<Thread>());
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
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
    });
    private final List<QueryPreProcessor> preProcessors = new ArrayList<>();
    private boolean closed = false;

    public MiConnection(Connection connection) {
        this.connection = connection;
        runningExecs.put(executor, true);
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

    public MiPreparedStatement prepare(String sql) throws SQLException {
        return new MiPreparedStatement(this, sql);
    }

    /* MiPreparedStatement */ 
    PreparedStatement preparedStatement(String sql) throws SQLException {
        return connection.prepareStatement(preProcess(sql));
    }

    /* MiPreparedStatement */
    protected Future<?> submit(Runnable queryCommand) {
        return executor.submit(queryCommand);
    }

    public <P, R> MiFuture<R> submit(MiFutureAction<P, R> action, P arg) {
        ActionResult<P, R> result = new ActionResult<>(action, arg);
        result.cancelDelegate = submit(result);
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
                terminated = executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (!terminated) {
                executor.shutdownNow();
            }
        } finally {
            connection.close();
            runningExecs.remove(executor);
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
    
    private static final ConcurrentMap<ExecutorService, Boolean> runningExecs = new ConcurrentHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (ExecutorService e : runningExecs.keySet()) {
                    try {
                        e.shutdownNow();
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                    }
                }
            }
        });
    }
    
    // DSL =====================================================================
    
    private Select selectAll = null;

    public Select select() {
        if (selectAll == null) {
            selectAll = new Select(this);
        }
        return selectAll;
    }

    public Select select(String... fields) {
        return new Select(this, fields);
    }
}
