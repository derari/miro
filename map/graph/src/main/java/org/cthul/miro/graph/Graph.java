package org.cthul.miro.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;

/**
 * A graph of identifiable nodes.
 * Each node is identified by its type and a key.
 * New nodes can be created.
 */
public interface Graph extends AutoCloseable {
    
    /**
     * Finds or creates nodes of the given type and ensures that the specified
     * attributes are initialized.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return node selector
     * @throws MiException 
     */
    default <Node> NodeSelector<Node> newNodeSelector(Object typeKey, List<?> attributes) throws MiException {
        return this.<Node>newNodeSelector(typeKey).with(newAttributeLoader(typeKey, attributes));
    }
    
    default <Node> NodeSelector<Node> newNodeSelector(Class<Node> typeKey, List<?> attributes) throws MiException {
        return newNodeSelector((Object) typeKey, attributes);
    }
    
    /**
     * Finds or creates nodes of the given type.
     * @param <Node>
     * @param typeKey
     * @return node selector
     * @throws MiException 
     */
    <Node> NodeSelector<Node> newNodeSelector(Object typeKey) throws MiException;
    
    default <Node> NodeSelector<Node> newNodeSelector(Class<Node> typeKey) throws MiException {
        return newNodeSelector((Object) typeKey);
    }
    
    /**
     * Finds or creates nodes of the given type and ensures that the specified
     * attributes are initialized.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return node selector
     * @throws MiException 
     */
    default <Node> NodeSelector<Node> newNodeSelector(Object typeKey, Object... attributes) throws MiException {
        return newNodeSelector(typeKey, Arrays.asList(attributes));
    }
    
    default <Node> NodeSelector<Node> newNodeSelector(Class<Node> typeKey, Object... attributes) throws MiException {
        return newNodeSelector((Object) typeKey, attributes);
    }
    
    /**
     * Returns an entity type that will find or create nodes of the graph.
     * @param <Node>
     * @param typeKey
     * @return entity type
     */
    default <Node> EntityType<Node> getEntityType(Object typeKey) {
        return getEntityType(typeKey, Collections.emptyList());
    }
    
    default <Node> EntityType<Node> getEntityType(Class<Node> typeKey) {
        return getEntityType((Object) typeKey);
    }
    
    /**
     * Returns an entity type that will find or create nodes in the graph and
     * read the given attributes from the result set.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity type
     */
    <Node> EntityType<Node> getEntityType(Object typeKey, List<?> attributes);
    
    default <Node> EntityType<Node> getEntityType(Class<Node> typeKey, List<?> attributes) {
        return getEntityType((Object) typeKey, attributes);
    }
    
    /**
     * Returns an entity type that will find or create nodes in the graph and
     * read the given attributes from the result set.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity type
     */
    default <Node> EntityType<Node> getEntityType(Object typeKey, Object... attributes) {
        return getEntityType(typeKey, Arrays.asList(attributes));
    }
    
    default <Node> EntityType<Node> getEntityType(Class<Node> typeKey, Object... attributes) {
        return getEntityType((Object) typeKey, attributes);
    }
    
    /**
     * Returns an entity initializer that will load the given attributes from the database.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity initializer
     * @throws MiException 
     */
    <Node> EntityInitializer<Node> newAttributeLoader(Object typeKey, List<?> attributes) throws MiException;
    
    default <Node> EntityInitializer<Node> newAttributeLoader(Class<Node> typeKey, List<?> attributes) throws MiException {
        return newAttributeLoader((Object) typeKey, attributes);
    }
    
    /**
     * Returns an entity initializer that will load the given attributes from the database.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity initializer
     * @throws MiException 
     */
    default <Node> EntityInitializer<Node> newAttributeLoader(Object typeKey, Object... attributes) throws MiException {
        return newAttributeLoader(typeKey, Arrays.asList(attributes));
    }
    
    default <Node> EntityInitializer<Node> newAttributeLoader(Class<Node> typeKey, Object... attributes) throws MiException {
        return newAttributeLoader((Object) typeKey, attributes);
    }
    
    @Override
    void close() throws MiException;
}
