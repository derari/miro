package org.cthul.miro.graph;

import java.util.List;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityFactory;

/**
 *
 */
public interface NodeType<Node> {
    
    Node[] newArray(int length);
    
    NodeSelector<Node> newNodeFactory(GraphApi graph) throws MiException;
    
    EntityFactory<Node> newEntityFactory(GraphApi graph, NodeSelector<Node> nodeFactory, MiResultSet resultSet) throws MiException;
    
    NodeSelector<Node> newAttributeLoader(GraphApi graph, List<String> attributes, NodeSelector<Node> nodeFactory) throws MiException;
    
    EntityConfiguration<Node> newAttributeLoader(GraphApi graph, List<String> attributes) throws MiException;
    
    EntityConfiguration<Node> newAttributeSetter(GraphApi graph, List<String> attributes) throws MiException;
}
