package org.cthul.miro.util;

import org.cthul.miro.util.Closeables.FunctionalHelper;

public interface XFunction<T, R, X extends Throwable> extends FunctionalHelper {

    R apply(T t) throws X;
    
}
