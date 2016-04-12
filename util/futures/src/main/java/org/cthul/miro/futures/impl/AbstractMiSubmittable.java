package org.cthul.miro.futures.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cthul.miro.futures.MiFutures;

public abstract class AbstractMiSubmittable<R> extends AbstractMiFuture<R> {

    private final Executor executor;

    public AbstractMiSubmittable(boolean resettable) {
        this(null, resettable);
    }
    
    public AbstractMiSubmittable(Executor executor, boolean resettable) {
        this(executor, executor, resettable);
    }

    public AbstractMiSubmittable(Executor executor, Executor defaultExecutor, boolean resettable) {
        super(defaultExecutor, resettable);
        this.executor = executor;
    }

    protected Executor getExecutor() {
        return executor != null ? executor : MiFutures.defaultExecutor();
    }
    
    /**
     * If the action has not been submitted yet,
     * it will run in the current thread.
     * If it was already submitted, calling this has no effect.
     * @param runner
     * @return this action
     */
    protected AbstractMiSubmittable<R> run(Runner runner) {
        if (isStarted() || isCancelled()) return this;
        synchronized (lock()) {
            if (isStarted() || isCancelled()) return this;
            Future<?> cancelDelegatee = runner;
            runner.attempt = start(cancelDelegatee);
        }
        runner.run();
        return this;
    }
    
    /**
     * If the action has not been submitted yet, 
     * the runner is submitted to the executor.
     * If it was already submitted, calling this has no effect.
     * @param runner
     * @return this action
     */
    protected AbstractMiSubmittable<R> submit(Runner runner) {
        if (isStarted() || isCancelled()) return this;
        Executor exec = getExecutor();
        synchronized (lock()) {
            if (isStarted() || isCancelled()) return this;
            Future<?> cancelDelegatee;
            if (exec instanceof ExecutorService) {
                ExecutorService es = (ExecutorService) exec;
                cancelDelegatee = es.submit(runner);
            } else {
                exec.execute(runner);
                cancelDelegatee = runner;
            }
            runner.attempt = start(cancelDelegatee);
        }
        return this;
    }
    
    protected abstract class Runner implements Runnable, Future<R> {
        private Thread thread = null;
        private long attempt;

        @Override
        public void run() {
            // don't start before #submit is done
            synchronized (lock()) {
                thread = Thread.currentThread();
                if (!progress()) return;
            }
            try {
                call(attempt);
            } catch (Throwable t) {
                fail(t);
            } finally {
                thread = null;
            }
        }
        
        protected void result(R value) {
            AbstractMiSubmittable.this.result(attempt, value);
        }
        
        protected void fail(Throwable t) {
            AbstractMiSubmittable.this.fail(attempt, t);
        }
        
        protected boolean progress() {
            return AbstractMiSubmittable.this.progress(attempt);
        }
        
        protected abstract void call(long attempt) throws Throwable;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            synchronized (lock()) {
                if (thread == null) return true;
                if (mayInterruptIfRunning) {
                    thread.interrupt();
                    return true;
                }
                return false;
            }
        }

        @Override
        public boolean isCancelled() {
            return AbstractMiSubmittable.this.isCancelled();
        }

        @Override
        public boolean isDone() {
            return AbstractMiSubmittable.this.isDone();
        }

        @Override
        public R get() throws InterruptedException, ExecutionException {
            return AbstractMiSubmittable.this.get();
        }

        @Override
        public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return AbstractMiSubmittable.this.get(timeout, unit);
        }
    }
}
