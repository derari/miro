package org.cthul.miro.graph;

import org.cthul.miro.db.MiException;
import org.cthul.miro.graph.impl.CompositeSelector;

/**
 *
 * @param <Node>
 */
public interface NodeSet<Node> {
    
    default NodeSelector<Node> newNodeSelector() throws MiException {
        return CompositeSelector.buildSelector(this::newNodeSelector);
    }
    
    void newNodeSelector(SelectorBuilder<? super Node> builder) throws MiException;
}
