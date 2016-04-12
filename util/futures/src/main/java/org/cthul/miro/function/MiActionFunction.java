package org.cthul.miro.function;

import org.cthul.miro.futures.MiAction;

/**
 *
 */
public interface MiActionFunction<T, R, F> extends MiFunction<T, R> {

    F wrap(MiAction<? extends R> action);
}
