package org.cthul.miro.futures;

/**
 *
 * @param <V>
 */
public interface MiResettableAction<V> extends MiAction<V>, MiResettableFuture<V> {

    @Override
    MiResettableFuture<V> getTrigger();

//    @Override 
//    when this future is reset, it will be impossible to re-trigger,
//    so don't make it resettable    
//    MiResettableFuture<V> submit();
}
