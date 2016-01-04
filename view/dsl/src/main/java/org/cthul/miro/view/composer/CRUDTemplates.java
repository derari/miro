package org.cthul.miro.view.composer;

import org.cthul.miro.composer.Template;

/**
 *
 * @param <CB>
 * @param <RB>
 * @param <UB>
 * @param <DB>
 */
public interface CRUDTemplates<CB, RB, UB, DB> {
    
    Template<? super CB> insertTemplate();
    
    Template<? super RB> selectTemplate();
    
    Template<? super UB> updateTemplate();
    
    Template<? super DB> deleteTemplate();
    
    default <CB2, RB2, UB2, DB2> CRUDTemplates<CB2, RB2, UB2, DB2> adapt(CRUDTemplateLayer<CB2, RB2, UB2, DB2, ? extends CB, ? extends RB, ? extends UB, ? extends DB> adapter) {
        return new CRUDTemplates<CB2, RB2, UB2, DB2>() {
            @Override
            public Template<? super CB2> insertTemplate() {
                return adapter.insertTemplate(CRUDTemplates.this.insertTemplate());
            }
            @Override
            public Template<? super RB2> selectTemplate() {
                return adapter.selectTemplate(CRUDTemplates.this.selectTemplate());
            }
            @Override
            public Template<? super UB2> updateTemplate() {
                return adapter.updateTemplate(CRUDTemplates.this.updateTemplate());
            }
            @Override
            public Template<? super DB2> deleteTemplate() {
                return adapter.deleteTemplate(CRUDTemplates.this.deleteTemplate());
            }
        };
    }
}
