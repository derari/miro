package org.cthul.miro.graph.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.graph.SelectorBuilder;

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

        protected Node getNode(Object... key) {
            return map.get(key);
        }

        protected void putNode(Object[] key, Node e) {
            map.put(key, e);
        }
        
        @Override
        public void newNodeSelector(SelectorBuilder<? super Node> builder) throws MiException {
            NodeSelector<Node> factory = CompositeSelector.buildNestedSelector(builder, b -> {
                getNodeType().newNodeFactory(getGraph(), b);
            });
            builder.setFactory(key -> {
                Node e = getNode(key);
                if (e == null) {
                    e = factory.get(key);
                    putNode(key, e);
                }
                return e;
            });
            builder.addName(shortString(factory));
        }

        @Override
        public EntityType<Node> getEntityType(List<?> attributes) {
            return getNodeType().asEntityType(getGraph(), this, attributes);
        }
    }
}
