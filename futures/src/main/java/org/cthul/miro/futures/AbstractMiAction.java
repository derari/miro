package org.cthul.miro.futures;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AbstractMiAction<T, R> extends SimpleMiFuture<R> {

    private final Executor executor;
    private final MiFunction<? super T, ? extends R> function;

    public AbstractMiAction(Executor executor, MiFunction<? super T, ? extends R> function) {
        this(executor, function, executor);
    }

    public AbstractMiAction(Executor executor, MiFunction<? super T, ? extends R> function, Executor defaultExecutor) {
        super(defaultExecutor);
        this.executor = executor;
        this.function = function;
    }

    protected Executor getExecutor() {
        return executor != null ? executor : MiFutures.runNowExecutor();
    }
    
    /**
     * If the action has not been submitted yet, 
     * it is submitted to the executor.
     * If it was already submitted, calling this has no effect.
     * @param arg
     * @return this action
     */
    protected AbstractMiAction<T, R> submit(T arg) {
        if (isStarted() || isCancelled()) return this;
        Executor exec = getExecutor();
        synchronized (lock()) {
            if (isStarted() || isCancelled()) return this;
            Future<?> f = null;
            if (exec instanceof ExecutorService) {
                ExecutorService es = (ExecutorService) exec;
                f = es.submit(new Runner(arg));
            } else {
                exec.execute(new Runner(arg));
            }
            start(f);
        }
        return this;
    }
    
    protected class Runner implements Runnable {
        private final T arg;
        public Runner(T arg) {
            this.arg = arg;
        }
        @Override
        public void run() {
            // don't start before #submit is done
            synchronized (lock()) {
                if (!enter()) return;
            }
            try {
                R r = function.call(arg);
                result(r);
            } catch (Throwable t) {
                fail(t);
            }
        }
    }
}
