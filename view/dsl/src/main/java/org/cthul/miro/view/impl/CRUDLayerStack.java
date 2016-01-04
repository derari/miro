package org.cthul.miro.view.impl;

import org.cthul.miro.composer.Template;
import org.cthul.miro.composer.impl.TemplateFactoryStack;
import org.cthul.miro.view.composer.CRUDTemplateLayer;
import org.cthul.miro.view.composer.SimpleCRUDTemplateLayer;

/**
 *
 */
public class CRUDLayerStack<CB, RB, UB, DB> implements SimpleCRUDTemplateLayer<CB, RB, UB, DB> {

    public CRUDLayerStack() {
    }

    private final TemplateFactoryStack<CB> insertStack = new TemplateFactoryStack<>();
    private final TemplateFactoryStack<RB> selectStack = new TemplateFactoryStack<>();
    private final TemplateFactoryStack<UB> updateStack = new TemplateFactoryStack<>();
    private final TemplateFactoryStack<DB> deleteStack = new TemplateFactoryStack<>();
    
    public CRUDLayerStack<CB, RB, UB, DB> push(CRUDTemplateLayer<? super CB, ? super RB, ? super UB, ? super DB, CB, RB, UB, DB> layer) {
        insertStack.push(layer::insertTemplate);
        selectStack.push(layer::selectTemplate);
        updateStack.push(layer::updateTemplate);
        deleteStack.push(layer::deleteTemplate);
        return this;
    }

    @Override
    public Template<? super CB> insertTemplate(Template<? super CB> template) {
        return insertStack.asTemplate(template);
    }

    @Override
    public Template<? super RB> selectTemplate(Template<? super RB> template) {
        return selectStack.asTemplate(template);
    }

    @Override
    public Template<? super UB> updateTemplate(Template<? super UB> template) {
        return updateStack.asTemplate(template);
    }

    @Override
    public Template<? super DB> deleteTemplate(Template<? super DB> template) {
        return deleteStack.asTemplate(template);
    }
}
