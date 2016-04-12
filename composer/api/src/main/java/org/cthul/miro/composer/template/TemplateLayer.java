package org.cthul.miro.composer.template;

import java.util.function.Function;
import org.cthul.miro.composer.impl.AdaptedTemplate;

/**
 * Creates a template on top of another template.
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
