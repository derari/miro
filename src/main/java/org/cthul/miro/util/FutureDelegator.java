package org.cthul.miro.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;

/**
 * Base class for implementations that want to decorate a {@link MiFuture}.
 * @author Arian Treffer
 * @param <V> 
 */
public class FutureDelegator<V> implements MiFuture<V> {

    private final MiFuture<V> delegatee;

    public FutureDelegator(MiFuture<V> delegatee) {
        this.delegatee = delegatee;
    }

    protected MiFuture<V> getDelegatee() {
        return delegatee;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return getDelegatee().cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return getDelegatee().isCancelled();
    }

    @Override
    public boolean isDone() {
        return getDelegatee().isDone();
    }

    @Override
    public V _get() {
        return getDelegatee()._get();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return getDelegatee().get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return getDelegatee().get(timeout, unit);
    }

    @Override
    public boolean beDone() {
        return getDelegatee().beDone();
    }

    @Override
    public boolean beDone(long timeout, TimeUnit unit) {
        return getDelegatee().beDone(timeout, unit);
    }

    @Override
    public void waitUntilDone() throws InterruptedException {
        getDelegatee().waitUntilDone();
    }

    @Override
    public void waitUntilDone(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        getDelegatee().waitUntilDone(timeout, unit);
    }

    @Override
    public boolean hasResult() {
        return getDelegatee().hasResult();
    }

    @Override
    public boolean hasFailed() {
        return getDelegatee().hasFailed();
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
    public <R> MiFuture<R> onComplete(MiFutureAction<? super MiFuture<V>, R> action) {
        return getDelegatee().onComplete(action);
    }

    @Override
    public <R> MiFuture<R> onComplete(MiFutureAction<? super V, R> onSuccess, MiFutureAction<? super Throwable, R> onFailure) {
        return getDelegatee().onComplete(onSuccess, onFailure);
    }

    @Override
    public <R> MiFuture<R> onSuccess(MiFutureAction<? super V, R> action) {
        return getDelegatee().onSuccess(action);
    }

    @Override
    public <R> MiFuture<R> onFailure(MiFutureAction<? super Throwable, R> action) {
        return getDelegatee().onFailure(action);
    }
}
