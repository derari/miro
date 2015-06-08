package org.cthul.miro.graph;

import org.cthul.miro.graph.base.GraphImpl;
import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.db.MiConnection;

/**
 *
 */
public class SchemaBuilder implements Schema {
    
    private final Map<Object, NodeType<?>> types = new HashMap<>();

    public SchemaBuilder() {
    }
    
    public SchemaBuilder put(Object key, NodeType<?> type) {
        types.put(key, type);
        return this;
    }

    @Override
    public Graph newGraph(MiConnection connection) {
        return new GraphImpl(connection, types);
    }
}
