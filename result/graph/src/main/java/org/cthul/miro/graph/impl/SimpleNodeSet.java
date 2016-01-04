package org.cthul.miro.graph.impl;

import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeType;

/**
 * A set of nodes of one type in a graph.
 * Also implements the {@link NodeType} interface, except that it doesn't
 * need the graph parameter.
 * @param <Node>
 */
public class SimpleNodeSet<Node> extends NodeSet<Node> {
    
    private final KeyMap.MultiKey<Node> map = new KeyMap.MultiKey<>();

    public SimpleNodeSet(NodeType<Node> nodeType, GraphApi graph) {
        super(nodeType, graph);
    }

    @Override
    protected Node getNode(Object... key) {
        return map.get(key);
    }

    @Override
    protected void putNode(Object[] key, Node e) {
        map.put(key, e);
    }
}
