package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import org.cthul.miro.futures.MiFuture;

/**
 * A {@link MiFuture Future} with final value.
 * @param <V> 
 */
public class MiFinal<V> extends AbstractMiFuture<V> {
    
    public static <V> MiFinal<V> cancelled() {
        return new MiFinal<>();
    }

    public static <V> MiFinal<V> forValue(V value) {
        return new MiFinal<>(value);
    }

    public static <V> MiFinal<V> forError(Throwable error) {
        return new MiFinal<>(error);
    }

    public static <V> MiFinal<V> cancelled(Executor executor) {
        return new MiFinal<>(executor);
    }

    public static <V> MiFinal<V> forValue(Executor executor, V value) {
        return new MiFinal<>(executor, value);
    }

    public static <V> MiFinal<V> forError(Executor executor, Throwable error) {
        return new MiFinal<>(executor, error);
    }

    public MiFinal() {
        super(false);
        cancel(false);
    }

    public MiFinal(V value) {
        super(false);
        result(start(), value);
    }

    public MiFinal(Throwable error) {
        super(false);
        fail(start(), error);
    }
    
    public MiFinal(Executor executor) {
        super(executor, false);
        cancel(false);
    }
    
    public MiFinal(Executor executor, V value) {
        super(executor, false);
        result(start(), value);
    }

    public MiFinal(Executor executor, Throwable error) {
        super(executor, false);
        fail(start(), error);
    }
}
