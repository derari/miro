package org.cthul.miro.result.cursor;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.function.MiSupplier;

public interface FutureCursor<V> extends MiFuture<ResultCursor<V>>, Iterable<V>, AutoCloseable {
                               
    /** 
     * Shortcut for {@code _get().iterator()}
     * @see #_get()
     * @see ResultCursor#iterator()
     */
    @Override
    default Iterator<V> iterator() {
        return _get().iterator();
    }
    
    /**
     * Closes the result cursor as soon as it is available.
     * Equivalent to {@link #sendClose()}, but implements {@link AutoCloseable}.
     */
    @Override
    default void close() {
        sendClose();
    }
    
    /**
     * Waits until execution is complete or the thread is interrupted.
     * If possible, the result cursor is closed.
     * @return true iff execution is complete.
     */
    default boolean beClosed() {
        return sendClose().beDone();
    }

    /**
     * Waits until execution is complete, the timeout expires, 
     * or the thread is interrupted.
     * If possible, the result cursor is closed.
     * @param timeout
     * @param unit
     * @return true iff execution is complete.
     */
    default boolean beClosed(long timeout, TimeUnit unit) {
        return sendClose().beDone(timeout, unit);
    }

    /**
     * Waits until execution is complete or the thread is interrupted.
     * If possible, the result cursor is closed.
     * @throws InterruptedException if thread was interrupted
     */
    default void waitUntilClosed() throws InterruptedException {
        sendClose().await();
    }
    
    /**
     * Waits until execution is complete, the timeout expires, 
     * or the thread is interrupted.
     * If possible, the result cursor is closed.
     * @param timeout
     * @param unit
     * @throws InterruptedException if thread was interrupted
     * @throws TimeoutException if timeout expired
     */
    default void waitUntilClosed(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        sendClose().await(timeout, unit);
    }
    
    /**
     * Returns an {@linkplain MiAction action} that will close the
     * result cursor and return true.
     * @return action
     */
    default MiAction<Boolean> closeAction() {
        MiSupplier<Boolean> close = () -> sendClose().get();
        return close.asAction();
    }
    
    /**
     * Closes the result cursor as soon as it is available.
     * The future result will be {@code true}.
     * @return future
     */
    default MiFuture<Boolean> sendClose() {
       return andDo(f -> {
           if (f.hasResult()) {
               f.get().close();
           }
           return true;
       });
    }
}
