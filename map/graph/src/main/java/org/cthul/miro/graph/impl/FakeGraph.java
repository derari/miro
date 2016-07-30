package org.cthul.miro.graph.impl;

import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeType;

/**
 *
 */
public class FakeGraph extends AbstractGraph {

    public FakeGraph(MiConnection connection) {
        super(connection);
    }

    public FakeGraph(MiConnection connection, Function<Object, NodeType<?>> typeLookUp) {
        super(connection, typeLookUp);
    }

    public FakeGraph(MiConnection connection, Map<Object, NodeType<?>> types) {
        super(connection, types);
    }

    @Override
    protected AbstractNodeSet<?> newNodeSet(NodeType<?> type) {
        class FakeNodeSet<Node> extends AbstractNodeSet<Node> {
            public FakeNodeSet() {
                super((NodeType) type, FakeGraph.this);
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
        return new FakeNodeSet<>();
    }
}
