package org.cthul.miro.request.impl;

import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.request.template.Templates;

/**
 *
 * @param <Builder>
 */
public abstract class AbstractTemplateLayer<Builder> implements TemplateLayer<Builder> {

    protected abstract Template<? super Builder> createPartTemplate(Parent<Builder> parent, Object key);

    @Override
    public <B extends Builder> Template<B> build(Template<? super B> parent) {
        return new MyTemplate(parent);
    }

    @Override
    public String toString() {
        String id = Integer.toString(System.identityHashCode(this), Character.MAX_RADIX);
        if (id.length() < 6) id = "000000".substring(id.length()) + id;
        return getShortString() + "@" + id;
    }
    
    protected String getShortString() {
        return getClass().getSimpleName();
    }
    
    protected String getLayerString() {
        return getShortString();
    }
    
    protected class MyTemplate<B extends Builder> extends AbstractTemplate<B> {
        
        private final Parent parentAccess;

        public MyTemplate(Template<? super B> parent) {
            super(parent);
            parentAccess = parent::addTo;
        }

        @Override
        protected Template<? super B> createPartTemplate(Object key) {
            return AbstractTemplateLayer.this.createPartTemplate(parentAccess, key);
        }

        @Override
        protected String getShortString() {
            return AbstractTemplateLayer.this.getShortString();
        }
    }
    
    public static interface Parent<B> extends Templates.ComposableTemplate<B> { }
}
