package org.cthul.miro.cursor;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;
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
    public Iterator<V> iterator() {
        return _get().iterator();
    }

    @Override
    public void close() {
        sendClose();
    }

    @Override
    public boolean beClosed() {
        return sendClose().beDone();
    }

    @Override
    public boolean beClosed(long timeout, TimeUnit unit) {
        return sendClose().beDone(timeout, unit);
    }

    @Override
    public void waitUntilClosed() throws InterruptedException {
        sendClose().waitUntilDone();
    }

    @Override
    public void waitUntilClosed(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        sendClose().waitUntilDone(timeout, unit);
    }

    @Override
    public MiFutureAction<Object, Boolean> closeAction() {
        return new MiFutureAction<Object, Boolean>() {
            @Override
            public Boolean call(Object arg) throws Exception {
                if (hasResult()) {
                    getResult().close();
                }
                return true;
            }
        };
    }

    @Override
    public MiFuture<Boolean> sendClose() {
        return onComplete(closeCursorAction());
    }
    
    @Override
    public <R> MiFuture<R> onCursorComplete(final MiFutureAction<? super FutureCursor<V>, R> action) {
        return onComplete(new MiFutureAction<MiFuture<ResultCursor<V>>, R>() {
            @Override
            public R call(MiFuture<ResultCursor<V>> arg) throws Throwable {
                assert arg == getDelegatee();
                return action.call(FutureCursorDelegator.this);
            }
        });
    }

    public static MiFutureAction<MiFuture<? extends AutoCloseable>, Boolean> closeCursorAction() {
        return CLOSE_ACTION;
    }
    
    protected static MiFutureAction<MiFuture<? extends AutoCloseable>, Boolean> CLOSE_ACTION = new MiFutureAction<MiFuture<? extends AutoCloseable>, Boolean>() {
        @Override
        public Boolean call(MiFuture<? extends AutoCloseable> f) throws Exception {
            if (f.hasResult()) {
                f.getResult().close();
            }
            return true;
        }
    };
}
