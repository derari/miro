package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.function.MiFunction;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.MiResettableAction;
import org.cthul.miro.futures.MiResettableFuture;
import org.cthul.miro.function.MiSupplier;
import static org.cthul.miro.futures.MiFutures.futureAsAction;

/**
 * The result of a supplier that has to be submitted or triggered.
 * @param <V> 
 */
public class MiSubmittableAction<V> extends AbstractMiSubmittable<V> implements MiResettableAction<V> {

    private final MiSupplier<? extends V> action;
    private MiResettableFuture<V> trigger = null;

    public MiSubmittableAction(MiSupplier<? extends V> action, boolean resettable) {
        super(resettable);
        this.action = action;
    }

    public MiSubmittableAction(MiSupplier<? extends V> action, Executor executor, boolean resettable) {
        super(executor, resettable);
        this.action = action;
    }

    public MiSubmittableAction(MiSupplier<? extends V> action, Executor executor, Executor defaultExecutor, boolean resettable) {
        super(executor, defaultExecutor, resettable);
        this.action = action;
    }

    @Override
    public MiResettableFuture<V> getTrigger() {
        class Trigger extends MiResettableFutureDelegator<V> {
            public Trigger() {
                super(MiSubmittableAction.this);
            }
            @Override
            public void await() throws InterruptedException {
                MiSubmittableAction.this.run();
                super.await();
            }
            @Override
            protected MiResettableFuture<V> getDelegatee() {
                MiSubmittableAction.this.submit();
                return super.getDelegatee();
            }
            @Override
            protected MiResettableFuture<V> getCancelDelegatee() {
                return super.getDelegatee();
            }
        }
        if (trigger == null) {
            trigger = new Trigger();
        }
        return trigger;
    }

    @Override
    protected void tryRunNow(long ms) {
        run();
    }
    
    protected void run() {
        run(new Runner());
    }
    
    @Override
    public MiResettableFuture<V> submit() {
        return (MiResettableFuture<V>) submit(new Runner());
    }

    @Override
    public MiResettableFuture<V> reset() {
        slowReset();
        return this;
    }

    @Override
    public <R2> MiAction<R2> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R2> function) {
        MiFuture<R2> f = super.onComplete(executor, function);
        return futureAsAction(f, executor, this::submit, this::run);
    }

    @Override
    public <R2> MiAction<R2> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R2> function) {
        MiFuture<R2> f = super.onCompleteAlways(executor, function);
        return futureAsAction(f, executor, this::submit, this::run);
    }
    
    protected class Runner extends AbstractMiSubmittable.Runner {
        @Override
        protected void call(long attempt) throws Throwable {
            V result = action.call();
            result(result);
        }
    }
}
