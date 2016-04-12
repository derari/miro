package org.cthul.miro.futures;

import org.cthul.miro.function.MiActionFunction;
import org.cthul.miro.function.MiFutureFunction;
import org.cthul.miro.function.MiFunction;
import java.util.concurrent.Executor;

/**
 * A future that can be triggered or submitted.
 * @param <V>
 */
public interface MiAction<V> extends MiFuture<V> {
    
    /**
     * Any operation on the trigger not related to cancelling will cause 
     * the action to {@linkplain #submit() submit}.
     * @return the trigger
     */
    MiFuture<V> getTrigger();
    
    /**
     * Submits this action to be executed.
     * @return this
     */
    MiFuture<V> submit();
    
    @Override
    <R> MiAction<R> onComplete(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function);
    
    @Override
    <R> MiAction<R> onCompleteAlways(Executor executor, MiFunction<? super MiFuture<V>, ? extends R> function);
    
    @Override
    default <R> MiAction<R> onComplete(Executor executor, MiFunction<? super V, ? extends R> onSuccess, MiFunction<? super Throwable, ? extends R> onFailure) {
        return onComplete(executor, MiFutures.onComplete(onSuccess, onFailure));
    }

    @Override
    default <R> MiAction<R> onSuccess(Executor executor, MiFunction<? super V, ? extends R> action) {
        return onComplete(executor, action, MiFutures.expectedSuccess());
    }

    @Override
    default <R> MiAction<R> onFailure(Executor executor, MiFunction<? super Throwable, ? extends R> action) {
        return onComplete(executor, MiFutures.expectedFail(), action);
    }
    
    @Override
    default <R> MiAction<R> andDo(MiFunction<? super MiFuture<V>, ? extends R> action) {
        return onComplete(null, action);
    }

    @Override
    default <R> MiAction<R> andDo(MiFunction<? super V, ? extends R> onSuccess, MiFunction<? super Throwable, ? extends R> onFailure) {
        return onComplete(null, onSuccess, onFailure);
    }

    @Override
    default <R> MiAction<R> andThen(MiFunction<? super V, ? extends R> action) {
        return onSuccess(null, action);
    }
    
    @Override
    default <R, F> F andThen(MiFutureFunction<? super V, R, ? extends F> function) {
        MiAction<R> f = andThen((MiFunction<? super V, R>) function);
        return function.wrap(f.getTrigger());
    }
    
    @Override
    default <R, F> F andThen(MiActionFunction<? super V, R, ? extends F> function) {
        MiAction<R> f = andThen((MiFunction<? super V, R>) function);
        return function.wrap(f);
    }

    @Override
    default <R> MiAction<R> andFinally(MiFunction<? super MiFuture<V>, ? extends R> function) {
        return onCompleteAlways(null, function);
    }

    @Override
    default <R> MiAction<R> orCatch(MiFunction<? super Throwable, R> action) {
        return onFailure(null, action);
    }
}
