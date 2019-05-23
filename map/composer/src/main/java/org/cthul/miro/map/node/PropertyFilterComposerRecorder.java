package org.cthul.miro.map.node;

import java.util.Arrays;
import java.util.Map;
import org.cthul.miro.composer.node.MultiKeyValueMapRecorder;
import java.util.function.Function;
import org.cthul.miro.composer.AbstractComposer;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.map.PropertyFilter;
import org.cthul.miro.map.PropertyFilterComposer;

public class PropertyFilterComposerRecorder<Builder> extends AbstractComposer<Builder, PropertyFilter, PropertyFilterComposer> implements PropertyFilterComposer {
    
    public static PropertyFilterComposerRecorder<PropertyFilter> create() {
        return new PropertyFilterComposerRecorder<>(Function.identity());
    }
    
    private static final NodeKey PROPERTIES_IN = newIndex().key();

    public PropertyFilterComposerRecorder(Function<? super Builder, ? extends PropertyFilter> builderAdapter) {
        super(1, null, builderAdapter);
        putNode(PROPERTIES_IN, new PropertyFilterRecorder());
    }

    public PropertyFilterComposerRecorder(PropertyFilterComposerRecorder<Builder> src, Function<? super Builder, ? extends PropertyFilter> builderAdapter) {
        super(src, builderAdapter);
    }

    @Override
    protected Object copy(Function<?, ? extends PropertyFilter> builderAdapter) {
        return new PropertyFilterComposerRecorder(this, builderAdapter);
    }

    @Override
    public PropertyFilter getPropertyFilter() {
        return getNode(PROPERTIES_IN);
    }
    
    protected static class PropertyFilterRecorder extends MultiKeyValueMapRecorder<String, Object> implements PropertyFilter {

        public PropertyFilterRecorder() {
        }

        public PropertyFilterRecorder(MultiKeyValueMapRecorder source) {
            super(source);
        }

        @Override
        protected String[] getKeyArray(Map<? extends String, ? extends Object> map) {
            String[] keys = map.keySet().toArray(new String[map.size()]);
            Arrays.sort(keys);
            return keys;
        }

        @Override
        protected Object[] newValueArray(int size) {
            return new Object[size];
        }

        @Override
        public Object copy(Object composer) {
            return new PropertyFilterRecorder(this);
        }

        @Override
        public ListNode<Object[]> forProperties(String... propertyKeys) {
            return forKeys(propertyKeys);
        }
    }
}
