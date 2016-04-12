package org.cthul.miro.map.impl;

import java.util.LinkedHashSet;
import org.cthul.miro.composer.Copyable;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.impl.AbstractTemplateLayer;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.template.Templates;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.map.Mapping;

/**
 *
 */
public class GenericMappingLayer<Entity> extends AbstractTemplateLayer<Mapping<? extends Entity>> {

    @Override
    protected Template<? super Mapping<? extends Entity>> createPartTemplate(Parent<Mapping<? extends Entity>> parent, Object key) {
        if (key == Mapping.key()) {
            return Templates.newNodePart(() -> new MappingPart());
        }
        return null;
    }
    
    protected class MappingPart implements Mapping<Entity>, StatementPart<Mapping<? extends Entity>>, Copyable<Object> {
        
        private final LinkedHashSet<EntityConfiguration<? super Entity>> list;

        public MappingPart() {
            this.list = new LinkedHashSet<>();
        }

        public MappingPart(MappingPart source) {
            this.list = source.list;
        }

        @Override
        public void configureWith(EntityConfiguration<? super Entity> config) {
            list.add(config);
        }

        @Override
        public void addTo(Mapping<? extends Entity> builder) {
            list.forEach(cfg -> builder.configureWith(cfg));
        }

        @Override
        public Object copyFor(InternalComposer<Object> iqc) {
            return new MappingPart(this);
        }
    }
}
