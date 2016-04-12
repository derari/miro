package org.cthul.miro.composer;

import java.util.function.Function;

/**
 * Interface for {@link StatementPart}s and nodes that have state 
 * and should be copied for new composers.
 * @param <Builder>
 */
public interface Copyable<Builder> {

    /**
     * Creates a copy.
     * Non-node {@link StatementPart}s can return null if they are already 
     * added to the composer.
     * @param ic
     * @return the copy
     */
    Object copyFor(InternalComposer<Builder> ic);
    
    static <T> T tryCopy(T original, InternalComposer<?> ic) {
        if (original instanceof Copyable) {
            return (T) ((Copyable) original).copyFor(ic);
        }
        return original;
    }
    
    static <T> Function<T, T> copier(InternalComposer<?> ic) {
        return v -> tryCopy(v, ic);
    }
}
