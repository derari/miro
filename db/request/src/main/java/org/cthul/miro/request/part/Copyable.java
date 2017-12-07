package org.cthul.miro.request.part;

import java.util.function.Predicate;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.util.Key;

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
     * @param cc
     * @return the copy
     */
    Object copyFor(CopyComposer<Builder> cc);

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
    
    /**
     * Provides access to the copied composer.
     * To get internal access, resolve as node from the previous internal composer.
     * @param <Builder> 
     */
    interface CopyComposer<Builder> extends Composer, Key<InternalComposer<Builder>> { }
    
    static <T> T tryCopy(T original, CopyComposer<?> cc) {
        if (original instanceof Copyable) {
            return (T) ((Copyable) original).copyFor(cc);
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
