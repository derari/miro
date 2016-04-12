package org.cthul.miro.map;

import org.cthul.miro.composer.StatementHolder;
import org.cthul.miro.composer.template.TemplateLayer;

/**
 *
 * @param <Entity>
 * @param <Statement>
 */
public interface MappedStatement<Entity, Statement>
                 extends StatementHolder<Statement> {

    Mapping<Entity> getMapping();
    
    @Override
    Statement getStatement();
    
    static <Entity, Statement> TemplateLayer<MappedStatement<Entity, ? extends Statement>> wrapped(TemplateLayer<? super Mapping<Entity>> layer) {
        return layer.adapt(MappedStatement::getMapping);
    }
}
