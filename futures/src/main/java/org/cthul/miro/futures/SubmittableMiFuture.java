package org.cthul.miro.futures;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class SubmittableMiFuture<T, R> extends AbstractMiFuture<R> {

    private final Executor executor;
    private final MiFunction<? super T, ? extends R> function;

    public SubmittableMiFuture(Executor executor, MiFunction<? super T, ? extends R> function) {
        this(executor, function, executor);
    }

    public SubmittableMiFuture(Executor executor, MiFunction<? super T, ? extends R> function, Executor defaultExecutor) {
        super(defaultExecutor);
        this.executor = executor;
        this.function = function;
    }

    protected Executor getExecutor() {
        return executor != null ? executor : MiFutures.defaultExecutor();
    }
    
    /**
     * If the action has not been submitted yet, 
     * it is submitted to the executor.
     * If it was already submitted, calling this has no effect.
     * @param arg
     * @return this action
     */
    protected SubmittableMiFuture<T, R> submit(T arg) {
        if (isStarted() || isCancelled()) return this;
        Executor exec = getExecutor();
        synchronized (lock()) {
            if (isStarted() || isCancelled()) return this;
            Runner r = new Runner(arg);
            Future<?> f = null;
            if (exec instanceof ExecutorService) {
                ExecutorService es = (ExecutorService) exec;
                f = es.submit(r);
            } else {
                exec.execute(r);
                f = r;
            }
            start(f);
        }
        return this;
    }
    
    protected class Runner implements Runnable, Future<R> {
        private final T arg;
        private Thread thread = null;
        public Runner(T arg) {
            this.arg = arg;
        }
        @Override
        public void run() {
            // don't start before #submit is done
            Thread thread = Thread.currentThread();
            synchronized (lock()) {
                if (!continueWork()) return;
            }
            try {
                R r = function.call(arg);
                result(r);
            } catch (Throwable t) {
                fail(t);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCancelled() {
            return SubmittableMiFuture.this.isCancelled();
        }

        @Override
        public boolean isDone() {
            return SubmittableMiFuture.this.isDone();
        }

        @Override
        public R get() throws InterruptedException, ExecutionException {
            return SubmittableMiFuture.this.get();
        }

        @Override
        public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return SubmittableMiFuture.this.get(timeout, unit);
        }
    }
}
