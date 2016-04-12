package org.cthul.miro.futures;

/**
 *
 * @param <V>
 */
public interface MiResettableFuture<V> extends MiFuture<V> {
    
    MiResettableFuture<V> reset();
}
