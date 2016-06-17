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
    public <Node> NodeSelector<Node> newNodeSelector(Object typeKey) throws MiException {
        return nullGraph().newNodeSelector(typeKey);
    }

    @Override
    public <Node> EntityType<Node> getEntityType(Object typeKey, List<?> attributes) {
        NodeType<Node> n = nodeType(typeKey);
        if (attributes.isEmpty() && n instanceof EntityType) {
            return (EntityType<Node>) n;
        }
        return nullGraph().getEntityType(typeKey, attributes);
    }

    @Override
    public <Node> EntityInitializer<Node> newAttributeLoader(Object typeKey, List<?> attributes) throws MiException {
        return nullGraph().newAttributeLoader(typeKey, attributes);
    }

    @Override
    public void close() throws MiException {
    }
}
