package org.cthul.miro.request.part;

import java.util.List;
import java.util.stream.Collectors;
import org.cthul.miro.request.impl.ValueKey;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface CopyManager {
    
    static final Key<CopyManager> KEY = new ValueKey<>("CopyManager", true);

    <V> V tryCopy(V value);
    
    default <V> List<V> copyAll(List<V> values) {
        return values.stream()
                .map(this::tryCopy)
                .filter(v -> v != null)
                .collect(Collectors.toList());
    }
}
