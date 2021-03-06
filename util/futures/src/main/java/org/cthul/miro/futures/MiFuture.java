package org.cthul.miro.futures;

import org.cthul.miro.function.MiActionFunction;
import org.cthul.miro.function.MiFutureFunction;
import org.cthul.miro.function.MiFunction;
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
     * Waits for the operation to complete.
     * Throws an {@code IllegalStateException} if the operation does not 
     * complete or fails.
     * @throws IllegalStateException if no success
     */
    default void awaitSuccess() {
        try {
            await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (hasResult()) return;
        throw new IllegalStateException("Failed.");
    }
    
    /**
     * Waits for the operation to complete.
     * Throws an {@code IllegalStateException} if the operation does not 
     * complete or fails.
     * @param timeout
     * @param unit
     * @throws IllegalStateException if no success
     */
    default void awaitSuccess(long timeout, TimeUnit unit) {
        try {
            await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) { }
        if (hasResult()) return;
        throw new IllegalStateException("Failed.");
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
    
    /**
     * Throws an {@code IllegalStateException} if not {@linkplain #isDone() done}.
     * @throws IllegalStateException if not done
     */
    default void assertIsDone() {
        if (isDone()) return;
        throw new IllegalStateException("Not done yet.");
    }
    
    /**
     * Cancels computation of this value and all input values.
     * @param mayInterruptIfRunning
     * @return 
     */
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
    
    /**
     * Like {@link #get()}, but immediately throws an 
     * {@code IllegalStateException} if no result is available.
     * @return result
     * @throws ExecutionException 
     * @throws IllegalStateException if not done
     */
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

    <R> MiFuture<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function);

    <R> MiFuture<R> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function);
    
    default <R> MiFuture<R> onComplete(Executor executor, MiFunction<? super V, ? extends R> onSuccess, MiFunction<? super Throwable, ? extends R> onFailure) {
        return onComplete(executor, MiFutures.onComplete(onSuccess, onFailure));
    }

    default <R> MiFuture<R> onSuccess(Executor executor, MiFunction<? super V, ? extends R> function) {
        return onComplete(executor, function, MiFutures.expectedSuccess());
    }

    default <R> MiFuture<R> onFailure(Executor executor, MiFunction<? super Throwable, ? extends R> function) {
        return onComplete(executor, MiFutures.expectedFail(), function);
    }
    
    default <R> MiFuture<R> andDo(MiFunction<? super MiFuture<V>, ? extends R> function) {
        return onComplete(null, function);
    }

    default <R> MiFuture<R> andDo(MiFunction<? super V, ? extends R> onSuccess, MiFunction<? super Throwable, ? extends R> onFailure) {
        return onComplete(null, onSuccess, onFailure);
    }
    
    default <R> MiFuture<R> andThen(MiFunction<? super V, ? extends R> function) {
        return onSuccess(null, function);
    }

    default <R, F> F andThen(MiFutureFunction<? super V, R, ? extends F> function) {
        MiFuture<R> f = andThen((MiFunction<? super V, R>) function);
        return function.wrap(f);
    }

    default <R, F> F andThen(MiActionFunction<? super V, R, ? extends F> function) {
        MiFuture<R> f = andThen((MiFunction<? super V, R>) function);
        MiAction<R> a = MiFutures.futureAsAction(f);
        return function.wrap(a);
    }

    default <R> MiFuture<R> andFinally(MiFunction<? super MiFuture<V>, ? extends R> function) {
        return onCompleteAlways(null, function);
    }

    default <R> MiFuture<R> orCatch(MiFunction<? super Throwable, R> function) {
        return onFailure(null, function);
    }
}
