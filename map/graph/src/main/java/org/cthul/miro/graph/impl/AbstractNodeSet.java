package org.cthul.miro.graph.impl;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.entity.InitializationBuilder;
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
public abstract class AbstractNodeSet<Node> implements GraphNodes<Node> {

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
    
    /**
     * Finds or creates nodes in the set and
     * ensures that the specified attributes are initialized.
     * @throws org.cthul.miro.db.MiException
     * @see Graph#newNodeSelector(Object, List)
     */
    @Override
    public abstract void newNodeSelector(SelectorBuilder<? super Node> builder) throws MiException;
    
    /**
     * Returns an entity type that will find or create nodes in the graph and
     * read the given attributes from the result set.
     * @param attributes
     * @return entity type
     * @see Graph#getEntityType(Object, List)
     */
    @Override
    public abstract EntityType<Node> getEntityType(List<?> attributes);
    
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

    @Override
    public void newAttributeLoader(List<?> attributes, InitializationBuilder<? extends Node> builder) throws MiException {
        getNodeType().newAttributeLoader(getGraph(), attributes, builder);
    }
    
}
