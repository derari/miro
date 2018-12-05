package org.cthul.miro.composer;

/**
 *
 */
public interface NoOverride {
    
    NoOverride noOverride();
    
    static <T> T noOverride(T composer) {
        if (composer instanceof NoOverride) {
            return (T) ((NoOverride) composer).noOverride();
        }
        return composer;
    }
}
