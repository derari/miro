package org.cthul.miro.graph.impl;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.graph.*;

/**
 * A set of nodes of one type in a graph.
 * <p>
 * Implements the non-default methods of {@link Graph}, 
 * but without the {@code typeKey} parameter.
 * <p>
 * As entity type, its factories return nodes of the set.
 * @param <Node>
 */
public abstract class AbstractNodeSet<Node> implements NodeSet<Node> {

    private final NodeType<Node> nodeType;
    private final GraphApi graph;

    public AbstractNodeSet(NodeType<Node> nodeType, GraphApi graph) {
        this.graph = graph;
        this.nodeType = nodeType;
    }

    protected NodeType<Node> getNodeType() {
        return nodeType;
    }

    @Override
    public GraphApi getGraph() {
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
     * Finds or creates nodes in the set and
     * ensures that the specified attributes are initialized.
     * @return node selector
     * @see Graph#newNodeSelector(Object, List)
     */
    @Override
    public NodeSelector<Node> newNodeSelector() {
        return new NodesOfSet();
    }
    
    /**
     * Returns an entity type that will find or create nodes in the graph and
     * read the given attributes from the result set.
     * @param attributes
     * @return entity type
     * @see Graph#getEntityType(Object, List)
     */
    @Override
    public EntityType<Node> getEntityType(List<?> attributes) {
        return getNodeType().asEntityType(getGraph(), newNodeSelector(), attributes);
    }
    
    /**
     * Creates an initializer that will load the given attributes from the database.
     * @param attributes
     * @return initializer
     * @throws MiException
     * @see Graph#newAttributeLoader(Object, List)
     */
    @Override
    public EntityInitializer<Node> newAttributeLoader(List<?> attributes) throws MiException {
        if (attributes.isEmpty()) return EntityTypes.noInitialization();
        return getNodeType().newAttributeLoader(getGraph(), attributes);
    }

    private class NodesOfSet implements NodeSelector<Node> {
        private NodeSelector<Node> factory = null;

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
    }
}
