package org.cthul.miro.composer.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface CopyManager {
    
    static final Key<CopyManager> key = new ValueKey<>("CopyManager", true);

    <V> V tryCopy(V value);
    
    default <V> List<V> copyAll(List<V> values) {
        return values.stream()
                .map(this::tryCopy)
                .collect(Collectors.toList());
    }
}
