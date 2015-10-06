package org.cthul.miro.dsl;

import org.cthul.miro.composer.QueryComposer;
import org.cthul.miro.composer.StatementComposer;
import org.cthul.miro.composer.StatementHolder;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.db.MiQuery;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.syntax.RequestType;
import org.cthul.miro.dsl.EntityQueryComposer.MyBuilder;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFutures;
import org.cthul.miro.map.EntitySetup;
import org.cthul.miro.result.EntityResultBuilder;

/**
 * An {@link EntityQuery} that implements {@link QueryComposer}.
 * @param <Query> internal statement type
 * @param <Entity> entity type
 */
public class EntityQueryComposer<Query extends MiQuery, Entity> 
                extends StatementComposer<Query, MyBuilder<Query, Entity>>
                implements EntityQuery<Entity> {

    private final EntityType<? extends Entity> entityType;

    public EntityQueryComposer(EntityType<Entity> entityType, RequestType<Query> requestType, Template<? super MyBuilder<Query, Entity>> template) {
        super(requestType, template);
        this.entityType = entityType;
    }
    
    @Override
    public <Result> ObjectQuery<Result> getResult(EntityResultBuilder<? extends Result, ? super Entity> resultBuilder) {
        return new ObjectQueryImpl<>(resultBuilder);
    }

    @Override
    protected MyBuilder newBuilder(Query statement) {
        return new MyBuilder(entityType, statement);
    }
    
    protected <Result> MiAction<Result> resultAction(EntityResultBuilder<? extends Result, ? super Entity> resultBuilder) {
        return MiFutures.action(() -> {
            MyBuilder<Query, Entity> builder = buildStatement();
            MiResultSet rs = builder.getStatement().execute();
            return resultBuilder.build(rs, builder.getEntityType());
        });
    }

    protected class ObjectQueryImpl<Result> implements ObjectQuery<Result> {
        
        private final EntityResultBuilder<? extends Result, ? super Entity> resultBuilder;

        public ObjectQueryImpl(EntityResultBuilder<? extends Result, ? super Entity> resultBuilder) {
            this.resultBuilder = resultBuilder;
        }

        @Override
        public MiAction<Result> asAction() {
            return resultAction(resultBuilder);
        }
    }
    
    protected static class MyBuilder<Statement, Entity> 
                     implements StatementHolder<Statement>,
                                EntitySetup<Entity> {

        private EntityType<? extends Entity> entityType;
        private final Statement statement;

        public MyBuilder(EntityType<? extends Entity> entityType, Statement statement) {
            this.entityType = entityType;
            this.statement = statement;
        }

        @Override
        public Statement getStatement() {
            return statement;
        }

        public EntityType<? extends Entity> getEntityType() {
            return entityType;
        }

        @Override
        public void configureWith(EntityConfiguration<? super Entity> config) {
            entityType = entityType.with(config);
        }
    }
}
