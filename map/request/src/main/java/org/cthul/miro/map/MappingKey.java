package org.cthul.miro.map;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.impl.ValueKey;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.util.Key;

/**
 *
 * @param <V>
 */
public interface MappingKey<V> extends Key<V> {
    
    static final MappingKey<Type> TYPE = MKey.TYPE;
    
    static final MappingKey<Configuration> CONFIGURATION = MKey.CONFIGURATION;
    
    /** Allows to load field values from the result set. */
    static final MappingKey<ListNode<String>> LOAD = MKey.LOAD;
    
    /** Adds the property's required columns to the result set. */
    static final MappingKey<ListNode<String>> INCLUDE = MKey.INCLUDE;
    
    /** {@link #INCLUDE} + {@link #LOAD} */
    static final MappingKey<ListNode<String>> FETCH = MKey.FETCH;
    
    /** Allows to set fields to given values. */
    static final MappingKey<SetProperty> SET = MKey.SET;
    
//    static final MappingKey<?> LOAD_ALL = MKey.LOAD_ALL;
    
    static final MappingKey<PropertyFilter> PROPERTY_FILTER = MKey.PROPERTY_FILTER;
    
    static MKey key(Object o) {
        return Key.castDefault(o, MKey.NIL);
    }
    
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    static enum MKey implements MappingKey {
        
        TYPE,
        CONFIGURATION,
        
        LOAD,
        INCLUDE,
        FETCH,
        
        SET,
        
        @Deprecated
        LOAD_ALL,
        
        PROPERTY_FILTER,
        
        NIL;
    }
    
    static interface SetProperty {
        default void set(String key, Object value) {
            set(key, () -> value);
        }
        void set(String key, Supplier<?> value);
    }
    
    interface PropertyFilter {
        
        ListNode<Object[]> forProperties(String... propertyKeys);
    }
    
    class PropertyFilterKey extends ValueKey<ListNode<Object[]>> {
        private final String[] propertyKeys;
        public PropertyFilterKey(String... attributeKeys) {
            super(Arrays.stream(attributeKeys).collect(Collectors.joining(",")));
            this.propertyKeys = attributeKeys;
        }

        public String[] getAttributeKeys() {
            return propertyKeys;
        }
    }
    
    static interface Type {
        
        void setGraph(Graph graph);
        
        void setType(EntityType<?> type);
        
        Graph getGraph();
    }
    
    static interface Configuration {
        void configureWith(EntityConfiguration<?> config);

        default void initializeWith(EntityInitializer<?> init) {
            configureWith(EntityTypes.asConfiguration(init));
        }
    }
}
