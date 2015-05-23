package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFunction;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.MiFutures;

/**
 * The result of a function that has to be submitted or triggered.
 * @param <T>
 * @param <R> 
 */
public class SimpleMiAction<T, R> extends SubmittableMiFuture<T, R> implements MiAction<R> {

    private final T arg;
    private MiFuture<R> trigger = null;

    public SimpleMiAction(Executor executor, T arg, MiFunction<? super T, ? extends R> function) {
        this(executor, arg, function, executor);
    }

    public SimpleMiAction(Executor executor, T arg, MiFunction<? super T, ? extends R> function, Executor defaultExecutor) {
        super(executor, function, defaultExecutor);
        this.arg = arg;
    }

    /**
     * Any operation on the trigger not related do cancelling will cause 
     * the action to {@linkplain #submit() submit}.
     * @return the trigger
     */
    @Override
    public MiFuture<R> getTrigger() {
        if (trigger == null) {
            trigger = MiFutures.trigger(this);
        }
        return trigger;
    }
    
    @Override
    public MiFuture<R> submit() {
        return submit(arg);
    }

    @Override
    public <R2> MiAction<R2> onComplete(Executor executor, MiFunction<? super MiFuture<R>, ? extends R2> function) {
        MiFuture<R2> f = super.onComplete(executor, function);
        return new OnComplete<>(f, executor);
    }

    @Override
    public <R2> MiAction<R2> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<R>, ? extends R2> function) {
        MiFuture<R2> f = super.onCompleteAlways(executor, function);
        return new OnComplete<>(f, executor);
    }
    
    class OnComplete<V> extends MiFutureDelegator<V> implements MiAction<V> {
        
        private MiFuture<V> trigger = null;

        public OnComplete(MiFuture<V> delegatee, Executor defaultExecutor) {
            super(delegatee, defaultExecutor);
        }

        @Override
        public MiFuture<V> getTrigger() {
            if (trigger == null) {
                trigger = MiFutures.trigger(this);
            }
            return trigger;
        }

        @Override
        public MiFuture<V> submit() {
            SimpleMiAction.this.submit();
            return this;
        }

        @Override
        public <R> MiAction<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
            MiFuture<R> f = super.onComplete(executor, function);
            return new OnComplete<>(f, executor);
        }

        @Override
        public <R> MiAction<R> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function) {
            MiFuture<R> f = super.onCompleteAlways(executor, function);
            return new OnComplete<>(f, executor);
        }
    }
}
