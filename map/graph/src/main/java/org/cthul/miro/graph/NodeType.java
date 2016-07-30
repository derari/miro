package org.cthul.miro.graph;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;

/**
 * Creates entity factories and node selectors.
 * @param <Node>
 */
public interface NodeType<Node> {
    
    /**
     * Creates a node selector that always creates new nodes.
     * @param graph used only to obtain nodes of other types
     * @return node factory
     * @throws MiException 
     */
    NodeSelector<Node> newNodeFactory(GraphApi graph) throws MiException;
  
    /**
     * Creates an initializer that will load the given attributes from the database.
     * @param graph used to obtain nodes of other types and to connect to the database
     * @param attributes the attributes to load
     * @return attribute loader
     * @throws MiException 
     */
    EntityInitializer<Node> newAttributeLoader(GraphApi graph, List<?> attributes) throws MiException;
    
    /**
     * Returns a configuration that will fetch the given attributes from a result set.
     * @param graph
     * @param attributes
     * @return attribute reader
     */
    EntityConfiguration<Node> getAttributeReader(GraphApi graph, List<?> attributes);
    
    /**
     * Creates an entity type that uses key attributes of the {@code resultSet} 
     * to obtains nodes from the {@code nodeSelector}.
     * @param graph used only to obtain nodes of other types
     * @param nodeSelector
     * @return entity type
     */
    EntityType<Node> asEntityType(GraphApi graph, NodeSelector<Node> nodeSelector);
    
    /**
     * Creates an entity type that uses key attributes of the {@code resultSet} 
     * to obtains nodes from the {@code nodeSelector} and initializes the
     * given attributes from the result set.
     * @param graph used only to obtain nodes of other types
     * @param nodeSelector
     * @param attributes
     * @return entity type
     */
    default EntityType<Node> asEntityType(GraphApi graph, NodeSelector<Node> nodeSelector, List<?> attributes) {
        EntityType<Node> type = asEntityType(graph, nodeSelector);
        if (attributes.isEmpty()) return type;
        return type.with(getAttributeReader(graph, attributes));
    }
}
