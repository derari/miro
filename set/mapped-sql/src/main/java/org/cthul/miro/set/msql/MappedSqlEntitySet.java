package org.cthul.miro.set.msql;

import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.composer.template.TemplateLayerStack;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.map.impl.QueryableEntitySet;
import org.cthul.miro.map.MappedStatement;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class MappedSqlEntitySet<Entity, This extends MappedSqlEntitySet<Entity, This>> extends SqlEntitySet<Entity, This> {

    private final TemplateLayer<? super MappedStatement<Entity, ? extends SelectBuilder>> templateLayer;
    
    public MappedSqlEntitySet(QueryableEntitySet<Entity> entitySet, TemplateLayer<? super MappedStatement<Entity, ? extends SelectBuilder>> templateLayer) {
        super(entitySet);
        this.templateLayer = templateLayer;
    }

    protected MappedSqlEntitySet(MappedSqlEntitySet<Entity, This> source) {
        super(source);
        this.templateLayer = source.templateLayer;
    }
    
    @Override
    protected void intializeTemplateStack(TemplateLayerStack<MappedStatement<Entity, ? extends SelectBuilder>> stack) {
        super.intializeTemplateStack(stack);
        if (templateLayer != null) {
            stack.push(templateLayer);
        }
    }
}
