package org.cthul.miro.futures;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface MiFuture<V> extends Future<V> {

    /**
     * Waits until execution is complete or the thread is interrupted.
     * @return true iff execution is complete.
     */
    default boolean beDone() {
        try {
            await();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Waits until execution is complete, the timeout expires, 
     * or the thread is interrupted.
     * @param timeout
     * @param unit
     * @return true iff execution is complete.
     */
    default boolean beDone(long timeout, TimeUnit unit) {
        try {
            await(timeout, unit);
            return true;
        } catch (TimeoutException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Waits until execution is complete or the thread is interrupted.
     * @throws InterruptedException if thread was interrupted
     */
    void await() throws InterruptedException;
    
    /**
     * Waits until execution is complete, the timeout expires, 
     * or the thread is interrupted.
     * @param timeout
     * @param unit
     * @throws InterruptedException if thread was interrupted
     * @throws TimeoutException if timeout expired
     */
    void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
    
    default void assertIsDone() {
        if (isDone()) return;
        throw new IllegalStateException("Not done yet.");
    }
    
    boolean deepCancel(boolean mayInterruptIfRunning);
    
    /**
     * @return true iff the execution succeeded
     * @throws IllegalStateException if execution is not complete
     */
    boolean hasResult();

    /**
     * @return true iff the execution failed
     * @throws IllegalStateException if execution is not complete
     */
    default boolean hasFailed() {
        return !hasResult();
    }

    /**
     * Returns the result of {@link #get()}, 
     * or {@code null}, if the execution failed.
     * @return result
     * @throws IllegalStateException if execution is not complete
     */
    V getResult();

    /**
     * Returns the Throwable thrown by the execution, 
     * or {@code null}, if the execution was successful.
     * @return result
     * @throws IllegalStateException if execution is not complete
     */
    Throwable getException();
    
    /**
     * Like {@link #get()}, but without checked exceptions.
     * @return result
     */
    default V _get() {
        try {
            return get();
        } catch (InterruptedException | ExecutionException e) {
            throw MiFutures.rethrowUnchecked(e);
        }
    }
    
    @SuppressWarnings("ThrowableResultIgnored")
    default V getNow() throws ExecutionException {
        assertIsDone();
        if (isCancelled()) {
            throw new CancellationException();
        }
        if (hasFailed()) {
            throw new ExecutionException(getException());
        }
        return getResult();
    }

    @Override
    default V get() throws InterruptedException, ExecutionException {
        await();
        return getNow();
    }

    @Override
    default V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        await(timeout, unit);
        return getNow();
    }

    <R> MiFuture<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> action);
    
    default <R> MiFuture<R> onComplete(Executor executor, MiFunction<? super V, ? extends R> onSuccess, MiFunction<? super Throwable, ? extends R> onFailure) {
        return onComplete(executor, MiFutures.onComplete(onSuccess, onFailure));
    }

    default <R> MiFuture<R> onSuccess(Executor executor, MiFunction<? super V, ? extends R> action) {
        return onComplete(executor, action, MiFutures.expectedSuccess());
    }

    default <R> MiFuture<R> onFailure(Executor executor, MiFunction<? super Throwable, ? extends R> action) {
        return onComplete(executor, MiFutures.expectedFail(), action);
    }
    
    default <R> MiFuture<R> onComplete(MiFunction<? super MiFuture<V>, ? extends R> action) {
        return onComplete(null, action);
    }

    default <R> MiFuture<R> onComplete(MiFunction<? super V, ? extends R> onSuccess, MiFunction<? super Throwable, ? extends R> onFailure) {
        return onComplete(null, onSuccess, onFailure);
    }

    default <R> MiFuture<R> onSuccess(MiFunction<? super V, ? extends R> action) {
        return onSuccess(null, action);
    }

    default <R> MiFuture<R> onFailure(MiFunction<? super Throwable, R> action) {
        return onFailure(null, action);
    }
}
