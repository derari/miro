package org.cthul.miro.util;

public interface XBiConsumer<T, U, X extends Throwable> {

    void accept(T t, U u) throws X;
    
}
