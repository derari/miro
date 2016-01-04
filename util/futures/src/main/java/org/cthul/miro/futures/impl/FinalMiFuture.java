package org.cthul.miro.futures.impl;

import java.util.concurrent.Executor;
import org.cthul.miro.futures.MiFuture;

/**
 * A {@link MiFuture Future} with final value.
 * @param <V> 
 */
public class FinalMiFuture<V> extends AbstractMiFuture<V> {
    
    public static <V> FinalMiFuture<V> cancelled() {
        return new FinalMiFuture<>();
    }

    public static <V> FinalMiFuture<V> forValue(V value) {
        return new FinalMiFuture<>(value);
    }

    public static <V> FinalMiFuture<V> forError(Throwable error) {
        return new FinalMiFuture<>(error);
    }

    public static <V> FinalMiFuture<V> cancelled(Executor executor) {
        return new FinalMiFuture<>(executor);
    }

    public static <V> FinalMiFuture<V> forValue(Executor executor, V value) {
        return new FinalMiFuture<>(executor, value);
    }

    public static <V> FinalMiFuture<V> forError(Executor executor, Throwable error) {
        return new FinalMiFuture<>(executor, error);
    }

    public FinalMiFuture() {
        super();
        cancel(false);
    }

    public FinalMiFuture(V value) {
        super();
        result(value);
    }

    public FinalMiFuture(Throwable error) {
        super();
        fail(error);
    }
    
    public FinalMiFuture(Executor executor) {
        super(executor);
        cancel(false);
    }
    
    public FinalMiFuture(Executor executor, V value) {
        super(executor);
        result(value);
    }

    public FinalMiFuture(Executor executor, Throwable error) {
        super(executor);
        fail(error);
    }
}
