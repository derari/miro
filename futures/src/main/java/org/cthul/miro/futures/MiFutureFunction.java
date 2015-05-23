package org.cthul.miro.futures;

/**
 *
 */
public interface MiFutureFunction<T, R, F> extends MiFunction<T, R> {
    
    F wrap(MiFuture<? extends R> future);
}
