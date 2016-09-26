package org.cthul.miro.util;

import org.cthul.miro.util.Closeables.FunctionalHelper;

public interface XTriFunction<T, U, V, R, X extends Throwable> extends FunctionalHelper {

    R apply(T t, U u, V v) throws X;
    
}
