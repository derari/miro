package org.cthul.miro.graph.impl;

import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeType;

/**
 * A set of nodes of one type in a graph.
 * Also implements the {@link NodeType} interface, except that it doesn't
 * need the graph parameter.
 * @param <Node>
 */
public class FakeNodeSet<Node> extends NodeSet<Node> {
    
    public FakeNodeSet(NodeType<Node> nodeType, GraphApi graph) {
        super(nodeType, graph);
    }

    @Override
    protected Node getNode(Object... key) {
        return null;
    }

    @Override
    protected void putNode(Object[] key, Node e) {
    }

    @Override
    protected String shortString(Object s) {
        return String.valueOf(s);
    }
}
