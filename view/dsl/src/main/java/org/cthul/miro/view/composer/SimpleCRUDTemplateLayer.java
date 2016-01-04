package org.cthul.miro.view.composer;

import org.cthul.miro.view.impl.CRUDTemplatesStack;

/**
 *
 */
public interface SimpleCRUDTemplateLayer<CB, RB, UB, DB> 
            extends CRUDTemplateLayer<CB, RB, UB, DB, CB, RB, UB, DB>,
                    SimpleLayerBuilder<CB, RB, UB, DB> {

    @Override
    default SimpleCRUDTemplateLayer<CB, RB, UB, DB> asLayer() {
        return this;
    }

    @Override
    default CRUDTemplatesStack<CB, RB, UB, DB> asTemplates() {
        return SimpleLayerBuilder.super.asTemplates();
    }
}
