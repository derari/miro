package org.cthul.miro.util;

import org.cthul.miro.MiFuture;

/**
 * A {@link MiFuture Future} with final value.
 * @param <V> 
 */
public class FinalFuture<V> extends FutureBase<V> {
    
    public static <V> FinalFuture<V> forValue(V value) {
        return new FinalFuture<>(value);
    }

    public static <V> FinalFuture<V> forError(Throwable error) {
        return new FinalFuture<>(error);
    }

    public FinalFuture(V value) {
        super(null);
        setValue(value);
    }

    public FinalFuture(Throwable error) {
        super(null);
        setException(error);
    }
}
