package org.cthul.miro.futures;

import java.util.concurrent.Executor;

/**
 * The result of a function that has to be submitted or triggered.
 * @param <T>
 * @param <R> 
 */
public class MiAction<T, R> extends AbstractMiAction<T, R> {

    private final T arg;
    private MiFuture<R> trigger = null;

    public MiAction(Executor executor, T arg, MiFunction<? super T, ? extends R> function) {
        this(executor, arg, function, executor);
    }

    public MiAction(Executor executor, T arg, MiFunction<? super T, ? extends R> function, Executor defaultExecutor) {
        super(executor, function, defaultExecutor);
        this.arg = arg;
    }

    /**
     * Any operation on the trigger not related do cancelling will cause 
     * the action to {@linkplain #submit() submit}.
     * @return the trigger
     */
    public MiFuture<R> getTrigger() {
        if (trigger == null) {
            trigger = new MiFutureDelegator<R>(this) {
                @Override
                protected MiFuture<R> getDelegatee() {
                    submit();
                    return super.getDelegatee();
                }
                @Override
                protected MiFuture<R> getCancelDelegatee() {
                    return super.getDelegatee();
                }
            };
        }
        return trigger;
    }
    
    public MiFuture<R> submit() {
        return submit(arg);
    }
}
