package org.cthul.miro.graph;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.entity.FactoryBuilder;
import org.cthul.miro.entity.InitializationBuilder;
import org.cthul.miro.graph.impl.CompositeSelector;

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
    default NodeSelector<Node> newNodeFactory(GraphApi graph) throws MiException {
        return CompositeSelector.buildSelector(b -> newNodeFactory(graph, b));
    }
    
    /**
     * Creates a node selector that always creates new nodes.
     * @param graph used only to obtain nodes of other types
     * @param builder
     * @throws MiException 
     */
    void newNodeFactory(GraphApi graph, SelectorBuilder<? super Node> builder) throws MiException;
    
//    /**
//     * Creates an entity factory that always creates new nodes.
//     * Keys are provided by the result set.
//     * @param rs
//     * @param graph used only to obtain nodes of other types
//     * @return entity factory
//     * @throws MiException 
//     */
//    default EntityFactory<Node> newEntityFactory(MiResultSet rs, GraphApi graph) throws MiException {
//        return EntityTypes.buildFactory(b -> newEntityFactory(rs, graph, b));
//    }
//    
//    /**
//     * Creates an entity factory that always creates new nodes.
//     * Keys are provided by the result set.
//     * @param rs
//     * @param graph used only to obtain nodes of other types
//     * @param builder
//     * @throws MiException 
//     */
//    void newEntityFactory(MiResultSet rs, GraphApi graph, FactoryBuilder<? super Node> builder) throws MiException;
  
    /**
     * Creates an initializer that will load the given attributes from the database.
     * @param graph used to obtain nodes of other types and to connect to the database
     * @param attributes the attributes to load
     * @return attribute loader
     * @throws MiException 
     */
    default EntityInitializer<Node> newAttributeLoader(GraphApi graph, List<?> attributes) throws MiException {
        return EntityTypes.buildInitializer(b -> newAttributeLoader(graph, attributes, b));
    }
  
    /**
     * Creates an initializer that will load the given attributes from the database.
     * @param graph used to obtain nodes of other types and to connect to the database
     * @param attributes the attributes to load
     * @param builder
     * @throws MiException 
     */
    void newAttributeLoader(GraphApi graph, List<?> attributes, InitializationBuilder<? extends Node> builder) throws MiException;
    
    /**
     * Returns a configuration that will fetch the given attributes from a result set.
     * @param graph
     * @param attributes
     * @return attribute reader
     */
    EntityConfiguration<Node> getAttributeReader(GraphApi graph, List<?> attributes);
    
    /**
     * Creates an entity type that uses key attributes of the {@code resultSet} 
     * to obtains nodes from the {@code nodeSet}.
     * @param graph used only to obtain nodes of other types
     * @param nodeSet
     * @return entity type
     */
    EntityType<Node> asEntityType(GraphApi graph, NodeSet<Node> nodeSet);
    
    /**
     * Creates an entity type that uses key attributes of the {@code resultSet} 
     * to obtains nodes from the {@code nodeSet} and reads the
     * given attributes from the result set.
     * @param graph used only to obtain nodes of other types
     * @param nodeSet
     * @param attributes
     * @return entity type
     */
    default EntityType<Node> asEntityType(GraphApi graph, NodeSet<Node> nodeSet, List<?> attributes) {
        EntityType<Node> type = asEntityType(graph, nodeSet);
        if (attributes.isEmpty()) return type;
        return type.with(getAttributeReader(graph, attributes));
    }
}
