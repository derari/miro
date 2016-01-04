package org.cthul.miro.graph;

import java.util.List;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;

/**
 * Creates entity factories and node selectors.
 * @param <Node>
 */
public interface NodeType<Node> {
    
    /**
     * Creates an empty array of nodes.
     * @param length
     * @return array
     */
    Node[] newArray(int length);
    
    /**
     * Creates a node selector that always creates new nodes.
     * @param graph used only to obtain nodes of other types
     * @return node factory
     * @throws MiException 
     */
    NodeSelector<Node> newNodeFactory(GraphApi graph) throws MiException;
  
    EntityInitializer<Node> newAttributeInitializer(GraphApi graph, List<?> attributes) throws MiException;
    
    /**
     * Creates an entity factory that looks up keys in the {@code resultSet} and 
     * obtains nodes from the {@code nodeFactory}.
     * @param graph used only to obtain nodes of other types
     * @param nodeFactory
     * @param resultSet
     * @return
     * @throws MiException 
     */
    EntityFactory<Node> newEntityFactory(GraphApi graph, NodeSelector<Node> nodeFactory, MiResultSet resultSet) throws MiException;
}
