package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cthul.miro.function.MiFunction;
import org.cthul.miro.futures.MiFuture;

/**
 * Base class for implementations that want to decorate a {@link MiFuture}.
 * @param <V>
 */
public class MiFutureDelegator<V> implements MiFuture<V> {
    
    private final MiFuture<V> delegate;
    private final Executor defaultExecutor;

    public MiFutureDelegator(MiFuture<? extends V> delegate) {
        this(delegate, null);
    }

    @SuppressWarnings("unchecked")
    public MiFutureDelegator(MiFuture<? extends V> delegate, Executor defaultExecutor) {
        this.delegate = (MiFuture) delegate;
        this.defaultExecutor = defaultExecutor;
    }

    protected MiFuture<V> getDelegate() {
        return delegate;
    }
    
    protected MiFuture<V> getCancelDelegate() {
        return getDelegate();
    }
    
    protected Executor replaceExecutor(Executor executor) {
        if (executor != null) return executor;
        return defaultExecutor;
    }

    @Override
    public void await() throws InterruptedException {
        getDelegate().await();
    }

    @Override
    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        getDelegate().await(timeout, unit);
    }
    
    @Override
    public boolean isDone() {
        return getDelegate().isDone();
    }

    @Override
    public boolean hasResult() {
        return getDelegate().hasResult();
    }

    @Override
    public V getResult() {
        return getDelegate().getResult();
    }

    @Override
    public Throwable getException() {
        return getDelegate().getException();
    }

    @Override
    public <R> MiFuture<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> action) {
        executor = replaceExecutor(executor);
        return getDelegate().onComplete(executor, action);
    }

    @Override
    public <R> MiFuture<R> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> action) {
        executor = replaceExecutor(executor);
        return getDelegate().onCompleteAlways(executor, action);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return getCancelDelegate().cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return getCancelDelegate().isCancelled();
    }

    @Override
    public boolean deepCancel(boolean mayInterruptIfRunning) {
        return getCancelDelegate().deepCancel(mayInterruptIfRunning);
    }
}
