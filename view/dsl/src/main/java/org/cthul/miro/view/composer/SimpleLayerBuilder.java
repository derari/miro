package org.cthul.miro.view.composer;

import org.cthul.miro.view.impl.CRUDTemplatesStack;

/**
 *
 */
public interface SimpleLayerBuilder<CB, RB, UB, DB> extends LayerBuilder<CB, RB, UB, DB, CB, RB, UB, DB> {
    
    @Override
    default CRUDTemplatesStack<CB, RB, UB, DB> asTemplates() {
        return new CRUDTemplatesStack<CB, RB, UB, DB>().push(asLayer());
    }
}
