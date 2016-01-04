package org.cthul.miro.at.compose;

import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.StatementFactory;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.sql.SelectQuery;
import org.cthul.miro.db.sql.SqlDQML;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.map.impl.MappedQueryComposer;

/**
 *
 */
public class MappedInterfaceStatementBuilder<Entity> {
    
    private final ComposerFactoy<Entity> factory;

    @SuppressWarnings("Convert2Lambda")
    public MappedInterfaceStatementBuilder(Class<Entity> entityClass, EntityType<Entity> entityType) {
        this(new ComposerFactoy<Entity>() {
            @Override
            public <Statement> RequestComposer<?> newComposer(RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
                return new MappedQueryComposer(entityClass, entityType, requestType, template);
            }
        });
    }

    @SuppressWarnings("Convert2Lambda")
    public MappedInterfaceStatementBuilder(Class<Entity> entityClass, Graph graph) {
        this(new ComposerFactoy<Entity>() {
            @Override
            public <Statement> RequestComposer<?> newComposer(RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
                return new MappedQueryComposer(entityClass, graph, requestType, template);
            }
        });
    }

    @SuppressWarnings("Convert2Lambda")
    public MappedInterfaceStatementBuilder(Class<Entity> entityClass, GraphSchema schema) {
        this(new ComposerFactoy<Entity>() {
            @Override
            public <Statement> RequestComposer<?> newComposer(RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
                return new MappedQueryComposer(entityClass, schema, requestType, template);
            }
        });
    }
    
    public MappedInterfaceStatementBuilder(ComposerFactoy<Entity> factory) {
        this.factory = factory;
    }
    
    public <Builder, Statement> StatementFactory<MappedStatementBuilder<Entity, ? extends Builder>, Statement> newFactory(RequestType<Builder> requestType, Class<Statement> stmtClass) {
        return new InterfaceStatementFactory<>(factory.forRequestType(requestType), stmtClass);
    }
    
    public <Statement> StatementFactory<MappedStatementBuilder<Entity, ? extends SelectQuery>, Statement> select(Class<Statement> stmtClass) {
        return newFactory(SqlDQML.DQML.SELECT, stmtClass);
    }
    
    public InterfaceTemplateLayer<Entity, SqlFilterableClause, SqlFilterableClause, SqlFilterableClause, SqlFilterableClause> getTemplates() {
        return new InterfaceTemplateLayer<>();
    }
    
    public static interface ComposerFactoy<Entity> {
        
        <SqlStatement> RequestComposer<?> newComposer(RequestType<SqlStatement> requestType, Template<? super MappedStatementBuilder<Entity, SqlStatement>> template);
        
        default <SqlStatement> StatementFactory<MappedStatementBuilder<Entity, ? extends SqlStatement>, RequestComposer<?>> forRequestType(RequestType<SqlStatement> requestType) {
            return (template, attributes) -> {
                RequestComposer<?> c = newComposer(requestType, template);
                c.requireAll(attributes);
                return c;
            };
        }
    }
}
