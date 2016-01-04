package org.cthul.miro.graph.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.util.Closables;

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
    public <Node> NodeSelector<Node> nodeSelector(Object typeKey, List<?> attributes) throws MiException {
        return nullGraph().nodeSelector(typeKey, attributes);
    }

    @Override
    public <Node> EntityType<Node> entityType(Object typeKey) throws MiException {
        NodeType<Node> n = nodeType(typeKey);
        if (n instanceof EntityType) {
            return (EntityType<Node>) n;
        }
        try {
            return nullGraph().entityType(typeKey);
        } catch (MiException e) {
            throw Closables.unchecked(e);
        }
    }

    @Override
    public <Node> EntityInitializer<Node> attributeInitializer(Object typeKey, List<?> attributes) throws MiException {
        return nullGraph().attributeInitializer(typeKey, attributes);
    }

    @Override
    public void close() throws MiException {
    }
}
