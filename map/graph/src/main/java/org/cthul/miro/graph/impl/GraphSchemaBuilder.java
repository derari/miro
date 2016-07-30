package org.cthul.miro.graph.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.graph.*;

/**
 * Allows to configure node types for a {@link GraphSchema}.
 */
public class GraphSchemaBuilder implements GraphSchema, Graph {
    
    private final Map<Object, NodeType<?>> types = new HashMap<>();
    private final Function<Object, NodeType<?>> lookUp = this::nodeType;
    private Graph nullGraph = null;

    public GraphSchemaBuilder() {
    }
    
    public GraphSchemaBuilder put(Object key, NodeType<?> type) {
        types.put(key, type);
        return this;
    }
    
    public <N> NodeType<N> nodeType(Object key) {
        return (NodeType<N>) types.get(key);
    }

    @Override
    public Graph newGraph(MiConnection connection) {
        return new SimpleGraph(connection, lookUp);
    }

    @Override
    public Graph newFakeGraph(MiConnection connection) {
        if (connection == null) {
            return asGraph();
        }
        return new FakeGraph(connection, lookUp);
    }
    
    protected Graph nullGraph() {
        if (nullGraph == null) {
            nullGraph = new FakeGraph(null, lookUp);
        }
        return nullGraph;
    }
    
    public Graph asGraph() {
        return this;
    }

    @Override
    public <Node> NodeSet<Node> getNodeSet(Object typeKey) {
        return nullGraph().getNodeSet(typeKey);
    }

    @Override
    public void close() throws MiException {
    }
}
