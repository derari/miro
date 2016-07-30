package org.cthul.miro.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;

/**
 * Represents all nodes of a type in a graph.
 * @param <Node>
 */
public interface NodeSet<Node> {
    
    Graph getGraph();
    
    /**
     * Finds or creates nodes of the given type and ensures that the specified
     * attributes are initialized.
     * @param attributes
     * @return node selector
     * @throws MiException 
     */
    default NodeSelector<Node> newNodeSelector(List<?> attributes) throws MiException {
        return newNodeSelector().with(newAttributeLoader(attributes));
    }
    
    /**
     * Finds or creates nodes of the given type.
     * @return node selector
     * @throws MiException 
     */
    NodeSelector<Node> newNodeSelector() throws MiException;
    
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
    EntityInitializer<Node> newAttributeLoader(List<?> attributes) throws MiException;
    
    /**
     * Returns an entity initializer that will load the given attributes from the database.
     * @param attributes
     * @return entity initializer
     * @throws MiException 
     */
    default EntityInitializer<Node> newAttributeLoader(Object... attributes) throws MiException {
        return newAttributeLoader(Arrays.asList(attributes));
    }
}
