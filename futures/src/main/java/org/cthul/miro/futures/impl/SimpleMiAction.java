package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFunction;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.MiFutures;
import static org.cthul.miro.futures.MiFutures.futureAsAction;

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
     * Any operation on the trigger not related do canceling will cause 
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
        return futureAsAction(f, executor, this::submit);
    }

    @Override
    public <R2> MiAction<R2> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<R>, ? extends R2> function) {
        MiFuture<R2> f = super.onCompleteAlways(executor, function);
        return futureAsAction(f, executor, this::submit);
    }
}
