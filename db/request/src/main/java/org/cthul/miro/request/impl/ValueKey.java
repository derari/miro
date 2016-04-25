package org.cthul.miro.request.impl;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.cthul.miro.util.Key;

/**
 *
 * @param <V>
 */
public class ValueKey<V> implements Key<V> {
    
    private final Object value;

    public ValueKey(Object value) {
        this.value = value;
    }
    
    public ValueKey(Object name, boolean unique) {
        this.value = unique ? uniqueName(name) : name;
    }
    
    protected Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ Objects.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValueKey other = (ValueKey) obj;
        return Objects.equals(getValue(), other.getValue());
    }

    @Override
    public String toString() {
        return "<" + getValue() + ">";
    }
    
    private static final AtomicLong COUNTER = new AtomicLong(0);
    
    protected static String uniqueName(Object name) {
        return name + "@" + Long.toString(COUNTER.getAndIncrement(), Character.MAX_RADIX);
    }
}
