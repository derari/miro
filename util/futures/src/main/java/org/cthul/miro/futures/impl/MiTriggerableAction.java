package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.function.MiFunction;
import org.cthul.miro.futures.MiActionResult;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.MiResettableFuture;
import org.cthul.miro.futures.MiResult;
import static org.cthul.miro.futures.MiFutures.futureAsAction;
import static org.cthul.miro.futures.MiFutures.futureAsAction;

/**
 *
 * @param <V>
 */
public class MiTriggerableAction<V> extends AbstractMiSubmittable<V> implements MiActionResult<V> {

    private final Consumer<? super Result<V>> action;
    private MiResettableFuture<V> trigger = null;
    private transient long myAttempt = -1;
    
    public MiTriggerableAction(Consumer<? super Result<V>> action) {
        this(action, true);
    }

    public MiTriggerableAction(Consumer<? super Result<V>> action, boolean resettable) {
        super(null, resettable);
        this.action = action;
    }

    public MiTriggerableAction(Consumer<? super Result<V>> action, Executor defaultExecutor, boolean resettable) {
        super(defaultExecutor, resettable);
        this.action = action;
    }

    public MiTriggerableAction(Consumer<? super Result<V>> action, Executor executor, Executor defaultExecutor, boolean resettable) {
        super(executor, defaultExecutor, resettable);
        this.action = action;
    }

    @Override
    public MiResettableFuture<V> getTrigger() {
        class Trigger extends MiResettableFutureDelegator<V> {
            public Trigger() {
                super(MiTriggerableAction.this);
            }
            @Override
            protected MiResettableFuture<V> getDelegate() {
                MiTriggerableAction.this.submit();
                return super.getDelegate();
            }
            @Override
            protected MiResettableFuture<V> getCancelDelegate() {
                return super.getDelegate();
            }
        }
        if (trigger == null) {
            trigger = new Trigger();
        }
        return trigger;
    }

    @Override
    public MiFuture<V> submit() {
        return submit(new Runner());
    }

    @Override
    public <R> MiAction<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
        return futureAsAction(super.onComplete(executor, function), executor, this::submit);
    }

    @Override
    public <R> MiAction<R> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
        return futureAsAction(super.onCompleteAlways(executor, function), executor, this::submit);
    }

    @Override
    public MiResettableFuture<V> reset() {
        slowReset();
        return this;
    }
    
    @Override
    public void setResult(V value) {
       result(myAttempt, value);
    }

    @Override
    public void setFail(Throwable throwable) {
        fail(myAttempt, throwable);
    }
    
    @Override
    public boolean continueWork() {
        return progress(myAttempt);
    }

    protected class Runner extends AbstractMiSubmittable.Runner {
        @Override
        protected void call(long attempt) {
            synchronized (lock()) {
                myAttempt = Math.max(myAttempt, attempt);
            }
            action.accept(new Result<>(MiTriggerableAction.this, attempt));
        }
    }
    
    public static class Result<V> implements MiResult<V> {
        private final MiTriggerableAction<V> owner;
        private final long attempt;

        public Result(MiTriggerableAction<V> owner, long attempt) {
            this.owner = owner;
            this.attempt = attempt;
        }
        
        @Override
        public void setResult(V value) {
            owner.result(attempt, value);
        }
        
        @Override
        public void setFail(Throwable throwable) {
            owner.fail(attempt, throwable);
        }

        @Override
        public boolean continueWork() {
            return owner.progress(attempt);
        }
    }
}
