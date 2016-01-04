package org.cthul.miro.view.impl;

import org.cthul.miro.composer.Template;
import org.cthul.miro.composer.impl.TemplateStack;
import org.cthul.miro.view.composer.CRUDTemplateLayer;
import org.cthul.miro.view.composer.CRUDTemplates;

/**
 *
 */
public class CRUDTemplatesStack<CB, RB, UB, DB> implements CRUDTemplates<CB, RB, UB, DB> {

    public CRUDTemplatesStack() {
    }

    private final TemplateStack<CB> insertTemplate = new TemplateStack<>();
    private final TemplateStack<RB> selectTemplate = new TemplateStack<>();
    private final TemplateStack<UB> updateTemplate = new TemplateStack<>();
    private final TemplateStack<DB> deleteTemplate = new TemplateStack<>();
    
    public CRUDTemplatesStack<CB, RB, UB, DB> push(CRUDTemplateLayer<CB, RB, UB, DB, CB, RB, UB, DB> layer) {
        insertTemplate.push(layer::insertTemplate);
        selectTemplate.push(layer::selectTemplate);
        updateTemplate.push(layer::updateTemplate);
        deleteTemplate.push(layer::deleteTemplate);
        return this;
    }

    @Override
    public Template<? super CB> insertTemplate() {
        return insertTemplate.peek();
    }

    @Override
    public Template<? super RB> selectTemplate() {
        return selectTemplate.peek();
    }

    @Override
    public Template<? super UB> updateTemplate() {
        return updateTemplate.peek();
    }

    @Override
    public Template<? super DB> deleteTemplate() {
        return deleteTemplate.peek();
    }
}
