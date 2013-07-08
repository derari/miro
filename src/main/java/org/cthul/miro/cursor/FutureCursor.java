package org.cthul.miro.cursor;

import java.util.Iterator;
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
     * Actions that are already registered using 
     * {@link #onComplete(org.cthul.miro.MiFutureAction)} (and similar)
     * will be executed before the close.
     */
    @Override
    void close();
    
    /** {@inheritDoc} */
    @Override
    public <R> MiFuture<R> onComplete(MiFutureAction<? super MiFuture<ResultCursor<V>>, R> action);

    /** @see #onComplete(org.cthul.miro.MiFutureAction) */
    public <R> MiFuture<R> onCompleteC(MiFutureAction<? super FutureCursor<V>, R> action);
}
