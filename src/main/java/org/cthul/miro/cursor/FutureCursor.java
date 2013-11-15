package org.cthul.miro.cursor;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;

public interface FutureCursor<V> extends MiFuture<ResultCursor<V>>, Iterable<V>, AutoCloseable {

    /** 
     * Shortcut for {@code _get().iterator()}
     * @see #_get()
     * @see ResultCursor#iterator()
     */
    @Override
    Iterator<V> iterator();
    
    /**
     * Closes the result cursor as soon as it is available.
     * Equivalent to {@link #sendClose()}, but implements {@link AutoCloseable}.
     */
    @Override
    void close();
    
    /**
     * Waits until execution is complete or the thread is interrupted.
     * If possible, the result cursor is closed.
     * @return true iff execution is complete.
     */
    boolean beClosed();

    /**
     * Waits until execution is complete, the timeout expires, 
     * or the thread is interrupted.
     * If possible, the result cursor is closed.
     * @return true iff execution is complete.
     */
    boolean beClosed(long timeout, TimeUnit unit);

    /**
     * Waits until execution is complete or the thread is interrupted.
     * If possible, the result cursor is closed.
     * @throws InterruptedException if thread was interrupted
     */
    void waitUntilClosed() throws InterruptedException;
    
    /**
     * Waits until execution is complete, the timeout expires, 
     * or the thread is interrupted.
     * If possible, the result cursor is closed.
     * @throws InterruptedException if thread was interrupted
     * @throws TimeoutException if timeout expired
     */
    void waitUntilClosed(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
    
    /**
     * Returns an {@linkplain MiFutureAction action} that will close the
     * result cursor and return true.
     * @return action
     */
    MiFutureAction<Object, Boolean> closeAction();
    
    /**
     * Closes the result cursor as soon as it is available.
     * The future result will be {@code true}.
     * @return future
     */
    MiFuture<Boolean> sendClose();
    
    /** {@inheritDoc} */
    @Override
    public <R> MiFuture<R> onComplete(MiFutureAction<? super MiFuture<ResultCursor<V>>, R> action);

    /** @see #onComplete(org.cthul.miro.MiFutureAction) */
    public <R> MiFuture<R> onCursorComplete(MiFutureAction<? super FutureCursor<V>, R> action);
}
