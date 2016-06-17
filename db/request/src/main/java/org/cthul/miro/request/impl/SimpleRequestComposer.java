package org.cthul.miro.request.impl;

import org.cthul.miro.request.RequestComposer;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.TemplateLayer;

/**
 *
 * @param <Builder>
 */
public class SimpleRequestComposer<Builder> extends AbstractComposer<Builder> implements RequestComposer<Builder> {
    
    public SimpleRequestComposer(TemplateLayer<? super Builder> layer) {
        super(layer.build());
    }
    
    public SimpleRequestComposer(Template<? super Builder> template) {
        super(template);
    }

    protected SimpleRequestComposer(AbstractComposer<Builder> source) {
        super(source);
    }

    @Override
    public void build(Builder builder) {
        super.build(builder);
    }

    @Override
    public RequestComposer<Builder> copy() {
        return new SimpleRequestComposer<>(this);
    }
}
