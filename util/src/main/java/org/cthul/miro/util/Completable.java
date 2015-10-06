package org.cthul.miro.util;

/**
 * An object that may delay an action until it is told to complete.
 * <p>When a Completable also implements the Closable interface, closing it
 * should also complete it.
 */
public interface Completable {
    
    void complete() throws Exception;
}
