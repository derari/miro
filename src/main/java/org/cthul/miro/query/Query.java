package org.cthul.miro.query;

public interface Query {

    void put(Object key);
    
    void put(Object key, Object... args);
    
    void put2(Object key, Object subkey, Object... args);
}
