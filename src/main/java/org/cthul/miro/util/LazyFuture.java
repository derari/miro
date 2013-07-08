package org.cthul.miro.util;

import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;

public abstract class LazyFuture<V> extends FutureDelegator<V> {

    private MiFuture<V> value;

    public LazyFuture() {
        super(null);
    }
    
    @Override
    protected MiFuture<V> getDelegatee() {
        if (value == null) {
            initValue();
        }
        return value;
    }

    private synchronized void initValue() {
        if (value == null) {
            try {
                value = (MiFuture) initialize();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            value.onComplete(new MiFutureAction<MiFuture<V>, Void>() {
                @Override
                public Void call(MiFuture<V> param) throws Exception {
                    if (param.hasResult()) {
                        value = new FinalFuture<>(param.getResult());
                    } else {
                        value = new FinalFuture<>(param.getException());
                    }
                    return null;
                }
            });
        }
    }

    protected abstract MiFuture<? extends V> initialize() throws Exception;
}
