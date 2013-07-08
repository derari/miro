package org.cthul.miro.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;

/**
 * Base class for {@link MiFuture}s.
 * Implementations must call {@link #setValue(java.lang.Object)} or
 * {@link #setException(java.lang.Throwable)}.
 * @param <V> 
 */
public abstract class FutureBase<V> implements MiFuture<V> {

    private final Object lock = new Object();
    private final Future<?> cancelDelegate;
    private boolean done = false;
    private V value = null;
    private Throwable exception = null;
    private OnComplete<? super MiFuture<V>, ?> onCompleteListener = null;
    private List<OnComplete<? super MiFuture<V>, ?>> onCompleteListeners = null;

    public FutureBase(Future<?> cancelDelegate) {
        this.cancelDelegate = cancelDelegate;
    }

    protected Future<?> getCancelDelegate() {
        return cancelDelegate;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        Future<?> delegate = getCancelDelegate();
        if (delegate == null) {
            return false;
        }
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        Future<?> delegate = getCancelDelegate();
        if (delegate == null) {
            return false;
        }
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        if (done) {
            return true;
        }
        synchronized (lock) {
            return done;
        }
    }

    protected void setValue(V value) {
        synchronized (lock) {
            if (done) {
                throw new IllegalStateException("Already done");
            }
            this.value = value;
            done = true;
            lock.notifyAll();
        }
        triggerAllListeners();
    }

    protected void setException(Throwable exception) {
        synchronized (lock) {
            if (done) {
                throw new IllegalStateException("Already done");
            }
            this.exception = exception;
            done = true;
            lock.notifyAll();
        }
        triggerAllListeners();
    }

    private void triggerAllListeners() {
        assert isDone();
        if (onCompleteListener != null) {
            trigger(onCompleteListener);
        }
        if (onCompleteListeners != null) {
            for (OnComplete<? super MiFuture<V>, ?> l : onCompleteListeners) {
                trigger(l);
            }
        }
    }

    protected void trigger(OnComplete<? super MiFuture<V>, ?> listener) {
        listener.call(this);
    }

    @Override
    public void waitUntilDone() throws InterruptedException {
        if (done) {
            return;
        }
        synchronized (lock) {
            if (!done) {
                lock.wait();
            }
        }
    }

    @Override
    public void waitUntilDone(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (done) {
            return;
        }
        synchronized (lock) {
            if (!done) {
                lock.wait(unit.toMillis(timeout));
                if (!done) {
                    throw new TimeoutException();
                }
            }
        }
    }

    @Override
    public boolean beDone() {
        try {
            waitUntilDone();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean beDone(long timeout, TimeUnit unit) {
        try {
            waitUntilDone(timeout, unit);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public V _get() {
        try {
            return get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        waitUntilDone();
        return getAfterDone();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        waitUntilDone(timeout, unit);
        return getAfterDone();
    }

    private V getAfterDone() throws ExecutionException {
        assert done;
        if (exception != null) {
            throw new ExecutionException(exception);
        }
        return value;
    }

    protected void ensureDone() {
        if (!isDone()) {
            throw new IllegalStateException("Execution not yet complete");
        }
    }

    @Override
    public boolean hasResult() {
        ensureDone();
        return exception == null;
    }

    @Override
    public boolean hasFailed() {
        ensureDone();
        return exception != null;
    }

    @Override
    public V getResult() {
        ensureDone();
        return value;
    }

    @Override
    public Throwable getException() {
        ensureDone();
        return exception;
    }

    @Override
    public <R> MiFuture<R> onComplete(MiFutureAction<? super MiFuture<V>, R> action) {
        OnComplete<? super MiFuture<V>, R> listener = new OnComplete<>(cancelDelegate, action);
        if (done) {
            trigger(listener);
            return listener;
        }
        synchronized (lock) {
            if (done) {
                trigger(listener);
                return listener;
            }
            if (onCompleteListener == null) {
                onCompleteListener = listener;
            } else {
                if (onCompleteListeners == null) {
                    onCompleteListeners = new ArrayList<>();
                }
                onCompleteListeners.add(listener);
            }
        }
        return listener;
    }

    @Override
    public <R> MiFuture<R> onComplete(
            final MiFutureAction<? super V, R> onSuccess,
            final MiFutureAction<? super Throwable, R> onFailure) {
        return onComplete(new MiFutureAction<MiFuture<V>, R>() {
            @Override
            public R call(MiFuture<V> f) throws Exception {
                if (f.hasResult()) {
                    return onSuccess.call(f.getResult());
                } else {
                    return onFailure.call(f.getException());
                }
            }
        });
    }

    @Override
    public <R> MiFuture<R> onSuccess(final MiFutureAction<? super V, R> action) {
        return onComplete(new MiFutureAction<MiFuture<V>, R>() {
            @Override
            public R call(MiFuture<V> f) throws Exception {
                return action.call(f.get());
            }
        });
    }

    @Override
    public <R> MiFuture<R> onFailure(final MiFutureAction<? super Throwable, R> action) {
        return onComplete(new MiFutureAction<MiFuture<V>, R>() {
            @Override
            public R call(MiFuture<V> f) throws Exception {
                if (f.hasResult()) {
                    return null;
                } else {
                    return action.call(f.getException());
                }
            }
        });
    }

    protected static class OnComplete<Param, Result> extends FutureBase<Result> {

        private final MiFutureAction<Param, Result> action;

        public OnComplete(Future<?> cancelDelegate, MiFutureAction<Param, Result> action) {
            super(cancelDelegate);
            this.action = action;
        }

        protected void call(Param param) {
            final Result result;
            try {
                result = action.call(param);
            } catch (Throwable t) {
                setException(t);
                if (t instanceof Error) {
                    throw (Error) t;
                }
                if (t instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                return;
            }
            setValue(result);
        }
    }
}
