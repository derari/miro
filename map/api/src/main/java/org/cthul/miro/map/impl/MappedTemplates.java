package org.cthul.miro.map.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.base.AttributeConfiguration;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.MappingBuilder;

/**
 *
 * @param <Entity>
 */
public class MappedTemplates<Entity> implements MappingBuilder<Entity, MappedTemplates<Entity>> {
    
    private final Map<String, Function<Entity, ?>> getters = new HashMap<>();
    private final Map<String, BiConsumer<Entity, Object>> setters = new HashMap<>();
    private final MaterializationLayer<Entity> mLayer = new MaterializationLayer<>(this);

    public Map<String, Function<Entity, ?>> getGetters() {
        return getters;
    }

    public Map<String, BiConsumer<Entity, Object>> getSetters() {
        return setters;
    }

    @Override
    public <F> MappedTemplates<Entity> field(String id, Function<Entity, F> getter, BiConsumer<Entity, F> setter) {
        getters.put(id, getter);
        setters.put(id, (BiConsumer) setter);
        return this;
    }
    
    public TemplateLayer<Mapping<Entity>> getMaterializationLayer() {
        return mLayer;
    }
    
    public EntityConfiguration<Entity> attributeConfiguration(Iterable<String> attributes) {
        AttributeConfiguration<Entity> mapping = AttributeConfiguration.build();
        for (String s: attributes) {
            BiConsumer<Entity, Object> setter = setters.get(s);
            if (setter == null) throw new IllegalArgumentException(s);
            mapping.required(s, (e, rs, i) -> setter.accept(e, rs.get(i)));
        }
        return mapping;
    }
}
