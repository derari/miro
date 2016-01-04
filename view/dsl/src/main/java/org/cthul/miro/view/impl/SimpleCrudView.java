package org.cthul.miro.view.impl;

import java.util.List;
import org.cthul.miro.composer.Template;
import org.cthul.miro.view.composer.CRUDStatementFactory;
import org.cthul.miro.view.composer.CRUDTemplates;
import org.cthul.miro.view.ViewCRUD;

/**
 *
 */
public class SimpleCrudView<CB, RB, UB, DB, C, R, U, D> implements ViewCRUD<C, R, U, D> {
    
    private final CRUDStatementFactory<CB, ? extends RB, UB, DB, C, R, U, D> factory;
    private final CRUDTemplates<? super CB, ? super RB, ? super UB, ? super DB> templates;
    private Template<? super CB> insertTemplate;
    private Template<? super RB> selectTemplate;
    private Template<? super UB> updateTemplate;
    private Template<? super DB> deleteTemplate;

    public SimpleCrudView(CRUDStatementFactory<CB, ? extends RB, UB, DB, C, R, U, D> factory, 
                CRUDTemplates<? super CB, ? super RB, ? super UB, ? super DB> templates) {
        this.factory = factory;
        this.templates = templates;
    }

    @Override
    public C insert(List<?> attributes) {
        if (insertTemplate == null) {
            insertTemplate = templates.insertTemplate();
        }
        return factory.insert(insertTemplate, attributes);
    }

    @Override
    public R select(List<?> attributes) {
        if (selectTemplate == null) {
            selectTemplate = templates.selectTemplate();
        }
        return factory.select(selectTemplate, attributes);
    }

    @Override
    public U update(List<?> attributes) {
        if (updateTemplate == null) {
            updateTemplate = templates.updateTemplate();
        }
        return factory.update(templates.updateTemplate(), attributes);
    }

    @Override
    public D delete(List<?> attributes) {
        if (deleteTemplate == null) {
            deleteTemplate = templates.deleteTemplate();
        }
        return factory.delete(templates.deleteTemplate(), attributes);
    }
}
