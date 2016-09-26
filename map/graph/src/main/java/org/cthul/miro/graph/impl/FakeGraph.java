package org.cthul.miro.graph.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.FactoryBuilder;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.graph.SelectorBuilder;

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
            public void newNodeSelector(SelectorBuilder<? super Node> builder) throws MiException {
                getNodeType().newNodeFactory(getGraph(), builder);
            }
            @Override
            public EntityType<Node> getEntityType(List<?> attributes) {
                return getNodeType().asEntityType(getGraph(), this, attributes);
//                return (rs, b) -> {
//                    getNodeType().newEntityFactory(rs, getGraph(), b);
//                    if (attributes.isEmpty()) return;
//                    FactoryBuilder<Node> fb = (FactoryBuilder) b;
//                    fb.add(getNodeType().getAttributeReader(getGraph(), attributes), rs);
//                };
            }
            @Override
            protected String shortString(Object s) {
                return String.valueOf(s);
            }
        }
        return new FakeNodeSet<>();
    }
}
