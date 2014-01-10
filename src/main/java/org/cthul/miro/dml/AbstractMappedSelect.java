package org.cthul.miro.dml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.*;
import org.cthul.miro.graph.EntityGraphAdapter;
import org.cthul.miro.map.AbstractMappedQuery;
import org.cthul.miro.map.MappedTemplateProvider;
import org.cthul.miro.query.QueryType;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.result.ResultBuilder;

public class AbstractMappedSelect<Entity, Result, This extends MappedSelect<Entity, Result>> extends AbstractMappedQuery<Entity> implements MappedSelect<Entity, Result> {

    private final MappedTemplateProvider<Entity> templateProvider;
    private final ResultBuilder<Result, Entity> resultBuilder;
    
    public AbstractMappedSelect(QueryType<?> type, MappedTemplateProvider<Entity> templateProvider, ResultBuilder<Result, Entity> resultBuilder, String... select) {
        super(type, templateProvider);
        this.templateProvider = templateProvider;
        this.resultBuilder = resultBuilder;
        put(DataQueryKey.SELECT, (Object[]) select);
    }
    
    public AbstractMappedSelect( MappedTemplateProvider<Entity> templateProvider, ResultBuilder<Result, Entity> resultBuilder, String... select) {
        super(DataQuery.SELECT, templateProvider);
        this.templateProvider = templateProvider;
        this.resultBuilder = resultBuilder;
        put(DataQueryKey.SELECT, (Object[]) select);
    }
    
    protected This self() {
        return (This) this;
    }
    
    @Override
    public This into(Entity... entities) {
        return into(Arrays.asList(entities));
    }
    
    @Override
    public This into(List<Entity> entities) {
        EntityGraphAdapter<Entity> ga = templateProvider.getGraphAdapter();
        Object[] empty = {};
        put(DataQueryKey.ALL_KEY_DEPENDENCIES);
        addToGraph(entities);
        for (Entity e: entities) {
            Object[] key = ga.getKey(e, empty);
            put2(DataQueryKey.KEYS_IN, DataQuerySubkey.ADD, key);
        }
        return self();
    }
    
    @Override
    public This where(String key, Object... args) {
        put(key, args);
        return self();
    }

    @Override
    public Result _execute(MiConnection cnn) {
        try {
            return execute(cnn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Result execute(MiConnection cnn) throws SQLException {
        ResultSet rs = executeJdbc(cnn);
        return makeResult(cnn, rs);
    }
    
    @Override
    public MiFuture<Result> submit(MiConnection cnn) {
        return submitJdbc(cnn).onComplete(makeResultAction(cnn));
    }
    
    protected Result makeResult(MiConnection cnn, ResultSet rs) throws SQLException {
        return resultBuilder.build(rs, getEntityType(), getConfiguration(cnn));
    }
    
    protected MiFutureAction<MiFuture<ResultSet>, Result> makeResultAction(final MiConnection cnn) {
        return new MiFutureAction<MiFuture<ResultSet>, Result>() {
            @Override
            public Result call(MiFuture<ResultSet> f) throws Exception {
                return makeResult(cnn, f.get());
            }
        };
    }
}
