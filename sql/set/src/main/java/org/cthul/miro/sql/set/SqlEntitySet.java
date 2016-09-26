package org.cthul.miro.sql.set;

import org.cthul.miro.request.impl.SimpleRequestComposer;
import java.util.function.Consumer;
import org.cthul.miro.request.RequestComposer;
import org.cthul.miro.request.impl.SnippetTemplateLayer;
import org.cthul.miro.request.template.Snippets;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.request.template.TemplateLayerStack;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.set.base.AbstractQuerySet;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.layer.MappedQuery;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class SqlEntitySet<Entity, This extends SqlEntitySet<Entity, This>> extends AbstractQuerySet<Entity, SelectQuery, This> {

    private final TemplateLayer<MappedQuery<Entity, SelectQuery>> queryLayer;
    private final SnippetTemplateLayer<MappedQuery<Entity, SelectQuery>> snippetLayer;
    private Template<MappedQuery<Entity, SelectQuery>> template = null;

    public SqlEntitySet(MiConnection cnn, MappedSqlType<Entity> type) {
        this(cnn, type.getSelectLayer());
    }

    public SqlEntitySet(MiConnection cnn, TemplateLayer<MappedQuery<Entity, SelectQuery>> queryLayer) {
        super(cnn, SqlDQML.select());
        this.queryLayer = queryLayer;
        this.snippetLayer = Snippets.newLayer();
    }

    protected SqlEntitySet(SqlEntitySet<Entity, This> source) {
        super(source);
        this.queryLayer = source.queryLayer;
        this.template = source.template;
        this.snippetLayer = Snippets.newLayer();
    }
    
    private Snippets.Key<MappedQuery<Entity, SelectQuery>> snippetKey() {
        return Snippets.key();
    }
    
    protected This snippet(String key, Object... args) {
        return compose(c -> c.node(snippetKey()).set(key, args));
    }
    
    protected This snippet(Consumer<? super MappedQuery<Entity, SelectQuery>> snippet) {
        return compose(c -> c.node(snippetKey()).add(snippet));
    }
    
//    protected This once(Consumer<? super SelectBuilder> snippet) {
//        return compose(c -> c.node(snippetKey()).once(snippet));
//    }

    protected This sql(Consumer<? super SelectBuilder> action) {
        return snippet(snp -> action.accept(snp.getStatement()));
    }
    
    protected This sql(String sql, Object... args) {
        return snippet(snp -> snp.getStatement().sql(sql, args));
    }
    
    protected This map(Consumer<? super Mapping<Entity>> action) {
        return snippet(snp -> action.accept(snp.getMapping()));
    }
    
    protected This configureWith(EntityConfiguration<? super Entity> cfg) {
        return map(m -> m.configureWith(cfg));
    }
    
    protected This initializeWith(EntityInitializer<? super Entity> cfg) {
        return map(m -> m.initializeWith(cfg));
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        initializeSnippetLayer(snippetLayer);
        TemplateLayerStack<MappedQuery<Entity, SelectQuery>> stack = new TemplateLayerStack<>();
        intializeTemplateStack(stack);
        template = stack.build();
    }
    
    protected void initializeSnippetLayer(SnippetTemplateLayer<MappedQuery<Entity, SelectQuery>> snippetLayer) {
    }
    
    protected void intializeTemplateStack(TemplateLayerStack<MappedQuery<Entity, SelectQuery>> stack) {
        stack.push(snippetLayer);
        stack.push(queryLayer);
    }

    @Override
    protected RequestComposer<MappedQuery<Entity, SelectQuery>> newComposer() {
        return new SimpleRequestComposer<>(template);
    }
}
