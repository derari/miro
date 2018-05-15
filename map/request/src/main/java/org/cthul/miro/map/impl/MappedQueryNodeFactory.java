package org.cthul.miro.map.impl;

import java.util.LinkedHashSet;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.map.*;
import org.cthul.miro.request.*;
import org.cthul.miro.request.ComposerState.Behavior;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.part.MultiKeyValueNode;
import static java.util.Arrays.asList;

/**
 *
 */
public class MappedQueryNodeFactory<Entity> implements MappedQueryComposer.Internal, ComposerInternal {
    
    private final MappedType<Entity,?> owner;
    private final Object typeKey;
    private final EntityType<Entity> defaultType;
    
    public MappedQueryNodeFactory(MappedType<Entity, ?> owner) {
        this(owner, owner.entityClass(), owner.asPlainEntityType());
    }
    
    public MappedQueryNodeFactory(MappedType<Entity, ?> owner, Object typeKey, EntityType<Entity> defaultType) {
        this.owner = owner;
        this.typeKey = typeKey;
        this.defaultType = defaultType;
    }
    
    public MappedQueryComposer newComposer() {
//        return ComposerState.newComposer(new Impl(), this);
        return ComposerState.fromFactory(this);// newComposer(null, this);
    }

    protected MappedType<Entity, ?> getOwner() {
        return owner;
    }

    @Override
    public Initializable<MappedQueryComposer.Internal> getAlways() {
        return cmp -> {
            cmp.getType();
        };
    }

    @Override
    public Type<Entity> getType() {
        return new TypePart();
    }

    @Override
    public Configuration<Entity> getConfiguration() {
        return new ConfigurationPart();
    }

    @Override
    public ListNode<String> getLoadedProperties() {
        return new LoadField();
    }

    @Override
    public ListNode<String> getIncludedProperties() {
        return new IncludeProperty();
    }

    @Override
    public ListNode<String> getFetchedProperties() {
        return ListNode.multi((MappedQueryComposer<Entity> c) -> asList(c.getLoadedProperties(), 
                c.getIncludedProperties()
        ));
    }

    @Override
    public SetProperties getSetProperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PropertyFilter getPropertyFilter() {
        return new PropertyFilterPart(owner);
    }

    @Override
    public ListNode<String> getSelectedAttributes() {
        return e -> {};
    }

    @Override
    public MultiKeyValueNode<String, Object> getAttributeFilter() {
        return MultiKeyValueNode.dummy();
    }
    
    protected class TypePart implements Type<Entity>, StatementPart<Mapping<? extends Entity>>, Copyable2<Object> {

        private Graph graph = null;
        private EntityType<? extends Entity> entityType = defaultType;
        private boolean useGraph = false;

        public TypePart() {
        }

        protected TypePart(TypePart source) {
            this.graph = source.graph;
            this.entityType = source.entityType;
            this.useGraph = source.useGraph;
        }
        
        @Override
        public void setGraph(Graph graph) {
            this.graph = graph;
            useGraph = graph != null;
        }

        @Override
        public Graph getGraph() {
            return graph;
        }

        @Override
        public void setType(EntityType<? extends Entity> type) {
            this.entityType = type;
            useGraph = type == null && graph != null;
        }
        
        protected EntityType<? extends Entity> entityType() {
            if (useGraph) {
                if (graph == null) {
                    throw new IllegalStateException("Graph or entity type required");
                }
                if (typeKey == null) {
                    throw new IllegalStateException("Type key required");
                }
                return graph.getEntityType(typeKey);
            } else {
                if (entityType == null && defaultType != null) {
                    // entityType was explicitly set to null
                    throw new IllegalStateException("Graph or entity type required");
                }
                return entityType;
            }
        }

        @Override
        public void addTo(Mapping<? extends Entity> mapping) {
            EntityType et = entityType();
            if (et != null) mapping.setType(et);
        }

        @Override
        public Object copy(Object composer) {
            return new TypePart(this);
        }

        @Override
        public boolean allowRead() {
            return true;
        }
    }
    
    protected class ConfigurationPart
                    implements Configuration<Entity>, StatementPart<Mapping<? extends Entity>>, Copyable2<Object> {
        
        private final LinkedHashSet<EntityConfiguration<? super Entity>> list;

        public ConfigurationPart() {
            this.list = new LinkedHashSet<>();
        }

        public ConfigurationPart(ConfigurationPart source) {
            this.list = new LinkedHashSet<>(source.list);
        }
        
        @Override
        public void configureWith(EntityConfiguration<? super Entity> config) {
            list.add(config);
        }

        @Override
        public void addTo(Mapping<? extends Entity> mapping) {
            list.forEach(cfg -> mapping.configureWith(cfg));
        }

        @Override
        public Object copy(Object composer) {
            return new ConfigurationPart(this);
        }

        @Override
        public boolean allowRead() {
            return true;
        }
    }
    
    protected class LoadField extends CopyInitializable<MappedQueryComposer.Internal>
                    implements ListNode<String>, StatementPart<Mapping<? extends Entity>> {

        private final LinkedHashSet<String> attributes = new LinkedHashSet<>();
        private MappedQueryComposer cmp;

        public LoadField() {
        }

        public LoadField(LoadField src) {
            attributes.addAll(src.attributes);
        }

        @Override
        public void initialize(MappedQueryComposer.Internal composer) {
            this.cmp = composer;
        }
        
        @Override
        public void addTo(Mapping<? extends Entity> mapping) {
            GraphApi graph = (GraphApi) cmp.getType().getGraph();
            EntityConfiguration<Entity> cfg = getOwner().getAttributes().newConfiguration(graph, attributes);
            mapping.configureWith(cfg);
        }

        @Override
        public void add(String attribute) {
            attributes.add(attribute);
        }

        @Override
        protected LoadField copyInstance() {
            return new LoadField(this);
        }

        @Override
        public String toString() {
            return "LOAD " + attributes;
        }
    }
    
    protected class IncludeProperty extends CopyInitializable<MappedQueryComposer.Internal>
                    implements ListNode<String> {

        ListNode<String> resultColumns;
        
        public IncludeProperty() {
        }

        @Override
        public void initialize(MappedQueryComposer.Internal composer) {
            resultColumns = composer.getSelectedAttributes();
        }

        @Override
        public void add(String entry) {
            EntityAttribute<?,GraphApi> at = getOwner().getAttributes().getAttributeMap().get(entry);
            if (at == null) throw new IllegalArgumentException(entry);
            resultColumns.addAll(at.getColumns());
        }

        @Override
        protected IncludeProperty copyInstance() {
            return new IncludeProperty();
        }
    }
    
//    protected static class Impl<Entity> implements MappedQueryComposer.Delegator, Behavior<MappedQueryComposer.Delegator> {
//
//        private MappedQueryComposer.Delegator actual = null;
//
//        @Override
//        public Behavior<MappedQueryComposer.Delegator> copy() {
//            return new Impl<>();
//        }
//
//        @Override
//        public void initialize(MappedQueryComposer.Delegator composer) {
//            actual = composer;
//        }
//
//        @Override
//        public MappedQueryComposer getMappedQueryComposerDelegate() {
//            return actual.getMappedQueryComposerDelegate();
//        }
//
//        @Override
//        public PropertyFilterComposer getPropertyFilterComposerDelegate() {
//            return actual.getPropertyFilterComposerDelegate();
//        }
//    }
}
