package org.cthul.miro.cursor;

import java.util.Iterator;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;
import org.cthul.miro.cursor.FutureCursor;
import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.util.FutureDelegator;

/**
 * Decorates a {@link MiFuture} to be a {@link FutureCursor}.
 * @author Arian Treffer
 * @param <V> 
 */
public class FutureCursorDelegator<V> extends FutureDelegator<ResultCursor<V>> implements FutureCursor<V> {

    public FutureCursorDelegator(MiFuture<ResultCursor<V>> delegatee) {
        super(delegatee);
    }

    @Override
    public <R> MiFuture<R> onCompleteC(final MiFutureAction<? super FutureCursor<V>, R> action) {
        return onComplete(new MiFutureAction<Object, R>() {
            @Override
            public R call(Object param) throws Exception {
                return action.call(FutureCursorDelegator.this);
            }
        });
    }

    @Override
    public void close() {
        onComplete(CLOSE_ACTION);
    }

    @Override
    public Iterator<V> iterator() {
        return _get().iterator();
    }
    
    protected static MiFutureAction<MiFuture<? extends AutoCloseable>, ?> CLOSE_ACTION = new MiFutureAction<MiFuture<? extends AutoCloseable>, Void>() {
        @Override
        public Void call(MiFuture<? extends AutoCloseable> f) throws Exception {
            if (f.hasResult()) {
                f.getResult().close();
            }
            return null;
        }
    };
}
