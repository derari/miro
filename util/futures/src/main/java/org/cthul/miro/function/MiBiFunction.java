package org.cthul.miro.function;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.MiFutures;

/**
 *
 * @param <T>
 * @param <U>
 * @param <R>
 */
@FunctionalInterface
public interface MiBiFunction<T, U, R> extends BiFunction<T, U, R> {
    
    R call(T t, U u) throws Throwable;
    
    @Override
    default R apply(T t, U u) {
        try {
            return call(t, u);
        } catch (Throwable e) {
            throw MiFutures.rethrowUnchecked(e);
        }
    }
    
    default MiSupplier<R> curry(T t, U u) {
        return () -> apply(t, u);
    }
    
    default MiFunction<U, R> curry1(T t) {
        return u -> apply(t, u);
    }
    
    default MiFunction<T, R> curry2(U u) {
        return t -> apply(t, u);
    }

    default MiFuture<R> submit(Executor executor, T t, U u) {
        return curry(t, u).submit(executor);
    }
    
    default MiAction<R> asAction(Executor executor, T t, U u) {
        return curry(t, u).asAction(executor);
    }
    
    default MiFuture<R> getTrigger(Executor executor, T t, U u) {
        return curry(t, u).getTrigger(executor);
    }
    
    default MiFuture<R> submit(T t, U u) {
        return curry(t, u).submit();
    }
    
    default MiAction<R> asAction(T t, U u) {
        return curry(t, u).asAction();
    }
    
    default MiFuture<R> getTrigger(T t, U u) {
        return curry(t, u).getTrigger();
    }
}
