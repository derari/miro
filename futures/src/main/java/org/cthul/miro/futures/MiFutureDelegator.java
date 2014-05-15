package org.cthul.miro.futures;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Base class for implementations that want to decorate a {@link MiFuture}.
 * @param <V>
 */
public class MiFutureDelegator<V> implements MiFuture<V> {
    
    private final MiFuture<V> delegatee;
    private final Executor defaultExecutor;

    public MiFutureDelegator(MiFuture<V> delegatee) {
        this(delegatee, null);
    }

    public MiFutureDelegator(MiFuture<V> delegatee, Executor defaultExecutor) {
        this.delegatee = delegatee;
        this.defaultExecutor = defaultExecutor;
    }

    protected MiFuture<V> getDelegatee() {
        return delegatee;
    }
    
    protected MiFuture<V> getCancelDelegatee() {
        return getDelegatee();
    }
    
    protected Executor replaceExecutor(Executor executor) {
        if (executor != null) return executor;
        return defaultExecutor;
    }

    @Override
    public void await() throws InterruptedException {
        getDelegatee().await();
    }

    @Override
    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        getDelegatee().await(timeout, unit);
    }

    @Override
    public boolean hasResult() {
        return getDelegatee().hasResult();
    }

    @Override
    public V getResult() {
        return getDelegatee().getResult();
    }

    @Override
    public Throwable getException() {
        return getDelegatee().getException();
    }

    @Override
    public <R> MiFuture<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> action) {
        executor = replaceExecutor(executor);
        return getDelegatee().onComplete(executor, action);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return getCancelDelegatee().cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return getCancelDelegatee().isCancelled();
    }

    @Override
    public boolean deepCancel(boolean mayInterruptIfRunning) {
        return getCancelDelegatee().deepCancel(mayInterruptIfRunning);
    }
    
    @Override
    public boolean isDone() {
        return getDelegatee().isDone();
    }
}
