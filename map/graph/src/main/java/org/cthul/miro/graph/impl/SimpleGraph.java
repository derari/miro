package org.cthul.miro.graph.impl;

import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeType;

/**
 *
 */
public class SimpleGraph extends AbstractGraph {

    public SimpleGraph(MiConnection connection) {
        super(connection);
    }

    public SimpleGraph(MiConnection connection, Function<Object, NodeType<?>> typeLookUp) {
        super(connection, typeLookUp);
    }

    public SimpleGraph(MiConnection connection, Map<Object, NodeType<?>> types) {
        super(connection, types);
    }

    @Override
    protected AbstractNodeSet<?> newNodeSet(NodeType<?> type) {
        return new SimpleNodeSet<>(type, this);
    }
    
    private static class SimpleNodeSet<Node> extends AbstractNodeSet<Node> {
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
}
