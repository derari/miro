package org.cthul.miro.futures;

/**
 *
 */
public interface MiActionFunction<T, R, F> extends MiFunction<T, R> {
    
    F wrap(MiAction<? extends R> action);
}
