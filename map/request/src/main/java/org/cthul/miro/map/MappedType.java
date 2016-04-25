package org.cthul.miro.map;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.request.template.TemplateLayerStack;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.impl.AbstractTypeBuilder;
import org.cthul.miro.map.layer.FilterLayer;
import org.cthul.miro.map.layer.GenericMappingLayer;
import org.cthul.miro.map.layer.MaterializationLayer;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class MappedType<Entity, This extends MappedType<Entity, This>> extends AbstractTypeBuilder<Entity, This> {

    private TemplateLayer<Mapping<Entity>> materializationLayer = null;
    
    public MappedType(Class<Entity> clazz) {
        super(clazz);
    }

    public MappedType(Class<Entity> clazz, Object shortString) {
        super(clazz, shortString);
    }
    

    public TemplateLayer<Mapping<Entity>> getMaterializationLayer() {
        if (materializationLayer == null) {
            materializationLayer = TemplateLayerStack.join(
                new GenericMappingLayer<>(entityClass(), this),
                new FilterLayer<>(this),
                new MaterializationLayer<>(this));
        }
        return materializationLayer;
    }

    public Key<ListNode<Object[]>> getValueFilterKey(String[] properties) {
        List<String> columns = new ArrayList<>();
        for (String p: properties) {
            EntityAttribute<?> at = getAttributes().getAttributeMap().get(p);
            columns.addAll(at.getColumns());
        }
        return getColumnFilterKey(columns);
    }
    
    protected abstract Key<ListNode<Object[]>> getColumnFilterKey(List<String> columns);

    public abstract Key<ListNode<String>> getResultColumnsKey();
}
