package org.cthul.miro.function;

import java.util.concurrent.Executor;
import java.util.function.Function;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.MiFutures;
import org.cthul.miro.futures.MiResettableAction;
import org.cthul.miro.futures.MiResettableFuture;

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
    
    default MiSupplier<R> curry(T arg) {
        return () -> apply(arg);
    }

    default MiFuture<R> submit(Executor executor, T arg) {
        return curry(arg).submit(executor);
    }
    
    default MiResettableAction<R> asAction(Executor executor, T arg) {
        return curry(arg).asAction(executor);
    }
    
    default MiResettableFuture<R> getTrigger(Executor executor, T arg) {
        return curry(arg).getTrigger(executor);
    }
    
    default MiFuture<R> submit(T arg) {
        return curry(arg).submit();
    }
    
    default MiResettableAction<R> asAction(T arg) {
        return curry(arg).asAction();
    }
    
    default MiResettableFuture<R> getTrigger(T arg) {
        return curry(arg).getTrigger();
    }
}
