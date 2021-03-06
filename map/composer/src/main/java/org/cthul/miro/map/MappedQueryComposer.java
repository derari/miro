package org.cthul.miro.map;

import java.util.Map;
import java.util.function.Supplier;
import org.cthul.miro.entity.*;
import org.cthul.miro.composer.node.KeyValueNode;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.domain.EntitySet;

/**
 *
 * @param <Entity>
 */
public interface MappedQueryComposer<Entity> extends PropertyFilterComposer {
    
    Type<Entity> getType();
    
    Configuration<Entity> getConfiguration();
    
    ListNode<String> getLoadedProperties();
    
    ListNode<String> getIncludedProperties();
    
    ListNode<String> getFetchedProperties();
    
    SetProperties getSetProperties();
    
    static interface SetProperties extends KeyValueNode<String, Object> {
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
    
    static interface Type<Entity> {
        
        void setRepository(Repository repository);
        
        void setTemplate(EntityTemplate<? extends Entity> type);
        
        void setEntitySet(EntitySet<? extends Entity> set);
        
        EntitySet<? extends Entity> getEntitySet();
    }
    
    static interface Configuration<Entity> {
        void configureWith(EntityConfiguration<? super Entity> config);

        default void initializeWith(EntityInitializer<? super Entity> init) {
            configureWith(Entities.asConfiguration(init));
        }
    }

    /**
     *
     * @param <Entity>
     */
    interface Internal<Entity> extends MappedQueryComposer<Entity>, PropertyFilterComposer.Internal {

        ListNode<String> getSelectedAttributes();
    }

    /**
     *
     * @param <Entity>
     */
    public static interface Delegator<Entity> extends MappedQueryComposer<Entity>, PropertyFilterComposer.Delegator {

        MappedQueryComposer<Entity> getMappedQueryComposerDelegate();

        @Override
        default PropertyFilterComposer getPropertyFilterComposerDelegate() {
            return getMappedQueryComposerDelegate();
        }

        @Override
        default Type<Entity> getType() {
            return getMappedQueryComposerDelegate().getType();
        }

        @Override
        default Configuration<Entity> getConfiguration() {
            return getMappedQueryComposerDelegate().getConfiguration();
        }

        @Override
        default ListNode<String> getLoadedProperties() {
            return getMappedQueryComposerDelegate().getLoadedProperties();
        }

        @Override
        default ListNode<String> getIncludedProperties() {
            return getMappedQueryComposerDelegate().getIncludedProperties();
        }

        @Override
        default ListNode<String> getFetchedProperties() {
            return getMappedQueryComposerDelegate().getFetchedProperties();
        }

        @Override
        default SetProperties getSetProperties() {
            return getMappedQueryComposerDelegate().getSetProperties();
        }
    }
}
