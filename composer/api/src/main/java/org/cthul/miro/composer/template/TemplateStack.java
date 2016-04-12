package org.cthul.miro.composer.template;

import org.cthul.miro.composer.InternalComposer;

/**
 *
 */
public class TemplateStack<Builder> implements Template<Builder> {
    
    private Template<? super Builder> top;

    public TemplateStack() {
        this(Templates.noOp());
    }

    public TemplateStack(Template<? super Builder> parent) {
        this.top = parent;
    }
    
    public TemplateStack<Builder> push(TemplateLayer<? super Builder> newTemplate) {
        top = newTemplate.build(top);
        return this;
    }
    
    public <B extends Builder> TemplateStack<B> and(TemplateLayer<? super B> newTemplate) {
        TemplateStack<B> me = (TemplateStack) this;
        return me.push(newTemplate);
    }

    public Template<? super Builder> peek() {
        return top;
    }
    
    @Override
    public void addTo(Object key, InternalComposer<? extends Builder> query) {
        peek().addTo(key, query);
    }

    @Override
    public String toString() {
        return peek().toString();
    }
}
