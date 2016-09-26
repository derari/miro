package org.cthul.miro.util;

import org.cthul.miro.util.Closeables.FunctionalHelper;

public interface XConsumer<T, X extends Throwable> extends FunctionalHelper {

    void accept(T t) throws X;
    
}
