package org.cthul.miro.composer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.cthul.miro.util.Key;

/**
 *
 */
public class ConfigureKey implements Key<ConfigureKey.Configurable> {
    
    private final Object value;

    public ConfigureKey(Object value) {
        this.value = value;
    }

    protected Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return 928365 ^ Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConfigureKey other = (ConfigureKey) obj;
        return Objects.equals(getValue(), other.getValue());
    }

    @Override
    public String toString() {
        return "<" + getValue() + ">";
    }
    
    public static ConfigureKey key(Object value) {
        return new ConfigureKey(value);
    }
    
    private static final AtomicLong counter = new AtomicLong(0);
    
    protected static String uniqueName(Object name) {
        return name + "@" + Long.toString(counter.getAndIncrement(), Character.MAX_RADIX);
    }
    
    public static ConfigureKey unique(Object name) {
        return key(uniqueName(name));
    }
    
    public static interface Configurable {
        
        void set(Object... values);
    }
}
