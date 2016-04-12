package org.cthul.miro.graph.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeType;

/**
 *
 */
public abstract class AbstractGraph implements GraphApi {
    
    private final Map<Object, NodeSet<?>> nodeSets = new HashMap<>();
    private final Function<Object, NodeType<?>> typeLookUp;
    private final MiConnection connection;

    public AbstractGraph(MiConnection connection) {
        this(connection, (Function) null);
    }

    public AbstractGraph(MiConnection connection, Function<Object, NodeType<?>> typeLookUp) {
        this.connection = connection;
        this.typeLookUp = typeLookUp;
    }

    public AbstractGraph(MiConnection connection, Map<Object, NodeType<?>> types) {
        this(connection);
        types.entrySet().forEach((typeEntry) -> {
            addType(typeEntry.getKey(), typeEntry.getValue());
        });
    }
    
    public final void addType(Object typeKey, NodeType<?> type) {
        nodeSets.put(typeKey, newNodeSet(type));
    }
    
    protected abstract NodeSet<?> newNodeSet(NodeType<?> type);
    
    //<editor-fold defaultstate="collapsed" desc="MiConnection implemention">
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
    //</editor-fold>
    
    @SuppressWarnings("unchecked")
    protected <Node> NodeSet<Node> nodeSet(Object key) {
        NodeSet<Node> n = (NodeSet<Node>) nodeSets.get(key);
        if (n == null) {
            if (typeLookUp != null) {
                NodeType<?> t = typeLookUp.apply(key);
                if (t != null) {
                    addType(key, t);
                    return nodeSet(key);
                }
            }
            throw new IllegalArgumentException(
                    "Unknown node type: " + key);
        }
        return n;
    }

    @Override
    public <Node> NodeSelector<Node> newNodeSelector(Object typeKey) throws MiException {
        return this.<Node>nodeSet(typeKey).newNodeSelector();
    }

    @Override
    public <Node> EntityType<Node> getEntityType(Object typeKey, List<?> attributes) {
        return this.<Node>nodeSet(typeKey).getEntityType(attributes);
    }

    @Override
    public <Node> EntityInitializer<Node> newAttributeLoader(Object typeKey, List<?> attributes) throws MiException {
        return this.<Node>nodeSet(typeKey).newAttributeLoader(attributes);
    }

    @Override
    public void close() throws MiException {        
    }
}
