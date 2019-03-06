package org.cthul.miro.composer;

/**
 *
 */
public interface Extensible {
    
    Extensible extend();
    
    static <T> T extend(T composer) {
        if (composer instanceof Extensible) {
            return (T) ((Extensible) composer).extend();
        }
        return composer;
    }
}
