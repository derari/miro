package org.cthul.miro.request;

import org.cthul.miro.request.template.TemplateLayer;

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
