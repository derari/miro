package org.cthul.miro.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;

/**
 *
 */
public interface Graph extends AutoCloseable {
    
    <Node> NodeSelector<Node> nodeSelector(Object typeKey, List<String> attributes) throws MiException;
    
    default <Node> NodeSelector<Node> nodeSelector(Object typeKey) throws MiException {
        return nodeSelector(typeKey, Collections.emptyList());
    }
    
    default <Node> NodeSelector<Node> nodeSelector(Object typeKey, String... attributes) throws MiException {
        return nodeSelector(typeKey, Arrays.asList(attributes));
    }
    
    <Node> EntityType<Node> entityType(Object typeKey) throws MiException;
    
    <Node> EntityType<Node> entityType(Object typeKey, List<String> attributes) throws MiException;
    
    default <Node> EntityType<Node> entityType(Object typeKey, String... attributes) throws MiException {
        return this.<Node>entityType(typeKey).with(this.entityConfiguration(typeKey, attributes));
    }
    
    <Node> EntityConfiguration<Node> entityConfiguration(Object typeKey, List<String> attributes) throws MiException;
    
    default <Node> EntityConfiguration<Node> entityConfiguration(Object typeKey, String... attributes) throws MiException {
        return entityConfiguration(typeKey, Arrays.asList(attributes));
    }
    
    @Override
    void close() throws MiException;
}
