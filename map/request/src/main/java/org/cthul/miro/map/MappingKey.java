package org.cthul.miro.map;

import java.util.Map;
import java.util.function.Supplier;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.request.part.KeyValueNode;
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
    
    /** Adds each property's required columns to the result set. */
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
        
        PROPERTY_FILTER,
        
        NIL;
    }
    
    static interface SetProperty extends KeyValueNode<String, Object> {
        default void set(String key, Object value) {
            set(key, () -> value);
        }
        void set(String key, Supplier<?> value);

        @Override
        public default void put(Map<? extends String, ? extends Object> map) {
            map.entrySet().forEach(e -> set(e.getKey(), e.getValue()));
        }

        @Override
        public default void put(String key, Object value) {
            set(key, value);
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
