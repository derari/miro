package org.cthul.miro.futures;

/**
 *
 */
public interface MiResult<V> {
    
    void setResult(V value);
    
    void setFail(Throwable throwable);
    
    boolean continueWork();
}
