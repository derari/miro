package org.cthul.miro.query.api;

public interface QueryBuilder {

    void put(String key);
    
    void put(String key, Object... args);
    
    void put(String key, String subkey, Object... args);
}
