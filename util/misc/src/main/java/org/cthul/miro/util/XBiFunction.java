package org.cthul.miro.util;

import org.cthul.miro.util.Closables.FunctionalHelper;

public interface XBiFunction<T, U, R, X extends Throwable> extends FunctionalHelper {

    R apply(T t, U u) throws X;
    
}
