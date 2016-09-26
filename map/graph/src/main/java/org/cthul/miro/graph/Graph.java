package org.cthul.miro.graph;

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
    
    <Node> GraphNodes<Node> getNodeSet(Object typeKey);
    
    default <Node> GraphNodes<Node> getNodeSet(Class<Node> typeKey) {
        return getNodeSet((Object) typeKey);
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
    default <Node> NodeSelector<Node> newNodeSelector(Object typeKey, List<?> attributes) throws MiException {
        return this.<Node>getNodeSet(typeKey).newNodeSelector(attributes);
    }
    
    default <Node> NodeSelector<Node> newNodeSelector(Class<Node> typeKey, List<?> attributes) throws MiException {
        return this.<Node>getNodeSet(typeKey).newNodeSelector(attributes);
    }
    
    /**
     * Finds or creates nodes of the given type.
     * @param <Node>
     * @param typeKey
     * @return node selector
     * @throws MiException 
     */
    default <Node> NodeSelector<Node> newNodeSelector(Object typeKey) throws MiException {
        return this.<Node>getNodeSet(typeKey).newNodeSelector();
    }
    
    default <Node> NodeSelector<Node> newNodeSelector(Class<Node> typeKey) throws MiException {
        return this.<Node>getNodeSet(typeKey).newNodeSelector();
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
        return this.<Node>getNodeSet(typeKey).newNodeSelector(attributes);
    }
    
    default <Node> NodeSelector<Node> newNodeSelector(Class<Node> typeKey, Object... attributes) throws MiException {
        return this.<Node>getNodeSet(typeKey).newNodeSelector(attributes);
    }
    
    /**
     * Returns an entity type that will find or create nodes of the graph.
     * @param <Node>
     * @param typeKey
     * @return entity type
     */
    default <Node> EntityType<Node> getEntityType(Object typeKey) {
        return this.<Node>getNodeSet(typeKey).getEntityType();
    }
    
    default <Node> EntityType<Node> getEntityType(Class<Node> typeKey) {
        return this.<Node>getNodeSet(typeKey).getEntityType();
    }
    
    /**
     * Returns an entity type that will find or create nodes in the graph and
     * read the given attributes from the result set.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity type
     */
    default <Node> EntityType<Node> getEntityType(Object typeKey, List<?> attributes) {
        return this.<Node>getNodeSet(typeKey).getEntityType(attributes);
    }
    
    default <Node> EntityType<Node> getEntityType(Class<Node> typeKey, List<?> attributes) {
        return this.<Node>getNodeSet(typeKey).getEntityType(attributes);
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
        return this.<Node>getNodeSet(typeKey).getEntityType(attributes);
    }
    
    default <Node> EntityType<Node> getEntityType(Class<Node> typeKey, Object... attributes) {
        return this.<Node>getNodeSet(typeKey).getEntityType(attributes);
    }
    
    /**
     * Returns an entity initializer that will load the given attributes from the database.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity initializer
     * @throws MiException 
     */
    default <Node> EntityInitializer<Node> newAttributeLoader(Object typeKey, List<?> attributes) throws MiException {
        return this.<Node>getNodeSet(typeKey).newAttributeLoader(attributes);
    }
    
    default <Node> EntityInitializer<Node> newAttributeLoader(Class<Node> typeKey, List<?> attributes) throws MiException {
        return this.<Node>getNodeSet(typeKey).newAttributeLoader(attributes);
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
        return this.<Node>getNodeSet(typeKey).newAttributeLoader(attributes);
    }
    
    default <Node> EntityInitializer<Node> newAttributeLoader(Class<Node> typeKey, Object... attributes) throws MiException {
        return this.<Node>getNodeSet(typeKey).newAttributeLoader(attributes);
    }
    
    @Override
    void close() throws MiException;
}
