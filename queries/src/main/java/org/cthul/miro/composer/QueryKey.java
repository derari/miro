package org.cthul.miro.composer;

import org.cthul.miro.util.Key;

/**
 *
 */
public enum QueryKey {
        
    PHASE,
    
    NIL;
    
    public static QueryKey key(Object o) {
        return Key.castDefault(o, QueryKey.class, NIL);
    }
    
    public static enum Phase {
        COMPOSE,
        BUILD;
    }
}
