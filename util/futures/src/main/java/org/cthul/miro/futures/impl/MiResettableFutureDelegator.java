package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import org.cthul.miro.futures.MiResettableFuture;

/**
 *
 */
public class MiResettableFutureDelegator<V> extends MiFutureDelegator<V> implements MiResettableFuture<V> {

    public MiResettableFutureDelegator(MiResettableFuture<? extends V> delegatee) {
        super(delegatee);
    }

    public MiResettableFutureDelegator(MiResettableFuture<? extends V> delegatee, Executor defaultExecutor) {
        super(delegatee, defaultExecutor);
    }

    @Override
    protected MiResettableFuture<V> getDelegatee() {
        return (MiResettableFuture<V>) super.getDelegatee();
    }

    @Override
    protected MiResettableFuture<V> getCancelDelegatee() {
        return (MiResettableFuture<V>) super.getCancelDelegatee();
    }

    @Override
    public MiResettableFuture<V> reset() {
        getCancelDelegatee().reset();
        return this;
    }
}
