package org.cthul.miro.futures;

/**
 * A future that can be triggered or submitted.
 */
public interface MiAction<V> extends MiFuture<V> {
    
    /**
     * Any operation on the trigger not related do cancelling will cause 
     * the action to {@linkplain #submit() submit}.
     * @return the trigger
     */
    MiFuture<V> getTrigger();
    
    /**
     * Submits this action to be executed.
     * @return this
     */
    MiFuture<V> submit();
}
