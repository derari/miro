package org.cthul.miro.function;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.MiFutures;
import org.cthul.miro.futures.MiResettableAction;
import org.cthul.miro.futures.MiResettableFuture;

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
        return v -> {call(v); return v;};
    }
    
    default MiSupplier<T> curry(T value) {
        return () -> {call(value); return value;};
    }

    default MiFuture<T> submit(Executor executor, T value) {
        return curry(value).submit(executor);
    }
    
    default MiResettableAction<T> asAction(Executor executor, T value) {
        return curry(value).asAction(executor);
    }
    
    default MiResettableFuture<T> getTrigger(Executor executor, T value) {
        return curry(value).getTrigger(executor);
    }
    
    default MiFuture<T> submit(T value) {
        return curry(value).submit();
    }
    
    default MiResettableAction<T> asAction(T value) {
        return curry(value).asAction();
    }
    
    default MiResettableFuture<T> getTrigger(T value) {
        return curry(value).getTrigger();
    }
}
