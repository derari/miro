package org.cthul.miro.sql.map;

import java.util.function.Function;
import org.cthul.miro.composer.AbstractComposer;
import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.map.MappedQueryComposer;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.composer.SelectComposer;

public class DefaultMappedSelectComposer<Entity, Builder>
        extends AbstractComposer<Builder, MappedQuery<Entity, SelectQuery>, MappedSelectComposer<Entity>>
        implements MappedSelectComposer.Internal<Entity>, MappedQueryComposer.Delegator<Entity> {

    public static <Entity> DefaultMappedSelectComposer<Entity, MappedQuery<Entity, SelectQuery>> create(MappedQueryComposer<Entity> mqComposer, SelectComposer selComposer) {
        return new DefaultMappedSelectComposer<>(mqComposer, selComposer, Function.identity());
    }
    
    public static <Entity> MappedSelectRequest<Entity> createRequest(MappedQueryComposer<Entity> mqComposer, SelectComposer selComposer) {
        return new Request<>(create(mqComposer, selComposer));
    }
    
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    protected static final KeyIndex INDEX = AbstractComposer.index();
    protected static final NodeKey SELECT_COMPOSER = INDEX.key();
    protected static final NodeKey MAPPED_COMPOSER = INDEX.key();
    
    public DefaultMappedSelectComposer(MappedQueryComposer<Entity> mqComposer, SelectComposer selComposer, Function<? super Builder, ? extends MappedQuery<Entity, SelectQuery>> builderAdapter) {
        super(INDEX, null, builderAdapter);
        putAll(
            SELECT_COMPOSER, adapt(selComposer, MappedQuery::getStatement),
            MAPPED_COMPOSER, extend(adapt(mqComposer, MappedQuery::getMapping)));
    }

    public DefaultMappedSelectComposer(DefaultMappedSelectComposer<Entity, ?> src, Function<? super Builder, ? extends MappedQuery<Entity, SelectQuery>> builderAdapter) {
        super(src, builderAdapter);
    }

    @Override
    protected Object copy(Function<?, ? extends MappedQuery<Entity, SelectQuery>> builderAdapter) {
        return new DefaultMappedSelectComposer/*<>*/(this, builderAdapter);
    }

    @Override
    public SelectComposer getSelectComposer() {
        return getNode(SELECT_COMPOSER);
    }

    @Override
    public MappedQueryComposer<Entity> getMappedQueryComposerDelegate() {
        return getNode(MAPPED_COMPOSER);
    }
    
    private static class Request<Entity> 
            extends AbstractRequest<MappedQuery<Entity, SelectQuery>, Object> 
            implements MappedSelectRequest<Entity>, MappedSelectComposer.Delegator<Entity> {
        
        final DefaultMappedSelectComposer<Entity, MappedQuery<Entity, SelectQuery>> composer;

        public Request(DefaultMappedSelectComposer<Entity, MappedQuery<Entity, SelectQuery>> composer) {
            super(composer);
            this.composer = composer;
        }
        
        @Override
        public MappedSelectComposer<Entity> getMappedSelectComposerDelegate() {
            return composer;
        }
        
        @Override
        public MappedSelectRequest<Entity> copy() {
            return new Request<>(composer._copy());
        }
    }
}
