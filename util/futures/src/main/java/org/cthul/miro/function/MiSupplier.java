package org.cthul.miro.function;

import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.futures.MiFutures;
import org.cthul.miro.futures.MiResettableAction;
import org.cthul.miro.futures.MiResettableFuture;

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
    
    default MiResettableAction<T> asAction(Executor executor) {
        return MiFutures.action(executor, this);
    }
    
    default MiResettableFuture<T> getTrigger(Executor executor) {
        return asAction(executor).getTrigger();
    }
    
    default MiFuture<T> submit() {
        return submit(null);
    }
    
    default MiResettableAction<T> asAction() {
        return asAction(null);
    }
    
    default MiResettableFuture<T> getTrigger() {
        return getTrigger(null);
    }
}
