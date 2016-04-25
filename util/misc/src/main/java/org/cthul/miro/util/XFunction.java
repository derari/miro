package org.cthul.miro.util;

import org.cthul.miro.util.Closables.FunctionalHelper;

public interface XFunction<T, R, X extends Throwable> extends FunctionalHelper {

    R apply(T t) throws X;
    
}
