package org.cthul.miro.request.part;

import java.util.function.Predicate;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.template.InternalComposer;

/**
 * Interface for {@link StatementPart}s and nodes that have state 
 * and should be copied for new composers.
 * @param <Builder>
 */
public interface Copyable<Builder> {

    /**
     * Creates a copy.
     * Returns itself if stateless.
     * Non-node {@link StatementPart}s can return null if they are already 
     * added to the composer.
     * @param ic
     * @return the copy
     */
    Object copyFor(InternalComposer<Builder> ic);

    /**
     * Indicate whether this object can be used for read-only access.
     * If this part depends on other nodes or parts for read access,
     * they should be tested using the predicate
     * @param isLatest to test if dependencies are still valid
     * @return true iff this object allows read only
     */
    default boolean allowReadOnly(Predicate<Object> isLatest) {
        return false;
    }
    
    static <T> T tryCopy(T original, InternalComposer<?> ic) {
        if (original instanceof Copyable) {
            return (T) ((Copyable) original).copyFor(ic);
        }
        return original;
    }
    
    static boolean allowReadOnly(Object o, Predicate<Object> isLatest) {
        if (o instanceof Copyable) {
            return ((Copyable) o).allowReadOnly(isLatest);
        }
        return true;
    }
}
