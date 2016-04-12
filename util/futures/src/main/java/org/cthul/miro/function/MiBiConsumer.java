package org.cthul.miro.function;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.MiFutures;
import org.cthul.miro.futures.MiResettableAction;
import org.cthul.miro.futures.MiResettableFuture;

/**
 *
 * @param <T>
 * @param <U>
 */
@FunctionalInterface
public interface MiBiConsumer<T, U> extends BiConsumer<T, U> {
    
    void call(T value1, U value2) throws Throwable;
    
    @Override
    default void accept(T value1, U value2) {
        try {
            call(value1, value2);
        } catch (Throwable e) {
            throw MiFutures.rethrowUnchecked(e);
        }
    }
    
    default MiBiFunction<T, U, T> asFunction1() {
        return (t, u) -> {call(t,u); return t;};
    }
    
    default MiBiFunction<T, U, U> asFunction2() {
        return (t, u) -> {call(t,u); return u;};
    }
    
    default MiSupplier<T> curry1(T t, U u) {
        return () -> {call(t, u); return t;};
    }

    default MiSupplier<U> curry2(T t, U u) {
        return () -> {call(t, u); return u;};
    }

    default MiFuture<T> submit(Executor executor, T value1, U value2) {
        return curry1(value1, value2).submit(executor);
    }
   
    default MiResettableAction<T> asAction(Executor executor, T value1, U value2) {
        return curry1(value1, value2).asAction(executor);
    }
    
    default MiResettableFuture<T> getTrigger(Executor executor, T value1, U value2) {
        return curry1(value1, value2).getTrigger(executor);
    }
    
    default MiFuture<T> submit(T value1, U value2) {
        return curry1(value1, value2).submit();
    }
    
    default MiResettableAction<T> asAction(T value1, U value2) {
        return curry1(value1, value2).asAction();
    }
    
    default MiResettableFuture<T> getTrigger(T value1, U value2) {
        return curry1(value1, value2).getTrigger();
    }
}
