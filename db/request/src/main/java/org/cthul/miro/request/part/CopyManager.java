package org.cthul.miro.request.part;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.cthul.miro.request.impl.ValueKey;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface CopyManager {
    
    static final Key<CopyManager> KEY = new ValueKey<>("CopyManager", true);

    <V> V tryCopy(V value);
    
    default <V> List<V> copyAll(Collection<V> values) {
        return copyAll(values, new ArrayList<>(values.size()));
    }
    
    default <V, C extends Collection<? super V>> C copyAll(Collection<V> values, C bag) {
        this.<V>copyAll(values, bag::add);
        return bag;
    }
    
    default <V> void copyAll(Collection<V> values, Consumer<V> bag) {
        values.stream()
                .map(this::tryCopy)
                .filter(v -> v != null)
                .forEach(bag);
    }
}
