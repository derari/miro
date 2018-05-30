package org.cthul.miro.composer.node;

/**
 *
 */
public interface Copyable<Composer> {
    
    Object copy(Composer composer);
    
    default boolean allowReadOriginal() {
        return false;
    }
}
