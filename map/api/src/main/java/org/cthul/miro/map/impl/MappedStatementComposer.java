package org.cthul.miro.map.impl;

import org.cthul.miro.composer.ComposerKey;
import org.cthul.miro.composer.Template;
import org.cthul.miro.composer.impl.AbstractRequestComposer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.stmt.MiStatement;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.util.Closables;

/**
 *
 * @param <Entity>
 * @param <Statement>
 */
public abstract class MappedStatementComposer<Entity, Statement extends MiStatement<?>> 
                extends AbstractRequestComposer<Statement, 
                        MappedStatement<Entity, Statement>> {

    private final Class<Entity> entityClass;
    private EntityType<Entity> entityType;
    private Graph graph;
    private GraphApi graphApi;
    private GraphSchema schema;

    public MappedStatementComposer(EntityType<Entity> entityType, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(requestType, template);
        this.entityType = entityType;
        this.entityClass = null;
    }

    public MappedStatementComposer(Class<Entity> entityClass, EntityType<Entity> entityType, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(requestType, template);
        this.entityClass = entityClass;
        this.entityType = entityType;
    }

    public MappedStatementComposer(Class<Entity> clazz, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(requestType, template);
        this.entityClass = clazz;
    }

    public MappedStatementComposer(Class<Entity> clazz, Graph graph, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(requestType, template);
        this.entityClass = clazz;
        this.graph = graph;
    }

    public MappedStatementComposer(Class<Entity> clazz, GraphApi graphApi, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(requestType, template);
        this.entityClass = clazz;
        this.graph = graphApi;
        this.graphApi = graphApi;
    }

    public MappedStatementComposer(Class<Entity> clazz, GraphSchema schema, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(requestType, template);
        this.entityClass = clazz;
        this.schema = schema;
    }

    private void clearEntityType() {
        if (entityClass != null) entityType = null;
        graph = graphApi = null;
        schema = null;
    }
    
    @Override
    public void require(Object key) {
        if (key instanceof GraphApi) {
            clearEntityType();
            graph = graphApi = (GraphApi) key;
        } else if (key instanceof Graph) {
            clearEntityType();
            graph = (Graph) key;
        } else if (key instanceof GraphSchema) {
            clearEntityType();
            schema = (GraphSchema) key;
        }
        super.require(key);
    }
    
    protected EntityType<Entity> entityType() {
        if (entityType != null) {
            return entityType;
        }
        require(ComposerKey.FETCH_KEYS);
        try {
            if (graph != null) {
                entityType = graph.entityType(entityClass);
            } else if (schema != null) {
                MiConnection cnn = getConnection();
                if (cnn == null) {
                    entityType = schema.newFakeGraph(null).entityType(entityClass);
                } else {
                    entityType = schema.newGraph(cnn).entityType(entityClass);
                }
            }
        } catch (MiException e) {
            throw Closables.unchecked(e);
        }
        if (entityType != null) {
            return entityType;
        }
        throw new IllegalStateException("No entity type specified.");
    }

    @Override
    protected MappedStatement<Entity, Statement> newBuilder(Statement statement) {
        return new MappedStatement<>(entityType(), statement);
    }    
}
