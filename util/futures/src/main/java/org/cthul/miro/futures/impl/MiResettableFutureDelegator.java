package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import org.cthul.miro.futures.MiResettableFuture;

/**
 *
 */
public class MiResettableFutureDelegator<V> extends MiFutureDelegator<V> implements MiResettableFuture<V> {

    public MiResettableFutureDelegator(MiResettableFuture<? extends V> delegate) {
        super(delegate);
    }

    public MiResettableFutureDelegator(MiResettableFuture<? extends V> delegate, Executor defaultExecutor) {
        super(delegate, defaultExecutor);
    }

    @Override
    protected MiResettableFuture<V> getDelegate() {
        return (MiResettableFuture<V>) super.getDelegate();
    }

    @Override
    protected MiResettableFuture<V> getCancelDelegate() {
        return (MiResettableFuture<V>) super.getCancelDelegate();
    }

    @Override
    public MiResettableFuture<V> reset() {
        getCancelDelegate().reset();
        return this;
    }
}
