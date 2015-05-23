package org.cthul.miro.futures;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 *
 * @param <T>
 */
@FunctionalInterface
public interface MiConsumer<T> extends Consumer<T> {
    
    void call(T value) throws Throwable;
    
    @Override
    default void accept(T value) {
        try {
            call(value);
        } catch (Throwable e) {
            throw MiFutures.rethrowUnchecked(e);
        }
    }
    
    default MiFunction<T, T> asFunction() {
        return v -> {accept(v); return v;};
    }

    default MiFuture<T> submit(Executor executor, T value) {
        return asAction(executor, value).submit();
    }
    
    default MiAction<T> asAction(Executor executor, T value) {
        return asFunction().asAction(value);
    }
    
    default MiFuture<T> getTrigger(Executor executor, T value) {
        return asAction(executor, value).getTrigger();
    }
    
    default MiFuture<T> submit(T value) {
        return submit(null, value);
    }
    
    default MiAction<T> asAction(T value) {
        return asAction(null, value);
    }
    
    default MiFuture<T> getTrigger(T value) {
        return getTrigger(null, value);
    }
}
