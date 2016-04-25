package org.cthul.miro.request.template;

import java.util.function.Function;
import org.cthul.miro.request.impl.AdaptedTemplate;

/**
 * Creates a template on top of another template.
 * @param <Builder>
 */
public interface TemplateLayer<Builder> {
    
    <B extends Builder> Template<B> build(Template<? super B> parent);
    
    default <B extends Builder> Template<B> build() {
        return build(Templates.noOp());
    }
    
    default <Adaptee> TemplateLayer<Adaptee> adapt(Function<? super Adaptee, ? extends Builder> adapter) {
        return AdaptedTemplate.adapt(this, adapter);
    }
}
