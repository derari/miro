package org.cthul.miro.view.impl;

import org.cthul.miro.composer.ComposerParts;
import org.cthul.miro.composer.Template;
import org.cthul.miro.view.composer.CRUDTemplates;

/**
 *
 */
public class NoTemplates implements CRUDTemplates<Object, Object, Object, Object> {

    public static final NoTemplates INSTANCE = new NoTemplates();

    public static <C,R,U,D> CRUDTemplates<C,R,U,D> getInstance() {
        return (CRUDTemplates) INSTANCE;
    }
    
    @Override
    public Template<? super Object> insertTemplate() {
        return ComposerParts.noOp();
    }

    @Override
    public Template<? super Object> selectTemplate() {
        return ComposerParts.noOp();
    }

    @Override
    public Template<? super Object> updateTemplate() {
        return ComposerParts.noOp();
    }

    @Override
    public Template<? super Object> deleteTemplate() {
        return ComposerParts.noOp();
    }
}
