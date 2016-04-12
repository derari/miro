package org.cthul.miro.set.msql;

import org.cthul.miro.composer.impl.SimpleRequestComposer;
import java.util.function.Consumer;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.StatementHolder;
import org.cthul.miro.composer.impl.SnippetTemplateLayer;
import org.cthul.miro.composer.template.Snippets;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.template.TemplateLayerStack;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.db.sql.SqlDQML;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.map.impl.QueryableEntitySet;
import org.cthul.miro.result.Results;
import org.cthul.miro.set.base.AbstractQuerySet;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.map.Mapping;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class SqlEntitySet<Entity, This extends SqlEntitySet<Entity, This>> extends AbstractQuerySet<Entity, SelectBuilder, This> {

    private final SnippetTemplateLayer<SelectBuilder> snippetLayer;
    private Template<MappedStatement<Entity, ? extends SelectBuilder>> template = null;

    public SqlEntitySet(QueryableEntitySet<Entity> entitySet) {
        super(entitySet);
        snippetLayer = Snippets.newLayer();
    }

    protected SqlEntitySet(SqlEntitySet<Entity, This> source) {
        super(source);
        this.snippetLayer = source.snippetLayer;
        this.template = source.template;
    }
    
    private Snippets.Key<SelectBuilder> snippetKey() {
        return Snippets.key();
    }
    
    private Mapping.Key<Entity> mappingKey() {
        return Mapping.key();
    }
    
    protected This snippet(String key, Object... args) {
        return compose(c -> c.node(snippetKey()).set(key, args));
    }
    
    protected This snippet(Consumer<? super SelectBuilder> snippet) {
        return compose(c -> c.node(snippetKey()).add(snippet));
    }
    
//    protected This once(Consumer<? super SelectBuilder> snippet) {
//        return compose(c -> c.node(snippetKey()).once(snippet));
//    }

    protected This map(Consumer<Mapping<Entity>> action) {
        return compose(cmp -> action.accept(cmp.node(mappingKey())));
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
        TemplateLayerStack<MappedStatement<Entity, ? extends SelectBuilder>> stack = new TemplateLayerStack<>();
        intializeTemplateStack(stack);
        template = stack.build();
    }
    
    protected void initializeSnippetLayer(SnippetTemplateLayer<SelectBuilder> snippetLayer) {
    }
    
    protected void intializeTemplateStack(TemplateLayerStack<MappedStatement<Entity, ? extends SelectBuilder>> stack) {
        stack.push(StatementHolder.wrapped(snippetLayer));
        stack.push(MappedStatement.wrapped(Mapping.<Entity>newLayer()));
    }
    
    @Override
    protected RequestComposer<MappedStatement<Entity, ? extends SelectBuilder>> newComposer() {
        return new SimpleRequestComposer<>(template);
    }

    @Override
    protected Results.Action<Entity> result(QueryableEntitySet<Entity> entitySet, RequestComposer<? super MappedStatement<Entity, ? extends SelectBuilder>> composer) {
        return entitySet.query(SqlDQML.select(), composer);
    }
}
