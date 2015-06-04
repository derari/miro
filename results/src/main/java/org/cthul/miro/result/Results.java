package org.cthul.miro.result;

import java.util.Collection;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityType;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiActionFunction;
import org.cthul.miro.futures.impl.MiActionDelegator;
import org.cthul.miro.result.cursor.ResultCursor;

public class Results<Entity> implements AutoCloseable {
    
    public static <Entity> Builder<Entity> build(EntityType<Entity> entityType) {
        return new Builder<>(entityType);
    }
    
    protected final MiResultSet rs;
    protected final EntityType<Entity> type;

    public Results(MiResultSet rs, EntityType<Entity> type) {
        this.rs = rs;
        this.type = type;
    }
    
    protected <Result> Result build(EntityResultBuilder<Result, Entity> builder) throws MiException {
        return builder.build(rs, type);
    }
    
    protected <Result> Result _build(EntityResultBuilder<Result, Entity> builder) {
        try {
            return builder.build(rs, type);
        } catch (MiException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<Entity> asList() throws MiException {
        return build(ResultBuilders.getListResult());
    }
    
    public Entity[] asArray() throws MiException {
        return build(ResultBuilders.getArrayResult());
    }
    
    public ResultCursor<Entity> asCursor() throws MiException {
        return build(ResultBuilders.getCursorResult());
    }
    
    public Entity getFirst() throws MiException {
        return build(ResultBuilders.getFirstResult());
    }
    
    public Entity getSingle() throws MiException {
        return build(ResultBuilders.getSingleResult());
    }

    public void noResult() throws MiException {
        asList();
    }
    
    public List<Entity> _asList() {
        return _build(ResultBuilders.getListResult());
    }
    
    public Entity[] _asArray() {
        return _build(ResultBuilders.getArrayResult());
    }
    
    public ResultCursor<Entity> _asCursor() {
        return _build(ResultBuilders.getCursorResult());
    }
    
    public Entity _getFirst() {
        return _build(ResultBuilders.getFirstResult());
    }
    
    public Entity _getSingle() {
        return _build(ResultBuilders.getSingleResult());
    }

    public void _noResult() {
        _asList();
    }
    
    @Override
    public void close() throws MiException {
        rs.close();
    }
    
    public void _close() {
        try {
            close();
        } catch (MiException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static class Builder<Entity> implements MiActionFunction<MiResultSet, Results<Entity>, Future<Entity>> {
        
        private final EntityType<Entity> entityType;

        public Builder(EntityType<Entity> entityType) {
            this.entityType = entityType;
        }

        @Override
        public Future<Entity> wrap(MiAction<? extends Results<Entity>> future) {
            return new Future<>(future);
        }

        @Override
        public Results<Entity> call(MiResultSet resultSet) throws Throwable {
            return new Results<>(resultSet, entityType);
        }
        
        @SafeVarargs
        public final Builder<Entity> with(EntityConfiguration<? super Entity>... configurations) {
            return new Builder<>(EntityTypes.configuredType(entityType, configurations));
        }
        
        public final Builder<Entity> with(Collection<EntityConfiguration<? super Entity>> configurations) {
            return new Builder<>(EntityTypes.configuredType(entityType, configurations));
        }
    }
    
    public static class Future<Entity> extends MiActionDelegator<Results<Entity>> {

        public Future(MiAction<? extends Results<Entity>> delegatee) {
            super(delegatee);
        }
        
        public MiAction<List<Entity>> list() {
            return andThen(Results::asList);
        }
        
        public List<Entity> asList() throws InterruptedException, ExecutionException, MiException {
            return submit().get().asList();
        }
        
        public List<Entity> _asList() {
            return submit()._get()._asList();
        }
        
        public MiAction<Entity[]> array() {
            return andThen(Results::asArray);
        }
        
        public Entity[] asArray() throws InterruptedException, ExecutionException, MiException {
            return submit().get().asArray();
        }
        
        public Entity[] _asArray() {
            return submit()._get()._asArray();
        }
        
        // TODO: cursor
        
        public MiAction<Entity> first() {
            return andThen(Results::getFirst);
        }
        
        public Entity getFirst() throws InterruptedException, ExecutionException, MiException {
            return submit().get().getFirst();
        }
        
        public Entity _getFirst() {
            return submit()._get()._getFirst();
        }
        
        public MiAction<Entity> single() {
            return andThen(Results::getSingle);
        }
        
        public Entity getSingle() throws InterruptedException, ExecutionException, MiException {
            return submit().get().getSingle();
        }
        
        public Entity _getSingle() {
            return submit()._get()._getSingle();
        }
        
        public void noResult() throws InterruptedException, ExecutionException, MiException {
            submit().get().noResult();
        }
        
        public void _noResult() {
            submit()._get()._noResult();
        }
    }
}
