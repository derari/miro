package org.cthul.miro.view.composer;

/**
 *
 */
public interface LayerBuilder<CB, RB, UB, DB, CB2, RB2, UB2, DB2> {
    
    CRUDTemplateLayer<CB, RB, UB, DB, CB2, RB2, UB2, DB2> asLayer();
                    
    default CRUDTemplates<CB, RB, UB, DB> asTemplates() {
        return asLayer().asTemplates();
    }
}
