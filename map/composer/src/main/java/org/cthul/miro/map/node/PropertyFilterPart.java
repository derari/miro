package org.cthul.miro.map.node;

import org.cthul.miro.composer.node.Initializable;
import org.cthul.miro.composer.CopyableNodeSet;
import org.cthul.miro.composer.node.Copyable;
import java.util.*;
import org.cthul.miro.composer.node.*;
import org.cthul.miro.map.AbstractQueryableType;
import org.cthul.miro.map.PropertyFilter;
import org.cthul.miro.map.PropertyFilter.PropertyFilterKey;
import org.cthul.miro.map.PropertyFilterComposer.Internal;
import org.cthul.miro.entity.map.MappedProperty;

/**
 *
 */
public class PropertyFilterPart extends CopyableNodeSet<PropertyFilterKey, Void, ListNode<Object[]>> 
                implements PropertyFilter, Initializable<Internal>, Copyable<Internal> {
    
    private final AbstractQueryableType<?,?> owner;
    private Internal composer = null;

    public PropertyFilterPart(AbstractQueryableType<?, ?> owner) {
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
    public boolean allowReadOriginal() {
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
        
        private final AbstractQueryableType<?,?> owner;
        private final String[] propertyKeys;
        private final List<MappedProperty<?>> properties;
        private ListNode<Object[]> attributeFilter;
        private Object[][] bags = null;
        private Object[] totalBag = null;

        public PropertiesIn(AbstractQueryableType<?,?> owner, String[] properties) {
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
            // FIXME: column names, not property keys
            attributeFilter = composer.getAttributeFilter().forKeys(propertyKeys);
        }

        @Override
        protected Initializable<Internal> copyInstance() {
            return new PropertiesIn(this);
        }

        @Override
        public boolean allowReadOriginal() {
            return true;
        }

        @Override
        public void add(Object[] values) {
            if (bags == null) bags = new Object[properties.size()][];
            int totalLength = 0;
            for (int i = 0; i < bags.length; i++) {
                MappedProperty<?> at = properties.get(i);
                Object v = values[i];
                bags[i] = at.getMapping().writeColumns(v, 0, bags[i]);
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
