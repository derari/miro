package org.cthul.miro.futures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Base class for {@link MiFuture}s.
 * Implementations must call {@link #start()}, and {@link #result(java.lang.Object)} or
 * {@link #failed(java.lang.Throwable)}.
 * @param <V> 
 */
public class SimpleMiFuture<V> implements MiFuture<V> {
    
    private final Object lock = new Object();
    private final Executor defaultExecutor;
    private Future<?> cancelDelegatee;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean cancelSuccess = false;
    private boolean done = false;
    private V result = null;
    private Throwable exception = null;
    private OnComplete<?> onCompleteListener = null;
    private List<OnComplete<?>> moreOnCompleteListeners = null;

    public SimpleMiFuture() {
        this(null);
    }

    public SimpleMiFuture(Executor defaultExecutor) {
        this.defaultExecutor = defaultExecutor;
    }
    
    protected final Object lock() {
        return lock;
    }

    protected boolean isStarted() {
        // Don't sync. Every operation relying on the future not being 
        // started will have to sync anyway.
        return started;
    }
    
    protected void start() {
        start(null);
    }
    
    protected void start(Future<?> cancelDelegatee) {
        synchronized (lock) {
            if (started) {
                throw new IllegalArgumentException(
                        "Was already started.");
            }
            this.started = true;
            this.cancelDelegatee = cancelDelegatee;
        }
    }
    
    protected boolean enter() {
        if (isCancelled()) {
            result(null);
            return false;
        }
        if (Thread.interrupted()) {
            fail(new InterruptedException());
            return false;
        }
        return true;
    }
    
    protected void result(V result) {
        synchronized (lock) {
            if (cancelled) {
                cancelSuccess = true;
            } else {
                if (done) {
                    throw new IllegalArgumentException(
                            "Is already done.");
                }
                done = true;
                this.result = result;
                lock.notifyAll();
            }
        }
        triggerOnCompleteListeners();
    }
    
    protected void fail(Throwable exception) {
        synchronized (lock) {
            if (cancelled) {
                cancelSuccess = true;
            } else { 
                if (done) {
                    throw new IllegalArgumentException(
                            "Is already done.");
                }
                done = true;
                this.exception = exception;
                lock.notifyAll();
            }
        }
        triggerOnCompleteListeners();
    }
    
    protected Future<?> getCancelDelegate() {
        return cancelDelegatee;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (done) return false;
        synchronized (lock) {
            if (done) return false;
            done = cancelled = true;
            lock.notifyAll();
            if (!started) {
                cancelSuccess = true;
            }
            Future<?> delegate = getCancelDelegate();
            if (delegate != null && delegate.cancel(mayInterruptIfRunning)) {
                cancelSuccess = true;
            }
        }
        triggerOnCompleteListeners();
        return cancelSuccess;
    }

    @Override
    public boolean deepCancel(boolean mayInterruptIfRunning) {
        return cancel(mayInterruptIfRunning);
    }

    @Override
    public void await() throws InterruptedException {
        if (done) return;
        synchronized (lock) {
            if (done) return;
            lock.wait();
        }
    }

    @Override
    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (done) return;
        synchronized (lock) {
            if (done) return;
            lock.wait(unit.toMillis(timeout));
            if (!done) {
                throw new TimeoutException(
                    "Not done after " + timeout + " " + unit.toString().toLowerCase() + ".");
            }
        }
    }
    
    @Override
    public boolean hasResult() {
        assertIsDone();
        return exception == null && !cancelled;
    }

    @Override
    public V getResult() {
        assertIsDone();
        return result;
    }

    @Override
    public Throwable getException() {
        assertIsDone();
        if (cancelled) return new CancellationException();
        return exception;
    }

    @Override
    public boolean isCancelled() {
        // don't sync. Every operation relying on the future not being 
        // cancelled will have to sync anyway.
        return cancelSuccess;
    }

    @Override
    public boolean isDone() {
        if (done) return true;
        synchronized (lock) {
            return done;
        }
    }

    protected Executor getDefaultExecutor() {
        return defaultExecutor;
    }
    
    protected Executor replaceExecutor(Executor executor) {
        if (executor != null) return executor;
        return getDefaultExecutor();
    }

    @Override
    public <R> MiFuture<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> action) {
        executor = replaceExecutor(executor);
        OnComplete<R> onComplete = new OnComplete<>(executor, action);
        if (!enqueue(onComplete)) {
            onComplete.submit();
        }
        return onComplete;
    }

    private boolean enqueue(OnComplete<?> onComplete) {
        if (done) return false;
        synchronized (lock) {
            if (done) return false;
            if (onCompleteListener == null) {
                onCompleteListener = onComplete;
            } else {
                if (moreOnCompleteListeners == null) {
                    moreOnCompleteListeners = new ArrayList<>();
                }
                moreOnCompleteListeners.add(onComplete);
            }
        }
        return true;
    }

    private void triggerOnCompleteListeners() {
        assertIsDone();
        // It's okay if this is called multiple times.
        // #submit is thread-safe
        OnComplete<?> first = onCompleteListener;
        if (first != null) {
            onCompleteListener = null;
            first.submit();
        }
        List<OnComplete<?>> more = moreOnCompleteListeners;
        if (more != null) {
            moreOnCompleteListeners = null;
            more.stream().forEach((oc) -> oc.submit());
        }
    }

    protected class OnComplete<R> extends AbstractMiAction<MiFuture<V>, R> {

        public OnComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
            super(executor, function);
        }
        
        public void submit() {
            if (SimpleMiFuture.this.cancelled) {
                cancel(false);
            } else {
                submit(SimpleMiFuture.this);
            }
        }

        @Override
        public boolean deepCancel(boolean mayInterruptIfRunning) {
            SimpleMiFuture.this.deepCancel(mayInterruptIfRunning);
            return super.deepCancel(mayInterruptIfRunning);
        }
    }
}
