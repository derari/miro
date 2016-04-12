package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.function.MiFunction;
import org.cthul.miro.futures.MiFuture;

/**
 * Base class for implementations that want to decorate a {@link MiFuture}.
 * @param <V>
 */
public class MiActionDelegator<V> implements MiAction<V> {
    
    private final MiAction<V> delegatee;
    private final Executor defaultExecutor;

    public MiActionDelegator(MiAction<? extends V> delegatee) {
        this(delegatee, null);
    }

    @SuppressWarnings("unchecked")
    public MiActionDelegator(MiAction<? extends V> delegatee, Executor defaultExecutor) {
        this.delegatee = (MiAction) delegatee;
        this.defaultExecutor = defaultExecutor;
    }

    protected MiAction<V> getDelegatee() {
        return delegatee;
    }
    
    protected MiAction<V> getCancelDelegatee() {
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
    public boolean isDone() {
        return getDelegatee().isDone();
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
    public <R> MiAction<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> action) {
        executor = replaceExecutor(executor);
        return getDelegatee().onComplete(executor, action);
    }

    @Override
    public <R> MiAction<R> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> action) {
        executor = replaceExecutor(executor);
        return getDelegatee().onCompleteAlways(executor, action);
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
    public MiFuture<V> getTrigger() {
        return getDelegatee().getTrigger();
    }

    @Override
    public MiFuture<V> submit() {
        return getDelegatee().submit();
    }
}
