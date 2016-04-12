package org.cthul.miro.composer;

import org.cthul.miro.composer.template.TemplateLayer;

/**
 *
 * @param <Statement>
 */
public interface StatementHolder<Statement> {
    
    Statement getStatement();
    
    static <Statement> TemplateLayer<StatementHolder<? extends Statement>> wrapped(TemplateLayer<Statement> layer) {
        return layer.adapt(StatementHolder::getStatement);
    }
}
