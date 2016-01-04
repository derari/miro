package org.cthul.miro.map.impl;

import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.stmt.MiQuery;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.result.Results;

/**
 *
 */
public class MappedQueryComposer<Entity, Statement extends MiQuery> 
                extends MappedStatementComposer<Entity, Statement>
                implements RequestComposer<Results<Entity>> {

    public MappedQueryComposer(EntityType<Entity> entityType, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(entityType, requestType, template);
    }

    public MappedQueryComposer(Class<Entity> entityClass, EntityType<Entity> entityType, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(entityClass, entityType, requestType, template);
    }

    public MappedQueryComposer(Class<Entity> clazz, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(clazz, requestType, template);
    }

    public MappedQueryComposer(Class<Entity> clazz, Graph graph, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(clazz, graph, requestType, template);
    }

    public MappedQueryComposer(Class<Entity> clazz, GraphApi graphApi, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(clazz, graphApi, requestType, template);
    }

    public MappedQueryComposer(Class<Entity> clazz, GraphSchema schema, RequestType<Statement> requestType, Template<? super MappedStatementBuilder<Entity, Statement>> template) {
        super(clazz, schema, requestType, template);
    }
    
    @Override
    public Results<Entity> execute() throws MiException {
        MappedStatement<Entity, Statement> stmt = buildStatement();
        EntityType<Entity> et = stmt.getEntityType();
        MiResultSet rs = stmt.getStatement().execute();
        return newResult(rs, et);
    }

    protected Results<Entity> newResult(MiResultSet rs, EntityType<Entity> et) {
        return new Results<>(rs, et);
    }
    
    @Override
    public Results.Action<Entity> asAction() {
        MappedStatement<Entity, Statement> stmt = buildStatement();
        return stmt.getStatement().asAction().andThen(Results.build(stmt::getEntityType));
    }
    
//    public static class MappedResult<Entity> {
//        EntityType<Entity> entityType;
//        MiResultSet resultSet;
//
//        public MappedResult(EntityType<Entity> entityType, MiResultSet resultSet) {
//            this.entityType = entityType;
//            this.resultSet = resultSet;
//        }
//
//        public EntityType<Entity> getEntityType() {
//            return entityType;
//        }
//
//        public MiResultSet getResultSet() {
//            return resultSet;
//        }
//    }
}
