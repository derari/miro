package org.cthul.miro.sql.set;

import java.util.function.Consumer;
import java.util.function.Function;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.map.*;
import org.cthul.miro.request.ComposerState;
import org.cthul.miro.request.ComposerState.Behavior;
import org.cthul.miro.request.impl.SnippetSet;
import org.cthul.miro.set.base.AbstractQuerySet;
import org.cthul.miro.request.part.BatchNode;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.template.Snippets;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.template.AttributeFilter;
import org.cthul.miro.sql.template.SelectComposer;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class SqlEntitySet<Entity, This extends SqlEntitySet<Entity, This>> extends AbstractQuerySet<Entity, SelectQuery, MappedSelectRequest<Entity>, This> {

    private final MappedSelectRequest<Entity> composerPrototype;
    
    public SqlEntitySet(MiConnection cnn, MappedSqlType<Entity> type) {
        this(cnn, type.newMappedSelectComposer());
    }

    public SqlEntitySet(MiConnection cnn, MappedSelectRequest<Entity> composer) {
        super(cnn, SqlDQML.select());
        this.composerPrototype = composer;
    }

    protected SqlEntitySet(SqlEntitySet<Entity, This> source) {
        super(source);
        this.composerPrototype = source.composerPrototype;
    }
    
    protected This snippet(String key, Object... args) {
        return compose(c -> snippets(c).get(key).set(args));
    }
    
    protected This snippets(Consumer<? super Snippets<MappedQuery<Entity, SelectQuery>>> action) {
        return compose(c -> action.accept(snippets(c)));
    }
    
    protected This setSnippet(String key, Object... args) {
        return snippets(s -> s.set(key, args));
    }
    
    protected This build(Consumer<? super MappedQuery<Entity, SelectQuery>> snippet) {
        return snippets(s -> s.add(snippet));
    }
    
    protected This buildOnce(Consumer<? super MappedQuery<Entity, SelectQuery>> snippet) {
        return snippets(s -> s.once(snippet));
    }

    protected This sql(Consumer<? super SelectBuilder> action) {
        return build(snp -> action.accept(snp.getStatement()));
    }
    
    protected This sql(String sql, Object... args) {
        return build(stmt -> stmt.getStatement().sql(sql, args));
    }
    
    protected This map(Consumer<? super Mapping<Entity>> action) {
        return build(stmt -> action.accept(stmt.getMapping()));
    }
    protected This configureWith(EntityConfiguration<? super Entity> cfg) {
        return compose(c -> c.getConfiguration().configureWith(cfg));
    }
    
    protected This initializeWith(EntityInitializer<? super Entity> cfg) {
        return compose(c -> c.getConfiguration().initializeWith(cfg));
    }
    
    protected void intializeSnippetSet(SnippetSet<MappedQuery<Entity, SelectQuery>> snippetSet) {
    }

    @Override
    protected MappedSelectRequest<Entity> newComposer() {
        SnippetSet<MappedQuery<Entity, SelectQuery>> snippetSet = new SnippetSet<>();
        intializeSnippetSet(snippetSet);
        MappedSelectRequest<Entity> baseComposer = composerPrototype.copy();
        return ComposerState.builder()
                .setImpl(new WitSnippetsImpl<>())
                .addInterfaces(MappedSelectRequest.class, baseComposer, snippetSet)
                .put("getMappedSelectComposerDelegate", baseComposer)
                .put("getSnippets", snippetSet.newSnippetsPart())
                .create();
    }
    
    private Snippets<MappedQuery<Entity, SelectQuery>> snippets(MappedSelectRequest<?> req) {
        return ((SnippetComposer) req).getSnippets();
    }
    
    protected static final Function<MappedSelectRequest<?>, BatchNode<String>> FETCH = c -> c.getFetchedProperties();
    protected static final Function<MappedSelectRequest<?>, BatchNode<String>> LOAD = c -> c.getLoadedProperties();
    protected static final Function<MappedSelectRequest<?>, BatchNode<Object>> PROPERTY_FILTER = c -> c.getPropertyFilter();
    
    protected static interface MappedSelectComposerWithSnippets<Entity> extends 
            MappedSelectComposer.Delegator<Entity>, SnippetComposer<MappedQuery<Entity, SelectQuery>> {
        
        MappedSelectComposer<Entity> getMappedSelectComposerDelegate();

        @Override
        default PropertyFilterComposer getPropertyFilterComposerDelegate() {
            return getMappedSelectComposerDelegate();
        }

        @Override
        default SelectComposer getSelectComposerDelegate() {
            return getMappedSelectComposerDelegate();
        }

        @Override
        default MappedQueryComposer<Entity> getMappedQueryComposerDelegate() {
            return getMappedSelectComposerDelegate();
        }
//
//        @Override
//        default AttributeFilter getAttributeFilter() {
//            return MappedSelectComposer.Delegator.super.getAttributeFilter();
//        }
//
//        @Override
//        default ListNode<String> getSelectedAttributes() {
//            return MappedSelectComposer.Delegator.super.getSelectedAttributes();
//        }
    }
    
    protected static class WitSnippetsImpl<Entity> implements Behavior<MappedSelectComposerWithSnippets<Entity>>, MappedSelectComposerWithSnippets<Entity> {

        private MappedSelectComposerWithSnippets<Entity> actual;
        
        @Override
        public MappedSelectComposerWithSnippets<Entity> copy() {
            return new WitSnippetsImpl<>();
        }

        @Override
        public void initialize(MappedSelectComposerWithSnippets<Entity> composer) {
            actual = composer;
        }

        @Override
        public MappedSelectComposer<Entity> getMappedSelectComposerDelegate() {
            return actual.getMappedSelectComposerDelegate();
        }
        
        @Override
        public Snippets<MappedQuery<Entity, SelectQuery>> getSnippets() {
            return actual.getSnippets();
        }
    }
}
