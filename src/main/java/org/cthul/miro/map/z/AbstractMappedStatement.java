package org.cthul.miro.map.z;

import org.cthul.miro.MiConnection;

/**
 *
 */
public abstract class AbstractMappedStatement<Entity> extends MappedStatement<Entity> {

    public AbstractMappedStatement(MiConnection cnn, SimpleMapping<Entity> mapping) {
        super(cnn, mapping);
    }

    @Override
    public void put(String key) {
        put(key, (Object[]) null);
    }

    @Override
    public void put(String key, Object... args) {
        String subKey;
        int dot = key.indexOf('.');
        if (dot < 0) {
            subKey = null;
        } else {
            subKey = key.substring(dot+1);
            key = key.substring(0, dot);
        }
        put2(key, subKey, args);
    }
}
