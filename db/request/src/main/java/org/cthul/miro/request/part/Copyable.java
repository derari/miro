package org.cthul.miro.request.part;

import java.util.function.Predicate;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.util.Key;

/**
 * Interface for {@link StatementPart}s and nodes that have state 
 * and should be copied for new composers.
 */
public interface Copyable {

    /**
     * Creates a copy.
     * Returns itself if stateless.
     * Non-node {@link StatementPart}s can return null if they are already 
     * added to the composer.
     * @param cc
     * @return the copy
     */
    Object copyFor(CopyComposer cc);

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
     */
    interface CopyComposer extends Composer, Key<InternalComposer<?>> {
    
        /**
         * Returns the internal composer to which the copy will be added.
         * Do not invoke modifying operations during copying.
         * @param <Builder>
         * @param original
         * @return internal composer
         */
        default <Builder> InternalComposer<Builder> getInternal(InternalComposer<Builder> original) {
            return (InternalComposer) original.node(this);
        }
        
        default CopyManager getCopyManager() {
            return node(CopyManager.KEY);
        }
    }    
}
