package org.cthul.miro.composer.impl;

import java.util.function.Function;
import org.cthul.miro.composer.ComposerParts;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.Template;

/**
 *
 */
public class TemplateStack<Builder> implements Template<Builder> {
    
    private Template<? super Builder> top;

    public TemplateStack() {
        this(ComposerParts.noOp());
    }

    public TemplateStack(Template<? super Builder> parent) {
        this.top = parent;
    }
    
    public TemplateStack<Builder> push(Function<? super Template<? super Builder>, ? extends Template<? super Builder>> newTemplate) {
        top = newTemplate.apply(top);
        return this;
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
