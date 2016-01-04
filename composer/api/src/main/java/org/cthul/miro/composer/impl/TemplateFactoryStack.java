package org.cthul.miro.composer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.cthul.miro.composer.Template;

/**
 *
 * @param <Builder>
 */
public class TemplateFactoryStack<Builder> {
    
    private final List<Function<? super Template<? super Builder>, ? extends Template<? super Builder>>> factories = new ArrayList<>();
    
    public TemplateFactoryStack<Builder> push(Function<? super Template<? super Builder>, ? extends Template<? super Builder>> newTemplate) {
        factories.add(newTemplate);
        return this;
    }

    public TemplateStack<Builder> asTemplate() {
        return buildTemplate(new TemplateStack<>());
    }
    
    public TemplateStack<Builder> asTemplate(Template<? super Builder> parent) {
        return buildTemplate(new TemplateStack<>(parent));
    }

    protected TemplateStack<Builder> buildTemplate(TemplateStack<Builder> stack) {
        factories.forEach(f -> stack.push(f));
        return stack;
    }
}
