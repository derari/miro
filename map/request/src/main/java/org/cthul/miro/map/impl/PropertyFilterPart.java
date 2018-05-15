package org.cthul.miro.map.impl;

import java.util.*;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.map.MappedType;
import org.cthul.miro.map.PropertyFilter;
import org.cthul.miro.map.PropertyFilter.PropertyFilterKey;
import org.cthul.miro.map.PropertyFilterComposer.Internal;
import org.cthul.miro.request.*;
import org.cthul.miro.request.part.ListNode;

/**
 *
 */
public class PropertyFilterPart extends CopyableNodeSet<PropertyFilterKey, Void, ListNode<Object[]>> 
                implements PropertyFilter, Initializable<Internal>, Copyable2<Internal> {
    
    private final MappedType<?,?> owner;
    private Internal composer = null;

    public PropertyFilterPart(MappedType<?, ?> owner) {
        this.owner = owner;
    }

    public PropertyFilterPart(PropertyFilterPart src, Internal composer) {
        super(src);
        this.owner = src.owner;
        this.composer = composer;
    }

    @Override
    public void initialize(Internal composer) {
        this.composer = composer;
    }

    @Override
    public Object copy(Internal composer) {
        return new PropertyFilterPart(this, composer);
    }
    
    @Override
    public boolean allowRead() {
        return true;
    }

    @Override
    public ListNode<Object[]> forProperties(String... propertyKeys) {
        return getValue(new PropertyFilterKey(propertyKeys), null);
    }

    @Override
    protected void newEntry(PropertyFilterKey key, Void hint) {
        putNode(key, new PropertiesIn(owner, key.getAttributeKeys()));
    }

    @Override
    protected Object getInitializationArg() {
        return composer;
    }
    
    protected static class PropertiesIn extends CopyInitializable<Internal> implements ListNode<Object[]> {
        
        private final MappedType<?,?> owner;
        private final String[] propertyKeys;
        private final List<EntityAttribute<?, GraphApi>> properties;
        private ListNode<Object[]> attributeFilter;
        private Object[][] bags = null;
        private Object[] totalBag = null;

        public PropertiesIn(MappedType<?,?> owner, String[] properties) {
            this.owner = owner;
            this.propertyKeys = properties;
            this.properties = new ArrayList<>();
            for (String p: properties) {
                this.properties.add(owner.getAttributes().getAttributeMap().get(p));
            }
        }
        
        public PropertiesIn(PropertiesIn source) {
            this.owner = source.owner;
            this.propertyKeys = source.propertyKeys;
            this.properties = source.properties;
            this.bags = source.bags;
            source.bags = null;
            this.totalBag = source.totalBag;
            source.totalBag = null;
        }

        @Override
        public void initialize(Internal composer) {
            attributeFilter = composer.getAttributeFilter().forKeys(propertyKeys);
        }

        @Override
        protected Initializable<Internal> copyInstance() {
            return new PropertiesIn(this);
        }

        @Override
        public boolean allowRead() {
            return true;
        }

        @Override
        public void add(Object[] values) {
            if (bags == null) bags = new Object[properties.size()][];
            int totalLength = 0;
            for (int i = 0; i < bags.length; i++) {
                EntityAttribute<?, GraphApi> at = properties.get(i);
                Object v = values[i];
                bags[i] = at.toColumns(v, bags[i]);
                totalLength += bags[i].length;
            }
            if (totalBag == null) totalBag = new Object[totalLength];
            totalLength = 0;
            for (Object[] bag : bags) {
                System.arraycopy(bag, 0, totalBag, totalLength, bag.length);
                totalLength += bag.length;
            }
            attributeFilter.add(totalBag);
        }
    }
}
