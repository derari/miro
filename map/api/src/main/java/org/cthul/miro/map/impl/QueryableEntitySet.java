package org.cthul.miro.map.impl;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.stmt.MiQuery;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.result.Results;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.map.MappedStatement;

/**
 * References a set of entities that can be queried with a {@link RequestComposer}.
 * @param <Entity>
 */
public class QueryableEntitySet<Entity> {
    
    private MiConnection connection;
    private final Class<Entity> entityClazz;
    private Graph graph;
    private EntityType<Entity> entityType;
    private boolean useGraph = false;

    public QueryableEntitySet() {
        this.entityClazz = null;
    }

    public QueryableEntitySet(Class<Entity> entityClazz) {
        this.entityClazz = entityClazz;
    }

    public QueryableEntitySet(Class<Entity> entityClazz, EntityType<Entity> entityType) {
        this.entityClazz = entityClazz;
        setEntityType(entityType);
    }

    public QueryableEntitySet(EntityType<Entity> entityType) {
        this.entityClazz = null;
        setEntityType(entityType);
    }

    public QueryableEntitySet(Class<Entity> entityClazz, Graph graph) {
        this.entityClazz = entityClazz;
        setGraph(graph);
    }

    protected QueryableEntitySet(QueryableEntitySet<Entity> source) {
        this.connection = source.connection;
        this.entityClazz = source.entityClazz;
        this.entityType = source.entityType;
        this.graph = source.graph;
        this.useGraph = source.useGraph;
    }

    public void setConnection(MiConnection connection) {
        this.connection = connection;
    }

    public MiConnection getConnection() {
        return connection;
    }

    public Graph getGraph() {
        return graph;
    }

    public final void setGraph(Graph graph) {
        if (graph != null && entityClazz == null) {
            throw new NullPointerException("entity class");
        }
        this.useGraph = graph != null;
        this.graph = graph;
        if (graph instanceof MiConnection) {
            setConnection((MiConnection) graph);
        }
    }

    public EntityType<Entity> getEntityType() {
        return entityType;
    }

    public final void setEntityType(EntityType<Entity> entityType) {
        this.useGraph = entityType == null;
        this.entityType = entityType;
    }
    
    protected EntityType<Entity> entityType() {
        if (useGraph) {
            if (graph == null) {
                throw new IllegalStateException("Graph or entity type required");
            }
            return graph.getEntityType(entityClazz);
        } else {
            if (entityType == null) {
                throw new IllegalStateException("Graph or entity type required");
            }
            return entityType;
        }
    }
    
    protected MiConnection connection() {
        if (connection == null) {
            throw new IllegalStateException("Connection required");
        }
        return connection;
    }
    
    public <Stmt> Results.Action<Entity> query(RequestType<? extends Stmt> requestType, RequestComposer<? super MappedStatement<Entity, Stmt>> builder) {
        EntityType<Entity> et = entityType();
        Stmt stmt = connection().newStatement(requestType);
        SimpleMappedStatement<Entity, Stmt> mb = new SimpleMappedStatement<>(et, stmt);
        builder.build(mb);
        return ((MiQuery) mb.getStatement()).asAction()
                .andThen(Results.build(mb.getEntityType()));
    }
    
    public QueryableEntitySet<Entity> copy() {
        return new QueryableEntitySet<>(this);
    }
}
