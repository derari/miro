package org.cthul.miro.view.composer;

import java.util.function.Function;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.view.impl.NoTemplates;

/**
 *
 */
public interface CRUDTemplateLayer<CB, RB, UB, DB, CB2, RB2, UB2, DB2>
                 extends LayerBuilder<CB, RB, UB, DB, CB2, RB2, UB2, DB2> {
    
    Template<? super CB> insertTemplate(Template<? super CB2> template);
    
    Template<? super RB> selectTemplate(Template<? super RB2> template);
    
    Template<? super UB> updateTemplate(Template<? super UB2> template);
    
    Template<? super DB> deleteTemplate(Template<? super DB2> template);

    @Override
    default CRUDTemplateLayer<CB, RB, UB, DB, CB2, RB2, UB2, DB2> asLayer() {
        return this;
    }

    @Override
    default CRUDTemplates<CB, RB, UB, DB> asTemplates() {
        return wrap(NoTemplates.getInstance());
    }
    
    default CRUDTemplates<CB, RB, UB, DB> wrap(CRUDTemplates<? super CB2, RB2, UB2, DB2> templates) {
        return templates.adapt(this);
    }
    
    default <CB3, RB3, UB3, DB3> CRUDTemplateLayer<CB, RB, UB, DB, CB3, RB3, UB3, DB3> wrap(CRUDTemplateLayer<? super CB2, RB2, UB2, DB2, CB3, RB3, UB3, DB3> layer) {
        CRUDTemplateLayer<CB, RB, UB, DB, CB2, RB2, UB2, DB2> self = this;
        return new CRUDTemplateLayer<CB, RB, UB, DB, CB3, RB3, UB3, DB3>() {
            @Override
            public Template<? super CB> insertTemplate(Template<? super CB3> template) {
                return self.insertTemplate(layer.insertTemplate(template));
            }
            @Override
            public Template<? super RB> selectTemplate(Template<? super RB3> template) {
                return self.selectTemplate(layer.selectTemplate(template));
            }
            @Override
            public Template<? super UB> updateTemplate(Template<? super UB3> template) {
                return self.updateTemplate(layer.updateTemplate(template));
            }
            @Override
            public Template<? super DB> deleteTemplate(Template<? super DB3> template) {
                return self.deleteTemplate(layer.deleteTemplate(template));
            }
            @Override
            public CRUDTemplates<CB, RB, UB, DB> wrap(CRUDTemplates<? super CB3, RB3, UB3, DB3> templates) {
                return self.wrap(layer.wrap(templates));
            }
        };
    }
    
    default <CB0, RB0, UB0, DB0> 
                    CRUDTemplateLayer<CB0, RB0, UB0, DB0, CB2, RB2, UB2, DB2>
                    adapt(Function<CB0, CB> insertAdapter,
                        Function<RB0, RB> selectAdapter,
                        Function<UB0, UB> updateAdapter,
                        Function<DB0, DB> deleteAdapter) {
        return adapter(insertAdapter, selectAdapter, updateAdapter, deleteAdapter)
                .wrap(this);
    }
    
    static <CB, RB, UB, DB, CB2, RB2, UB2, DB2> 
                CRUDTemplateLayer<CB, RB, UB, DB, CB2, RB2, UB2, DB2>
                adapter(Function<CB, CB2> insertAdapter,
                        Function<RB, RB2> selectAdapter,
                        Function<UB, UB2> updateAdapter,
                        Function<DB, DB2> deleteAdapter) {
        return new CRUDTemplateLayer<CB, RB, UB, DB, CB2, RB2, UB2, DB2>() {
            @Override
            public Template<? super CB> insertTemplate(Template<? super CB2> template) {
                return template.adapt(insertAdapter);
            }
            @Override
            public Template<? super RB> selectTemplate(Template<? super RB2> template) {
                return template.adapt(selectAdapter);
            }
            @Override
            public Template<? super UB> updateTemplate(Template<? super UB2> template) {
                return template.adapt(updateAdapter);
            }
            @Override
            public Template<? super DB> deleteTemplate(Template<? super DB2> template) {
                return template.adapt(deleteAdapter);
            }
        };
    }
            
}
