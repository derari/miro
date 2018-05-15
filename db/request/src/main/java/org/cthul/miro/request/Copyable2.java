package org.cthul.miro.request;

/**
 *
 */
public interface Copyable2<Composer> {
    
    Object copy(Composer composer);
    
    default boolean allowRead() {
        return false;
    }
}
