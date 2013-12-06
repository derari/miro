package org.cthul.miro.query.parts;

public interface QueryPart {
    
    Object getKey();
    
    void put(Object key, Object... args);
    
}
