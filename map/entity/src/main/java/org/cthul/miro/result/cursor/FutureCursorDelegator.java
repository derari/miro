package org.cthul.miro.result.cursor;

import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.impl.MiFutureDelegator;

/**
 * Decorates a {@link MiFuture} to be a {@link FutureCursor}.
 * @param <V> 
 */
public class FutureCursorDelegator<V> extends MiFutureDelegator<ResultCursor<V>> implements FutureCursor<V> {

    public FutureCursorDelegator(MiFuture<ResultCursor<V>> delegate) {
        super(delegate);
    }
}
