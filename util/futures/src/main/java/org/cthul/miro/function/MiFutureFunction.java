package org.cthul.miro.function;

import org.cthul.miro.futures.MiFuture;

/**
 *
 */
public interface MiFutureFunction<T, R, F> extends MiFunction<T, R> {
    
    F wrap(MiFuture<? extends R> future);
}
