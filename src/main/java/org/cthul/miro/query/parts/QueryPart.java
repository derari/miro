package org.cthul.miro.query.parts;

public interface QueryPart {
    
    String getKey();
    
    void put(String key, Object... args);
    
}
