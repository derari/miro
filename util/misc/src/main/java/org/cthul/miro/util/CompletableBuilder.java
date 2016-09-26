package org.cthul.miro.util;

/**
 *
 */
public interface CompletableBuilder {
    
    CompletableBuilder addCompletable(Completable completable);
    
    CompletableBuilder addCloseable(AutoCloseable closeable);
    
    CompletableBuilder addName(Object name);
    
    default <C extends Completable & AutoCloseable> CompletableBuilder addCompleteAndClose(C completeAndCloseable) {
        return addCompletable(completeAndCloseable).addCloseable(completeAndCloseable);
    }
}
