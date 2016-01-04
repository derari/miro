package org.cthul.miro.futures;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;

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
    
    default MiSupplier<R> withArg(T t, U u) {
        return () -> apply(t, u);
    }

    default MiFuture<R> submit(Executor executor, T t, U u) {
        return asAction(executor, t, u).submit();
    }
    
    default MiAction<R> asAction(Executor executor, T t, U u) {
        return MiFutures.action(executor, null, x -> call(t, u));
    }
    
    default MiFuture<R> getTrigger(Executor executor, T t, U u) {
        return asAction(executor, t, u).getTrigger();
    }
    
    default MiFuture<R> submit(T t, U u) {
        return submit(null, t, u);
    }
    
    default MiAction<R> asAction(T t, U u) {
        return asAction(null, t, u);
    }
    
    default MiFuture<R> getTrigger(T t, U u) {
        return getTrigger(null, t, u);
    }
}
