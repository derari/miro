package org.cthul.miro.util;

public interface XSupplier<T, X extends Throwable> {

    T get() throws X;
}
