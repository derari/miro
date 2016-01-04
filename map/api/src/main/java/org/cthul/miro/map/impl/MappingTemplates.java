package org.cthul.miro.map.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.Template;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.base.AttributeMapping;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.map.MappingTemplateLayer;
import org.cthul.miro.view.composer.SimpleCRUDTemplateLayer;

/**
 *
 * @param <Entity>
 */
public class MappingTemplates<Entity> implements MappingTemplateLayer<Entity, MappingTemplates<Entity>> {
    
    private final Map<String, Function<Entity, ?>> getters = new HashMap<>();
    private final Map<String, BiConsumer<Entity, Object>> setters = new HashMap<>();

    public Map<String, BiConsumer<Entity, Object>> getSetters() {
        return setters;
    }

    @Override
    public <CS, RS, US, DS> SimpleCRUDTemplateLayer<MappedStatementBuilder<Entity, ? extends CS>, MappedStatementBuilder<Entity, ? extends RS>, MappedStatementBuilder<Entity, ? extends US>, MappedStatementBuilder<Entity, ? extends DS>> asLayer() {
        return new Templates();
    }

    @Override
    public <F> MappingTemplates<Entity> field(String id, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
        getters.put(id, getter);
        setters.put(id, (BiConsumer) setter);
        return this;
    }
    
    public EntityConfiguration<Entity> attributeConfiguration(Iterable<String> attributes) {
        AttributeMapping<Entity> mapping = AttributeMapping.build();
        for (String s: attributes) {
            BiConsumer<Entity, Object> setter = setters.get(s);
            mapping.required(s, (e, rs, i) -> setter.accept(e, rs.get(i)));
        }
        return mapping;
    }
    
    class Templates<CS, RS, US, DS> 
                    implements SimpleCRUDTemplateLayer<
                                    MappedStatementBuilder<Entity, CS>, 
                                    MappedStatementBuilder<Entity, RS>,
                                    MappedStatementBuilder<Entity, US>,
                                    MappedStatementBuilder<Entity, DS>> {

        @Override
        public Template<? super MappedStatementBuilder<Entity, CS>> insertTemplate(Template<? super MappedStatementBuilder<Entity, CS>> template) {
            return template;
        }

        @Override
        public Template<? super MappedStatementBuilder<Entity, RS>> selectTemplate(Template<? super MappedStatementBuilder<Entity, RS>> template) {
            return new MappedSelectTemplate<>(MappingTemplates.this, template);
        }

        @Override
        public Template<? super MappedStatementBuilder<Entity, US>> updateTemplate(Template<? super MappedStatementBuilder<Entity, US>> template) {
            return template;
        }

        @Override
        public Template<? super MappedStatementBuilder<Entity, DS>> deleteTemplate(Template<? super MappedStatementBuilder<Entity, DS>> template) {
            return template;
        }
    }
}
