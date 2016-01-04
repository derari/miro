package org.cthul.miro.futures.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cthul.miro.futures.MiFunction;
import org.cthul.miro.futures.MiFutures;

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
            Future<?> cancelDelegate = null;
            if (exec instanceof ExecutorService) {
                ExecutorService es = (ExecutorService) exec;
                cancelDelegate = es.submit(r);
            } else {
                exec.execute(r);
                cancelDelegate = r;
            }
            start(cancelDelegate);
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
            synchronized (lock()) {
                thread = Thread.currentThread();
                if (!beginWork()) return;
            }
            try {
                R r = function.call(arg);
                result(r);
            } catch (Throwable t) {
                fail(t);
            } finally {
                thread = null;
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            synchronized (lock()) {
                if (thread == null) return true;
                if (mayInterruptIfRunning) {
                    thread.interrupt();
                }
                return false;
            }
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
