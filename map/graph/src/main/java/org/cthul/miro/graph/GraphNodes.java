package org.cthul.miro.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.entity.InitializationBuilder;
import org.cthul.miro.graph.impl.CompositeSelector;

/**
 * Represents all nodes of a type in a graph.
 * @param <Node>
 */
public interface GraphNodes<Node> extends NodeSet<Node> {
    
    Graph getGraph();
    
    /**
     * Finds or creates nodes of the given type.
     * @return node selector
     * @throws MiException 
     */
    @Override
    default NodeSelector<Node> newNodeSelector() throws MiException {
        return NodeSet.super.newNodeSelector();
    }

    /**
     * Finds or creates nodes of the given type.
     * @param builder
     * @throws MiException 
     */
    @Override
    void newNodeSelector(SelectorBuilder<? super Node> builder) throws MiException;
    
    default NodeSet<Node> getInitializedSet(List<?> attributes) {
        return new NodeSet<Node>() {
            @Override
            public void newNodeSelector(SelectorBuilder<? super Node> builder) throws MiException {
                GraphNodes.this.newNodeSelector(attributes, builder);
            }
            @Override
            public String toString() {
                return GraphNodes.this.toString() + " with " +
                        attributes.stream().map(String::valueOf).collect(Collectors.joining(", "));
            }
        };
    }
    
    default NodeSet<Node> getInitializedSet(Object... attributes) {
        return getInitializedSet(Arrays.asList(attributes));
    }
    
    /**
     * Finds or creates nodes of the given type and ensures that the specified
     * attributes are initialized.
     * @param attributes
     * @return node selector
     * @throws MiException 
     */
    default NodeSelector<Node> newNodeSelector(List<?> attributes) throws MiException {
        return CompositeSelector.buildSelector(b -> {
            newNodeSelector(b);
            newAttributeLoader(attributes, b);
        });
    }
    
    /**
     * Finds or creates nodes of the given type and ensures that the specified
     * attributes are initialized.
     * @param attributes
     * @return node selector
     * @throws MiException 
     */
    default NodeSelector<Node> newNodeSelector(Object... attributes) throws MiException {
        return newNodeSelector(Arrays.asList(attributes));
    }
    
    /**
     * Finds or creates nodes of the given type and ensures that the specified
     * attributes are initialized.
     * @param attributes
     * @param builder
     * @throws MiException 
     */
    default void newNodeSelector(List<?> attributes, SelectorBuilder<? super Node> builder) throws MiException {
        newNodeSelector(builder);
        newAttributeLoader(attributes, (SelectorBuilder<Node>) builder);
    }
    
    /**
     * Returns an entity type that will find or create nodes of the graph.
     * @return entity type
     */
    default EntityType<Node> getEntityType() {
        return getEntityType(Collections.emptyList());
    }
    
    /**
     * Returns an entity type that will find or create nodes in the graph and
     * read the given attributes from the result set.
     * @param attributes
     * @return entity type
     */
    EntityType<Node> getEntityType(List<?> attributes);
    
    /**
     * Returns an entity type that will find or create nodes in the graph and
     * read the given attributes from the result set.
     * @param attributes
     * @return entity type
     */
    default EntityType<Node> getEntityType(Object... attributes) {
        return getEntityType(Arrays.asList(attributes));
    }
    
    /**
     * Returns an entity initializer that will load the given attributes from the database.
     * @param attributes
     * @return entity initializer
     * @throws MiException 
     */
    default EntityInitializer<Node> newAttributeLoader(List<?> attributes) throws MiException {
        return EntityTypes.buildInitializer(b -> newAttributeLoader(attributes, b));
    }
    
    /**
     * Returns an entity initializer that will load the given attributes from the database.
     * @param attributes
     * @return entity initializer
     * @throws MiException 
     */
    default EntityInitializer<Node> newAttributeLoader(Object... attributes) throws MiException {
        return newAttributeLoader(Arrays.asList(attributes));
    }
    
    /**
     * Returns an entity initializer that will load the given attributes from the database.
     * @param attributes
     * @param builder
     * @throws MiException 
     */
    void newAttributeLoader(List<?> attributes, InitializationBuilder<? extends Node> builder) throws MiException;
}
