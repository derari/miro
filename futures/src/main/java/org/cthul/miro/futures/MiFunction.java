package org.cthul.miro.futures;

import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 *
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface MiFunction<T, R> extends Function<T, R> {
    
    R call(T arg) throws Throwable;
    
    @Override
    default R apply(T arg) {
        try {
            return call(arg);
        } catch (Throwable e) {
            throw MiFutures.rethrowUnchecked(e);
        }
    }
    
    default MiSupplier<R> withArg(T arg) {
        return () -> apply(arg);
    }

    default MiFuture<R> submit(Executor executor, T arg) {
        return MiFutures.submit(executor, arg, this);
    }
    
    default MiAction<R> asAction(Executor executor, T arg) {
        return MiFutures.action(executor, arg, this);
    }
    
    default MiFuture<R> getTrigger(Executor executor, T arg) {
        return asAction(executor, arg).getTrigger();
    }
    
    default MiFuture<R> submit(T arg) {
        return submit(null, arg);
    }
    
    default MiAction<R> asAction(T arg) {
        return asAction(null, arg);
    }
    
    default MiFuture<R> getTrigger(T arg) {
        return getTrigger(null, arg);
    }
}
