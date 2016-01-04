package org.cthul.miro.composer;

/**
 *
 */
public enum ResultScope {
    
    ALWAYS,
    DEFAULT,
    OPTIONAL,
    WEAK_OPTIONAL,
    INTERNAL;
    
    public boolean isInternal() {
        return this == INTERNAL;
    }
    
}
