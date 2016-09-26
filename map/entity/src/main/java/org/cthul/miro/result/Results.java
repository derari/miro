package org.cthul.miro.result;

import java.util.Collection;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityType;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.function.MiActionFunction;
import org.cthul.miro.futures.impl.MiActionDelegator;
import org.cthul.miro.result.cursor.ResultCursor;

public class Results<Entity> implements AutoCloseable {
    
    public static <Entity> Builder<Entity> build(EntityType<Entity> entityType) {
        return build(() -> entityType);
    }
    
    public static <Entity> Builder<Entity> build(Supplier<? extends EntityType<Entity>> entityType) {
        return new Builder<>(entityType);
    }
    
//    public static <Entity> Builder2<Entity> builder() {
//        return BUILDER;
//    }
    
    protected final MiResultSet rs;
    protected final EntityType<? extends Entity> type;

    public Results(MiResultSet rs, EntityType<? extends Entity> type) {
        this.rs = rs;
        this.type = type;
    }
    
    public <Result> Result build(EntityResultBuilder<Result, Entity> builder) throws MiException {
        return builder.build(rs, type);
    }
    
    public <Result> Result _build(EntityResultBuilder<Result, Entity> builder) {
        try {
            return builder.build(rs, type);
        } catch (MiException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<Entity> asList() throws MiException {
        return build(ResultBuilders.getListResult());
    }
    
    public Entity[] asArray(Class<? super Entity> clazz) throws MiException {
        return build(ResultBuilders.getArrayResult(clazz));
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
    
    public Entity[] _asArray(Class<? super Entity> clazz) {
        return _build(ResultBuilders.getArrayResult(clazz));
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
    
    public static class Builder<Entity> implements MiActionFunction<MiResultSet, Results<Entity>, Action<Entity>> {
        
        private final Supplier<? extends EntityType<Entity>> entityType;

        public Builder(Supplier<? extends EntityType<Entity>> entityType) {
            this.entityType = entityType;
        }

        @Override
        public Action<Entity> wrap(MiAction<? extends Results<Entity>> future) {
            return new Action<>(future);
        }

        @Override
        public Results<Entity> call(MiResultSet resultSet) {
            return new Results<>(resultSet, entityType.get());
        }
        
        private Supplier<? extends EntityType<Entity>> typeWith(EntityConfiguration<? super Entity>... configurations) {
            return () -> EntityTypes.configuredType(entityType.get(), configurations);
        }
        
        private Supplier<? extends EntityType<Entity>> typeWith(Collection<EntityConfiguration<? super Entity>> configurations) {
            return () -> EntityTypes.configuredType(entityType.get(), configurations);
        }
        
        @SafeVarargs
        public final Builder<Entity> with(EntityConfiguration<? super Entity>... configurations) {
            return new Builder<>(typeWith(configurations));
        }
        
        public final Builder<Entity> with(Collection<EntityConfiguration<? super Entity>> configurations) {
            return new Builder<>(typeWith(configurations));
        }
    }
    
//    private static final Builder2 BUILDER = new Builder2();
    
//    public static class Builder2<Entity> 
//                    implements MiActionFunction<EntityResultSet<Entity>, Results<Entity>, Action<Entity>>,
//                               EntityResultBuilder<Results<Entity>, Entity> {
//
//        private final EntityConfiguration<? super Entity> cfg;
//        
//        public Builder2() {
//            this(EntityTypes.noConfiguration());
//        }
//
//        public Builder2(EntityConfiguration<? super Entity> cfg) {
//            this.cfg = cfg;
//        }
//
//        @Override
//        public Action<Entity> wrap(MiAction<? extends Results<Entity>> future) {
//            return new Action<>(future);
//        }
//
//        @Override
//        public Results<Entity> call(EntityResultSet<Entity> ers) {
//            return new Results<>(ers.getResultSet(), ers.getEntityType().with(cfg));
//        }
//        
//        @SafeVarargs
//        public final Builder2<Entity> with(EntityConfiguration<? super Entity>... configurations) {
//            return new Builder2<>(EntityTypes.multiConfiguration(cfg, configurations));
//        }
//        
//        public final Builder2<Entity> with(Collection<EntityConfiguration<? super Entity>> configurations) {
//            return new Builder2<>(EntityTypes.multiConfiguration(cfg, configurations));
//        }
//
//        @Override
//        public Results<Entity> build(MiResultSet rs, EntityType<? extends Entity> type) throws MiException {
//            return new Results<>(rs, type);
//        }
//    }
    
    public static class Action<Entity> extends MiActionDelegator<Results<Entity>> {

        public Action(MiAction<? extends Results<Entity>> delegatee) {
            super(delegatee);
        }
        
        public MiAction<List<Entity>> list() {
            return andThen(Results::asList);
        }
        
        protected Results<Entity> quickGet() throws InterruptedException, ExecutionException {
            return getTrigger().get();
        }
        
        protected Results<Entity> _quickGet() {
            return getTrigger()._get();
        }
        
        public List<Entity> asList() throws InterruptedException, ExecutionException, MiException {
            return quickGet().asList();
        }
        
        public List<Entity> _asList() {
            return _quickGet()._asList();
        }
        
        public MiAction<Entity[]> array(Class<? super Entity> clazz) {
            return andThen(r -> r.asArray(clazz));
        }
        
        public Entity[] asArray(Class<? super Entity> clazz) throws InterruptedException, ExecutionException, MiException {
            return quickGet().asArray(clazz);
        }
        
        public Entity[] _asArray(Class<? super Entity> clazz) {
            return _quickGet()._asArray(clazz);
        }
        
        // TODO: cursor
        
        public MiAction<Entity> first() {
            return andThen(Results::getFirst);
        }
        
        public Entity getFirst() throws InterruptedException, ExecutionException, MiException {
            return quickGet().getFirst();
        }
        
        public Entity _getFirst() {
            return _quickGet()._getFirst();
        }
        
        public MiAction<Entity> single() {
            return andThen(Results::getSingle);
        }
        
        public Entity getSingle() throws InterruptedException, ExecutionException, MiException {
            return quickGet().getSingle();
        }
        
        public Entity _getSingle() {
            return _quickGet()._getSingle();
        }
        
        public void noResult() throws InterruptedException, ExecutionException, MiException {
            quickGet().noResult();
        }
        
        public void _noResult() {
            _quickGet()._noResult();
        }
    }
}
