package org.cthul.miro.graph.impl;

import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
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
    protected NodeSet<?> newNodeSet(NodeType<?> type) {
        return new FakeNodeSet<>(type, this);
    }
}
