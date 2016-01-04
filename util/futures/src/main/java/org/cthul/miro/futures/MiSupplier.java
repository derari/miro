package org.cthul.miro.futures;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 *
 * @param <T>
 */
@FunctionalInterface
public interface MiSupplier<T> extends Supplier<T> {
    
    T call() throws Throwable;
    
    @Override
    default T get() {
        try {
            return call();
        } catch (Throwable e) {
            throw MiFutures.rethrowUnchecked(e);
        }
    }
    
    default MiFunction<Object, T> asFunction() {
        return v -> call();
    }

    default MiFuture<T> submit(Executor executor) {
        return asAction(executor).submit();
    }
    
    default MiAction<T> asAction(Executor executor) {
        return asFunction().asAction(null);
    }
    
    default MiFuture<T> getTrigger(Executor executor) {
        return asAction(executor).getTrigger();
    }
    
    default MiFuture<T> submit() {
        return submit(null);
    }
    
    default MiAction<T> asAction() {
        return asAction(null);
    }
    
    default MiFuture<T> getTrigger() {
        return getTrigger(null);
    }
}
