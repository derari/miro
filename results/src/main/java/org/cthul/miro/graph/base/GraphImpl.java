package org.cthul.miro.graph.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.syntax.MiQueryString;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.syntax.MiUpdateString;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeType;

/**
 *
 */
public class GraphImpl implements GraphApi {
    
    private final Map<Object, NodeSet<?>> nodeSets = new HashMap<>();
    private final MiConnection connection;

    public GraphImpl(MiConnection connection) {
        this.connection = connection;
    }

    public GraphImpl(MiConnection connection, Map<Object, NodeType<?>> types) {
        this.connection = connection;
        types.entrySet().forEach((typeEntry) -> {
            addType(typeEntry.getKey(), typeEntry.getValue());
        });
    }
    
    public final void addType(Object typeKey, NodeType<?> type) {
        nodeSets.put(typeKey, new NodeSet<>(type));
    }
    
    @Override
    public MiQueryString newQuery() {
        return connection.newQuery();
    }

    @Override
    public MiUpdateString newUpdate() {
        return connection.newUpdate();
    }

    @Override
    public <Stmt> Stmt newStatement(RequestType<Stmt> type) {
        return connection.newStatement(type);
    }

    @SuppressWarnings("unchecked")
    protected <Node> NodeSet<Node> nodeSet(Object key) {
        return (NodeSet<Node>) nodeSets.get(key);
    }

    @Override
    public <Node> NodeSelector<Node> nodeSelector(Object typeKey, List<String> attributes) throws MiException {
        return this.<Node>nodeSet(typeKey).newSelector(attributes);
    }

    @Override
    public <Node> EntityType<Node> entityType(Object typeKey) throws MiException {
        return nodeSet(typeKey);
    }

    @Override
    public <Node> EntityType<Node> entityType(Object typeKey, List<String> attributes) throws MiException {
        NodeSet<Node> nodeSet = nodeSet(typeKey);
        return nodeSet.with(nodeSet.newSetterConfiguration(attributes));
    }

    @Override
    public <Node> EntityConfiguration<Node> entityConfiguration(Object typeKey, List<String> attributes) throws MiException {
        return this.<Node>nodeSet(typeKey).newLoaderConfiguration(attributes);
    }

    @Override
    public void close() throws MiException {        
    }
    
    protected class NodeSet<Entity> implements EntityType<Entity> {

        private final KeyMap.MultiKey<Entity> map = new KeyMap.MultiKey<>();
        private final NodeType<Entity> nodeType;

        public NodeSet(NodeType<Entity> nodeType) {
            this.nodeType = nodeType;
        }

        public NodeSelector<Entity> newFactory() throws MiException {
            return new NodeSelector<Entity>() {
                NodeSelector<Entity> factory = null;
                NodeSelector<Entity> factory() throws MiException {
                    if (factory == null) {
                        factory = nodeType.newNodeFactory(GraphImpl.this);
                    }
                    return factory;
                }
                @Override
                public Entity get(Object... key) throws MiException {
                    Entity e = map.get(key);
                    if (e == null) {
                        e = factory().get(key);
                        map.put(key, e);
                    }
                    return e;
                }
                @Override
                public void complete() throws MiException {
                    if (factory != null) {
                        factory.complete();
                    }
                }
                @Override
                public void close() throws MiException {
                    if (factory != null) {
                        factory.close();
                    }
                }
            };
        }

        public NodeSelector<Entity> newSelector(List<String> attributes) throws MiException {
            return nodeType.newAttributeLoader(GraphImpl.this, attributes, newFactory());
        }

        @Override
        public EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException {
            return nodeType.newEntityFactory(GraphImpl.this, newFactory(), rs);
        }

        @Override
        public Entity[] newArray(int length) {
            return nodeType.newArray(length);
        }

        public EntityConfiguration<Entity> newLoaderConfiguration(List<String> attributes) throws MiException {
            return nodeType.newAttributeLoader(GraphImpl.this, attributes);
        }

        public EntityConfiguration<Entity> newSetterConfiguration(List<String> attributes) throws MiException {
            return nodeType.newAttributeSetter(GraphImpl.this, attributes);
        }
    }
}
