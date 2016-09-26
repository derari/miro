package org.cthul.miro.futures.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.cthul.miro.function.MiFunction;
import org.cthul.miro.futures.MiFuture;

/**
 * Base class for {@link MiFuture}s.
 * Implementations must call {@link #start()}, and {@link #result(long, java.lang.Object)} or
 * {@link #fail(long, java.lang.Throwable)}.
 * @param <V> 
 */
public abstract class AbstractMiFuture<V> implements MiFuture<V> {
    
    private final Object lock = new Object();
    private final Executor defaultExecutor;
    private final boolean resettable;
    private Future<?> cancelDelegate;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean cancelSuccess = false;
    private boolean done = false;
    private long attempt = 0;
    private V result = null;
    private Throwable exception = null;
    private OnComplete<?> onCompleteListener = null;
    private List<OnComplete<?>> moreOnCompleteListeners = null;

    public AbstractMiFuture(boolean resettable) {
        this(null, resettable);
    }

    public AbstractMiFuture(Executor defaultExecutor, boolean resettable) {
        this.defaultExecutor = defaultExecutor;
        this.resettable = resettable;
    }
    
    protected final Object lock() {
        return lock;
    }

    /**
     * Returns {@code true} if the task has started.
     * @return true iff the task has started.
     */
    protected boolean isStarted() {
        // Don't sync. Every operation relying on the future not being 
        // started will have to sync anyway.
        return started;
    }
    
    /**
     * Marks the operation as started.
     * @return attempt id
     */
    protected long start() {
        return start(null);
    }
    
    /**
     * Marks the operation as started.
     * @param cancelDelegate 
     * @return attempt id
     */
    protected long start(Future<?> cancelDelegate) {
        synchronized (lock) {
            if (started) {
                throw new IllegalStateException("Was already started.");
            }
            this.started = true;
            this.cancelDelegate = cancelDelegate;
            return attempt;
        }
    }
    
    protected void replaceCancelDelegate(Future<?> cancelDelegate) {
        synchronized (lock) {
            if (this.cancelDelegate == cancelDelegate) return;
            if (this.cancelDelegate != null) {
                this.cancelDelegate.cancel(true);
            }
            this.cancelDelegate = cancelDelegate;
        }
    }
    
    /**
     * Indicates if the underlying action should continue.
     * If the current thread's interrupt flag is set, the future will be
     * cancelled automatically.
     * @param attempt
     * @return true iff operation should continue
     */
    protected boolean progress(long attempt) {
        if (this.attempt != attempt) {
            return false;
        }
        if (cancelled) {
            cancelSuccess = true;
            return false;
        }
        if (Thread.interrupted()) {
            cancel(false);
            return false;
        }
        return true;
    }
    
    /**
     * Check before the task can complete.
     * @return true iff task can complete normally
     */
    @SuppressWarnings("NotifyNotInSynchronizedContext")
    private boolean tryComplete() {
        if (cancelled) {
            cancelSuccess = true;
            return false;
        }
        if (done) {
            throw new IllegalStateException("Is already done.");
        }
        return true;
    }
    
    /**
     * Sets the result of the operation.
     * @param attempt
     * @param result 
     */
    protected void result(long attempt, V result) {
        List<OnComplete<?>> listeners;
        synchronized (lock) {
            if (this.attempt != attempt) {
                return;
            }
            if (!tryComplete()) {
                return;
            }
            this.result = result;
            this.done = true;
            lock.notifyAll();
            listeners = getListenersToSubmit();
        }
        submitOnCompleteListeners(listeners);
    }
    
    /**
     * Marks the operation as failed and sets the exception.
     * @param attempt
     * @param exception 
     */
    protected void fail(long attempt, Throwable exception) {
        List<OnComplete<?>> listeners;
        synchronized (lock) {
            if (this.attempt != attempt) {
                return;
            }
            if (!tryComplete()) {
                return;
            }
            this.exception = exception;
            done = true;
            lock.notifyAll();
            listeners = getListenersToSubmit();
        }
        submitOnCompleteListeners(listeners);
    }
    
    protected Future<?> getCancelDelegate() {
        return cancelDelegate;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        List<OnComplete<?>> listeners;
        boolean success;
        if (done) return false;
        synchronized (lock) {
            if (done) return false;
            exception = new CancellationException();
            cancelSuccess = !started;
            cancelled = true;
            Future<?> delegate = getCancelDelegate();
            if (delegate != null && delegate.cancel(mayInterruptIfRunning)) {
                cancelSuccess = true;
            }
            listeners = getListenersToSubmit();
            done = true;
            lock.notifyAll();
            success = cancelSuccess;
        }
        submitOnCompleteListeners(listeners);
        return success;
    }

    @Override
    public boolean deepCancel(boolean mayInterruptIfRunning) {
        return cancel(mayInterruptIfRunning);
    }

    protected boolean isResettable() {
        return resettable;
    }
    
    protected boolean resetState(long timeout, TimeUnit timeUnit) {
        if (!resettable) {
            throw new UnsupportedOperationException();
        }
        if (!started) return true;
        synchronized (lock) {
            if (!started) return true;
            boolean success = done || cancel(true);
            attempt++;
            done = false;
            cancelled = cancelSuccess = false;
            cancelDelegate = null;
            result = null;
            exception = null;
            if (onCompleteListener != null) {
                onCompleteListener.reset();
            }
            if (moreOnCompleteListeners != null) {
                moreOnCompleteListeners.forEach(OnComplete::reset);
            }
            if (!success && timeout >= 0) {
                success = beDone(timeout, timeUnit);
            }
            started = false;
            return success;
        }
    }
    
    protected boolean slowReset() {
        return resetState(10, TimeUnit.MILLISECONDS);
    }
    
    protected boolean fastReset() {
        return resetState(-1, null);
    }
    
    protected void tryRunNow(long ms) {
    }

    @Override
    public void await() throws InterruptedException {
        if (done) return;
        synchronized (lock) {
            if (!done) tryRunNow(-1);
            while (!done) {
                lock.wait();
            }
        }
    }

    @Override
    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (timeout < 0) {
            await();
            return;
        }
        if (done) return;
        synchronized (lock) {
            long ms = unit.toMillis(timeout);
            if (!done) {
                long s = System.currentTimeMillis();
                tryRunNow(ms);
                ms = ms + s - System.currentTimeMillis();
            }
            if (done) return;
            lock.wait(ms);
            if (!done) {
                throw new TimeoutException(
                    "Not done after " + timeout + " " + unit.toString().toLowerCase() + ".");
            }
        }
    }
    
    protected <T> T assertIsDone(Supplier<T> result) {
        if (resettable) {
            synchronized (lock) {
                assertIsDone();
                return result.get();
            }
        } else {
            assertIsDone();
            return result.get();
        }
    }
    
    @Override
    public boolean hasResult() {
        return assertIsDone(() -> exception == null);
    }

    @Override
    public V getResult() {
        return assertIsDone(() -> result);
    }

    @Override
    public Throwable getException() {
        return assertIsDone(() -> exception);
    }

    @Override
    public boolean isCancelled() {
        // don't sync. Every operation relying on the future not being 
        // cancelled will have to sync anyway.
        // Future does not qualify as cancelled if it is not done,
        // even if cancelling is known to be successful
        return done && cancelSuccess;
    }

    @Override
    public boolean isDone() {
        // don't sync. Every operation relying on the future not being 
        // done will have to sync anyway.        
        return done;
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
        return onComplete(true, executor, action);
    }

    @Override
    public <R> MiFuture<R> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> action) {
        return onComplete(false, executor, action);
    }
    
    protected <R> MiFuture<R> onComplete(boolean canCancel, Executor executor, MiFunction<? super MiFuture<V>, ? extends R> action) {
        executor = replaceExecutor(executor);
        OnComplete<R> onComplete = new OnComplete<>(canCancel, executor, action);
        if (!enqueue(onComplete)) {
            onComplete.submit();
        }
        return onComplete;
    }

    private boolean enqueue(OnComplete<?> onComplete) {
        if (done && !resettable) return false;
        synchronized (lock) {
            if (done && !resettable) return false;
            if (onCompleteListener == null) {
                onCompleteListener = onComplete;
            } else {
                if (moreOnCompleteListeners == null) {
                    moreOnCompleteListeners = new ArrayList<>();
                }
                moreOnCompleteListeners.add(onComplete);
            }
            return !done;
        }
    }

    private synchronized List<OnComplete<?>> getListenersToSubmit() {
        OnComplete<?> first = onCompleteListener;
        List<OnComplete<?>> more = moreOnCompleteListeners;
        if (!resettable) {
            onCompleteListener = null;
            moreOnCompleteListeners = null;
        }
        if (first != null || resettable) {
            List<OnComplete<?>> listeners = new ArrayList<>(1);
            if (first != null) listeners.add(first);
            if (more != null) listeners.addAll(more);
            return listeners;
        } else if (more != null) {
            return more;
        } else {
            return Collections.emptyList();
        }
    }

    private void submitOnCompleteListeners(List<OnComplete<?>> listeners) {
        listeners.forEach(oc -> oc.submit());
    }

    protected class OnComplete<R> extends AbstractMiFutureFunction<MiFuture<V>, R> {
        
        private final boolean canCancel;

        public OnComplete(boolean canCancel, Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
            super(function, executor, AbstractMiFuture.this.resettable);
            this.canCancel = canCancel;
        }

        public void submit() {
            if (canCancel && AbstractMiFuture.this.cancelled) {
                cancel(false);
            } else {
                submit(AbstractMiFuture.this);
            }
        }

        protected void run() {
            if (canCancel && AbstractMiFuture.this.cancelled) {
                cancel(false);
            } else {
                run(AbstractMiFuture.this);
            }
        }

        @Override
        protected void tryRunNow(long ms) {
            if (AbstractMiFuture.this.beDone(ms, TimeUnit.MILLISECONDS)) {
                run();
            }
        }

        @Override
        protected boolean progress(long attempt) {
            return true;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!canCancel) return false;
            return super.cancel(mayInterruptIfRunning);
        }
        
        @Override
        public boolean deepCancel(boolean mayInterruptIfRunning) {
            AbstractMiFuture.this.deepCancel(mayInterruptIfRunning);
            return super.deepCancel(mayInterruptIfRunning);
        }

        protected void reset() {
            fastReset();
        }
    }
}
