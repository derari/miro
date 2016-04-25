package org.cthul.miro.request.impl;

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
