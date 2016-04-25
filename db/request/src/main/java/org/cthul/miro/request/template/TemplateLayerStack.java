package org.cthul.miro.request.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @param <Builder>
 */
public class TemplateLayerStack<Builder> implements TemplateLayer<Builder> {
    
    private final List<TemplateLayer<? super Builder>> layers = new ArrayList<>();
    
    public TemplateLayerStack<Builder> pushAll(Iterable<? extends TemplateLayer<? super Builder>> newTemplates) {
        newTemplates.forEach(this::push);
        return this;
    }
    
    public TemplateLayerStack<Builder> push(TemplateLayer<? super Builder> newTemplate) {
        layers.add(newTemplate);
        return this;
    }
    
    public <B extends Builder> TemplateLayerStack<B> and(TemplateLayer<? super B> newTemplate) {
        TemplateLayerStack<B> me = (TemplateLayerStack<B>) this;
        return me.push(newTemplate);
    }

    @Override
    public <B extends Builder> Template<B> build(Template<? super B> parent) {
        return (Template<B>) buildTemplate(new TemplateStack<>(parent)).peek();
    }

    protected <B extends Builder> TemplateStack<B> buildTemplate(TemplateStack<B> stack) {
        layers.forEach(f -> stack.push(f));
        return stack;
    }

    @Override
    public String toString() {
        return layers.stream().map(Object::toString).collect(Collectors.joining(","));
    }
    
    public static <B> TemplateLayerStack<B> join(TemplateLayer<? super B>... templates) {
        return join(Arrays.asList(templates));
    }
    
    public static <B> TemplateLayerStack<B> join(Collection<? extends TemplateLayer<? super B>> templates) {
        TemplateLayerStack<B> stack = new TemplateLayerStack<>();
        templates.forEach(stack::push);
        return stack;
    }
}
