package org.cthul.miro;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface MiFuture<V> extends Future<V> {

    /**
     * Waits until execution is complete or the thread is interrupted.
     * @return true iff execution is complete.
     */
    boolean beDone();

    /**
     * Waits until execution is complete, the timeout expires, 
     * or the thread is interrupted.
     * @return true iff execution is complete.
     */
    boolean beDone(long timeout, TimeUnit unit);

    /**
     * Waits until execution is complete or the thread is interrupted.
     * @throws InterruptedException if thread was interrupted
     */
    void waitUntilDone() throws InterruptedException;
    
    /**
     * Waits until execution is complete, the timeout expires, 
     * or the thread is interrupted.
     * @throws InterruptedException if thread was interrupted
     * @throws TimeoutException if timeout expired
     */
    void waitUntilDone(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
    
    /**
     * 
     * @throws IllegalStateException if execution is not complete
     */
    boolean hasResult();

    /**
     * 
     * @throws IllegalStateException if execution is not complete
     */
    boolean hasFailed();

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
     */
    V _get();

    <R> MiFuture<R> onComplete(MiFutureAction<? super MiFuture<V>, R> action);

    <R> MiFuture<R> onComplete(MiFutureAction<? super V, R> onSuccess, MiFutureAction<? super Throwable, R> onFailure);

    <R> MiFuture<R> onSuccess(MiFutureAction<? super V, R> action);

    <R> MiFuture<R> onFailure(MiFutureAction<? super Throwable, R> action);
}
