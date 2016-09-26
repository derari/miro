package org.cthul.miro.map.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.map.MappedType;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.PropertyFilter;
import org.cthul.miro.map.PropertyFilter.PropertyFilterKey;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Entity>
 */
public class FilterLayer<Entity> extends AbstractMappingLayer<Entity, Object> {

    public FilterLayer(MappedType<Entity, ?> owner) {
        super(owner);
    }

    @Override
    protected Template<? super Object> createPartTemplate(Parent<Object> parent, Object key) {
        switch (MappingKey.key(key)) {
            case PROPERTY_FILTER:
                return Templates.newNode(PropertyFilterHub::new);
        }
        if (key instanceof PropertyFilterKey) {
            PropertyFilterKey pfk = (PropertyFilterKey) key;
            return Templates.newNode(ic -> new PropertiesIn(ic, pfk.getAttributeKeys()));
        }
        return null;
    }
    
    protected class PropertyFilterHub implements PropertyFilter, Copyable<Object> {
        
        final InternalComposer<?> ic;

        public PropertyFilterHub(InternalComposer<?> ic) {
            this.ic = ic;
        }

        @Override
        public ListNode<Object[]> forProperties(String... propertyKeys) {
            return ic.node(new PropertyFilterKey(propertyKeys));
        }

        @Override
        public Object copyFor(InternalComposer<Object> ic) {
            return new PropertyFilterHub(ic);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
    
    protected class PropertiesIn implements ListNode<Object[]>, Copyable<Object> {
        
        final List<EntityAttribute<Entity, GraphApi>> properties;
        final Key<ListNode<Object[]>> valueFilterKey;
        final ListNode<Object[]> valueFilter;
        Object[][] bags = null;
        Object[] totalBag = null;

        public PropertiesIn(InternalComposer<?> ic, String[] properties) {
            this.properties = new ArrayList<>();
            for (String p: properties) {
                this.properties.add(getOwner().getAttributes().getAttributeMap().get(p));
            }
            this.valueFilterKey = getOwner().getValueFilterKey(properties);
            this.valueFilter = ic.node(valueFilterKey);
        }
        
        public PropertiesIn(InternalComposer<?> ic, PropertiesIn source) {
            this.properties = source.properties;
            this.valueFilterKey = source.valueFilterKey;
            this.valueFilter = ic.node(valueFilterKey);
        }

        @Override
        public void add(Object[] values) {
            if (bags == null) bags = new Object[properties.size()][];
            int total = 0;
            for (int i = 0; i < bags.length; i++) {
                EntityAttribute<Entity, GraphApi> at = properties.get(i);
                Object v = values[i];
                bags[i] = at.toColumns(v, bags[i]);
                total += bags[i].length;
            }
            if (totalBag == null) totalBag = new Object[total];
            total = 0;
            for (Object[] bag : bags) {
                System.arraycopy(bag, 0, totalBag, total, bag.length);
                total += bag.length;
            }
            valueFilter.add(totalBag);
        }

        @Override
        public Object copyFor(InternalComposer<Object> ic) {
            bags = null;
            totalBag = null;
            return new PropertiesIn(ic, this);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true; // write-only node
        }
    }
}
