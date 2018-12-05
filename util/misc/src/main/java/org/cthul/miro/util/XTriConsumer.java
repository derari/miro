package org.cthul.miro.util;

public interface XTriConsumer<T, U, V, X extends Throwable> {

    void accept(T t, U u, V v) throws X;
    
}
