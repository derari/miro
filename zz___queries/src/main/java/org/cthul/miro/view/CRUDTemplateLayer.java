package org.cthul.miro.view;

/**
 *
 */
public interface CRUDTemplateLayer<C, R, U, D> {
    
    
    default ViewCRUD<C,R,U,D> wrap(ViewCRUD<C,R,U,D> view) {
        return null;
    }
}
