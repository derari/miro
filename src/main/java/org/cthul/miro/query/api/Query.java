package org.cthul.miro.query.api;

public interface Query {

    void put(String key);
    
    void put(String key, Object... args);
    
    void put2(String key, String subkey, Object... args);
}
