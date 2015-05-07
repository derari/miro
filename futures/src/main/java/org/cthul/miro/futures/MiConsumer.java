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
    
    default MiFunction<T, Void> asFunction() {
        return v -> {accept(v); return null;};
    }

    default MiFuture<Void> submit(Executor executor, T value) {
        return asAction(executor, value).submit();
    }
    
    default MiAction<Void> asAction(Executor executor, T value) {
        return asFunction().asAction(value);
    }
    
    default MiFuture<Void> getTrigger(Executor executor, T value) {
        return asAction(executor, value).getTrigger();
    }
    
    default MiFuture<Void> submit(T value) {
        return submit(null, value);
    }
    
    default MiAction<Void> asAction(T value) {
        return asAction(null, value);
    }
    
    default MiFuture<Void> getTrigger(T value) {
        return getTrigger(null, value);
    }
}
