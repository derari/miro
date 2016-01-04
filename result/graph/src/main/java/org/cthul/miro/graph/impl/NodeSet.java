package org.cthul.miro.graph.impl;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeType;

/**
 * A set of nodes of one type in a graph.
 * Also implements the {@link NodeType} interface, except that it doesn't
 * need the graph parameter.
 * @param <Node>
 */
public abstract class NodeSet<Node> implements EntityType<Node> {

    private final NodeType<Node> nodeType;
    private final GraphApi graph;

    public NodeSet(NodeType<Node> nodeType, GraphApi graph) {
        this.graph = graph;
        this.nodeType = nodeType;
    }

    protected NodeType<Node> getNodeType() {
        return nodeType;
    }

    protected GraphApi getGraph() {
        return graph;
    }

    @Override
    public String toString() {
        return shortString(getNodeType());
    }
    
    protected String shortString(Object s) {
        return "graph[" + String.valueOf(s) + "]";
    }
    
    protected abstract Node getNode(Object... key);
    
    protected abstract void putNode(Object[] key, Node e);

    /**
     * Finds or creates nodes of this type.
     * @return node selector
     * @throws MiException 
     */
    public NodeSelector<Node> newNodeSelector() throws MiException {
        return new NodeSelector<Node>() {
            NodeSelector<Node> factory = null;

            NodeSelector<Node> factory() throws MiException {
                if (factory == null) {
                    factory = getNodeType().newNodeFactory(getGraph());
                }
                return factory;
            }

            @Override
            public Node get(Object... key) throws MiException {
                Node e = getNode(key);
                if (e == null) {
                    e = factory().get(key);
                    putNode(key, e);
                }
                return e;
            }

            @Override
            public void complete() throws MiException {
                if (factory != null) {
                    factory.complete();
                }
            }

            @Override
            public void close() throws MiException {
                if (factory != null) {
                    factory.close();
                }
            }

            @Override
            public String toString() {
                String s = factory != null ? factory.toString() : 
                        ("new " + getNodeType());
                return shortString(s);
            }
        };
    }

    /**
     * Finds or creates nodes of the given type and ensures that the specified
     * attributes are initialized.
     * @param attributes
     * @return node selector
     * @throws MiException 
     */
    public NodeSelector<Node> newSelector(List<?> attributes) throws MiException {
        NodeSelector<Node> selector = newNodeSelector();
        EntityInitializer<Node> init = newAttributeInitializer(attributes);
        return new NodeSelector<Node>() {
            @Override
            public Node get(Object... key) throws MiException {
                Node e = selector.get(key);
                init.apply(e);
                return e;
            }
            @Override
            public void complete() throws MiException {
                init.complete();
            }
            @Override
            public void close() throws MiException {
                init.close();
            }
            @Override
            public String toString() {
                return shortString(init);
            }
        };
    }

    /**
     * Returns an entity factory that will find or create nodes of the graph.
     * @param rs
     * @return entity factory
     * @throws MiException 
     */
    @Override
    public EntityFactory<Node> newFactory(MiResultSet rs) throws MiException {
        return newFactory(newNodeSelector(), rs);
    }

    /**
     * Creates an entity factory that looks up keys in the {@code resultSet} and 
     * obtains nodes from the {@code nodeFactory}.
     * @param nodeFactory
     * @param rs
     * @return
     * @throws MiException 
     */
    public EntityFactory<Node> newFactory(NodeSelector<Node> nodeFactory, MiResultSet rs) throws MiException {
        return getNodeType().newEntityFactory(getGraph(), nodeFactory, rs);
    }

    @Override
    public Node[] newArray(int length) {
        return getNodeType().newArray(length);
    }

    public EntityInitializer<Node> newAttributeInitializer(List<?> attributes) throws MiException {
        return getNodeType().newAttributeInitializer(getGraph(), attributes);
    }
}
