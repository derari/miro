package org.cthul.miro.map;

import org.cthul.miro.composer.QueryPart;
import org.cthul.miro.composer.template.AbstractTemplate;
import org.cthul.miro.composer.template.InternalQueryComposer;
import org.cthul.miro.composer.template.QueryPartType;
import org.cthul.miro.composer.template.Template;

/**
 *
 */
public class MappedTemplate<Entity, Builder extends EntitySetup<Entity>> extends AbstractTemplate<Builder> {
    
    private final MappedTemplateBuilder builder;

    public MappedTemplate(MappedTemplateBuilder builder, Template<? super Builder> parent) {
        super(parent);
        this.builder = builder;
    }

    @Override
    protected Template<? super Builder> createPartType(Object key) {
        return forKey(key).add(new MappedAttributePart());
//        return null; // new mappedattributetemplate
    }
    
    class MappedAttributeTemplate implements QueryPartType<Builder> {
        @Override
        public QueryPart addTo(Object key, InternalQueryComposer<? extends Builder> query) {
            query.require(MappedTemplate.this.superPartType(key));
            return query.addPart(key, new MappedAttributePart());
        }
    }
    
    class MappedAttributePart implements QueryPart<Builder> {

        @Override
        public void setUp(Object... args) {
            QueryPart.super.setUp(args);
        }

        @Override
        public void addTo(Builder builder) {
            
        }
    }
}
