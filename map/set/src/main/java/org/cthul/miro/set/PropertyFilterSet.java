package org.cthul.miro.set;

import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.composer.node.StatementPart;
import org.cthul.miro.map.PropertyFilter;
import org.cthul.miro.map.PropertyFilterComposer;
import org.cthul.miro.map.node.PropertyFilterComposerRecorder;

public class PropertyFilterSet<This extends PropertyFilterSet<This>>
        extends AbstractComposable<PropertyFilterComposer, This> {

    public PropertyFilterSet() {
    }

    public PropertyFilterSet(PropertyFilterSet<This> source) {
        super(source);
    }

    @Override
    protected PropertyFilterComposer newComposer() {
        return PropertyFilterComposerRecorder.create();
    }
    
    protected This putFilter(String key, Object value) {
        return setUp(PROPERTY_FILTER, pf -> pf.put(key, value));
    }
    
    protected This putFilter(Map<String, Object> map) {
        return setUp(PROPERTY_FILTER, pf -> pf.put(map));
    }
    
    protected AddValues<This> filterProperties(String... keys) {
        return args -> {
            return setUp(PROPERTY_FILTER, pf -> pf.forProperties(keys).add(args));
        };
    }
    
    protected void addFiltersTo(PropertyFilter propertyFilter) {
        ((StatementPart<PropertyFilter>) getComposer()).addTo(propertyFilter);
    }
    
    protected static final Function<PropertyFilterComposer, PropertyFilter> PROPERTY_FILTER = PropertyFilterComposer::getPropertyFilter;
    
    public static interface AddValues<T> {
        
        T add(Object... values);
    }
}
