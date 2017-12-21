package org.cthul.miro.map.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.map.MappedType;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.PropertyFilter;
import org.cthul.miro.map.PropertyFilter.PropertyFilterKey;
import org.cthul.miro.request.Composer;
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
    
    protected class PropertyFilterHub implements PropertyFilter, Copyable {
        
        final Composer composer;

        public PropertyFilterHub(Composer ic) {
            this.composer = ic;
        }

        @Override
        public ListNode<Object[]> forProperties(String... propertyKeys) {
            return composer.node(new PropertyFilterKey(propertyKeys));
        }

        @Override
        public Object copyFor(CopyComposer cc) {
            return new PropertyFilterHub(cc);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
    
    protected class PropertiesIn implements ListNode<Object[]>, Copyable {
        
        final List<EntityAttribute<Entity, GraphApi>> properties;
        final Key<ListNode<Object[]>> valueFilterKey;
        final ListNode<Object[]> valueFilter;
        Object[][] bags = null;
        Object[] totalBag = null;

        public PropertiesIn(Composer ic, String[] properties) {
            this.properties = new ArrayList<>();
            for (String p: properties) {
                this.properties.add(getOwner().getAttributes().getAttributeMap().get(p));
            }
            this.valueFilterKey = getOwner().getValueFilterKey(properties);
            this.valueFilter = ic.node(valueFilterKey);
        }
        
        public PropertiesIn(Composer ic, PropertiesIn source) {
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
        public Object copyFor(CopyComposer cc) {
            bags = null;
            totalBag = null;
            return new PropertiesIn(cc, this);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true; // write-only node
        }
    }
}
