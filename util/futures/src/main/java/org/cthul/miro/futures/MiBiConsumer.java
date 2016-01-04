package org.cthul.miro.futures;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

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
    
    default MiBiFunction<T, U, T> asFunction() {
        return (t, u) -> {call(t,u); return t;};
    }

    default MiFuture<T> submit(Executor executor, T value1, U value2) {
        return asAction(executor, value1, value2).submit();
    }
   
    default MiAction<T> asAction(Executor executor, T t, U u) {
        return MiFutures.action(executor, null, x -> {call(t, u); return t;});
    }
    
    default MiFuture<T> getTrigger(Executor executor, T value1, U value2) {
        return asAction(executor, value1, value2).getTrigger();
    }
    
    default MiFuture<T> submit(T value1, U value2) {
        return submit(null, value1, value2);
    }
    
    default MiAction<T> asAction(T value1, U value2) {
        return asAction(null, value1, value2);
    }
    
    default MiFuture<T> getTrigger(T value1, U value2) {
        return getTrigger(null, value1, value2);
    }
}
