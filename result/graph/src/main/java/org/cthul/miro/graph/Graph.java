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
    <Node> NodeSelector<Node> nodeSelector(Object typeKey, List<?> attributes) throws MiException;
    
    default <Node> NodeSelector<Node> nodeSelector(Class<Node> typeKey, List<?> attributes) throws MiException {
        return nodeSelector((Object) typeKey, attributes);
    }
    
    /**
     * Finds or creates nodes of the given type.
     * @param <Node>
     * @param typeKey
     * @return node selector
     * @throws MiException 
     */
    default <Node> NodeSelector<Node> nodeSelector(Object typeKey) throws MiException {
        return nodeSelector(typeKey, Collections.emptyList());
    }
    
    default <Node> NodeSelector<Node> nodeSelector(Class<Node> typeKey) throws MiException {
        return nodeSelector((Object) typeKey);
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
    default <Node> NodeSelector<Node> nodeSelector(Object typeKey, Object... attributes) throws MiException {
        return nodeSelector(typeKey, Arrays.asList(attributes));
    }
    
    default <Node> NodeSelector<Node> nodeSelector(Class<Node> typeKey, Object... attributes) throws MiException {
        return nodeSelector((Object) typeKey, attributes);
    }
    
    /**
     * Returns an entity type that will find or create nodes of the graph.
     * @param <Node>
     * @param typeKey
     * @return entity type
     * @throws MiException 
     */
    <Node> EntityType<Node> entityType(Object typeKey) throws MiException;
    
    default <Node> EntityType<Node> entityType(Class<Node> typeKey) throws MiException {
        return entityType((Object) typeKey);
    }
    
    /**
     * Returns an entity type that will find or create nodes in the graph and
     * ensures that the given attributes are initialized.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity type
     * @throws MiException 
     */
    default <Node> EntityType<Node> entityType(Object typeKey, List<?> attributes) throws MiException {
        return this.<Node>entityType(typeKey).with(rs -> attributeInitializer(typeKey, attributes));
    }
    
    default <Node> EntityType<Node> entityType(Class<Node> typeKey, List<?> attributes) throws MiException {
        return entityType((Object) typeKey, attributes);
    }
    
    /**
     * Returns an entity type that will find or create nodes in the graph and
     * ensure that the given attributes are initialized.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity type
     * @throws MiException 
     */
    default <Node> EntityType<Node> entityType(Object typeKey, Object... attributes) throws MiException {
        return entityType(typeKey, Arrays.asList(attributes));
    }
    
    default <Node> EntityType<Node> entityType(Class<Node> typeKey, Object... attributes) throws MiException {
        return entityType((Object) typeKey, attributes);
    }
    
    /**
     * Returns an entity initializer that will fill the given attributes.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity initializer
     * @throws MiException 
     */
    <Node> EntityInitializer<Node> attributeInitializer(Object typeKey, List<?> attributes) throws MiException;
    
    default <Node> EntityInitializer<Node> attributeInitializer(Class<Node> typeKey, List<?> attributes) throws MiException {
        return attributeInitializer((Object) typeKey, attributes);
    }
    
    /**
     * Returns an entity initializer that will fill the given attributes.
     * @param <Node>
     * @param typeKey
     * @param attributes
     * @return entity initializer
     * @throws MiException 
     */
    default <Node> EntityInitializer<Node> attributeInitializer(Object typeKey, Object... attributes) throws MiException {
        return attributeInitializer(typeKey, Arrays.asList(attributes));
    }
    
    default <Node> EntityInitializer<Node> attributeInitializer(Class<Node> typeKey, Object... attributes) throws MiException {
        return attributeInitializer((Object) typeKey, attributes);
    }
    
    @Override
    void close() throws MiException;
}
