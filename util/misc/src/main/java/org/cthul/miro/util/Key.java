package org.cthul.miro.util;

/**
 *
 * @param <Value>
 */
public interface Key<Value> {
    
    default Value cast(Object v) {
        return (Value) v;
    }
    
    static <T> T castDefault(Object o, T def) {
        return castDefault(o, (Class<T>) def.getClass(), def);
    }
    
    static <T> T castDefault(Object o, Class<T> clazz, T def) {
        if (clazz.isInstance(o)) {
            return clazz.cast(o);
        }
        return def;
    }
}
