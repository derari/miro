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
     * @param <Builder> 
     */
    interface CopyComposer<Builder> extends Composer, Key<InternalComposer<Builder>> {
    
        /**
         * Returns the internal composer to which the copy will be added.
         * Do not invoke modifying operations during copying.
         * @param original
         * @return internal composer
         */
        default InternalComposer<Builder> toInternal(InternalComposer<Builder> original) {
            return original.node(this);
        }
    }    
}
