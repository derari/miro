package org.cthul.miro.composer.impl;

import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.template.Template;

/**
 *
 * @param <Builder>
 */
public class SimpleRequestComposer<Builder> extends AbstractComposer<Builder> implements RequestComposer<Builder> {
    
    public SimpleRequestComposer(Template<? super Builder> template) {
        super(template);
    }

    public SimpleRequestComposer(AbstractComposer<Builder> source) {
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
