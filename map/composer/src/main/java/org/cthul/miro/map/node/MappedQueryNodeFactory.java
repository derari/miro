package org.cthul.miro.map.node;

import org.cthul.miro.composer.node.StatementPart;
import org.cthul.miro.composer.ComposerState;
import org.cthul.miro.composer.node.Initializable;
import org.cthul.miro.composer.ComposerInternal;
import org.cthul.miro.composer.node.CopyInitializable;
import org.cthul.miro.composer.node.Copyable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.map.*;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.util.XBiConsumer;
import org.cthul.miro.util.XConsumer;
import static java.util.Arrays.asList;

/**
 *
 */
public class MappedQueryNodeFactory<Entity> implements MappedQueryComposer, ComposerInternal {
    
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
        return ComposerState.fromFactory(this);
    }

    protected MappedType<Entity, ?> getOwner() {
        return owner;
    }

    @Override
    public Initializable<MappedQueryComposer> getAlways() {
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
        return ListNode.multi((MappedQueryComposer<Entity> c) -> asList(
                c.getLoadedProperties(), 
                c.getIncludedProperties()
        ));
    }

    @Override
    public SetProperties getSetProperties() {
        return new SetField();
    }

    @Override
    public PropertyFilter getPropertyFilter() {
        return new PropertyFilterPart(owner);
    }

//    @Override
//    public ListNode<String> getSelectedAttributes() {
//        return e -> {};
//    }
//
//    @Override
//    public MultiKeyValueNode<String, Object> getAttributeFilter() {
//        return MultiKeyValueNode.dummy();
//    }
    
    protected class TypePart implements Type<Entity>, StatementPart<Mapping<? extends Entity>>, Copyable<Object> {

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
        public boolean allowReadOriginal() {
            return true;
        }
    }
    
    protected class ConfigurationPart
                    implements Configuration<Entity>, StatementPart<Mapping<? extends Entity>>, Copyable<Object> {
        
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
        public boolean allowReadOriginal() {
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
    
    protected class SetField implements SetProperties, 
                        EntityInitializer<Entity>, Copyable<Object>,
                        StatementPart<Mapping<Entity>> {

        final List<XConsumer<Entity, MiException>> setUps = new ArrayList<>();
        
        @Override
        public void set(String key, Supplier<?> value) {
            EntityAttribute<Entity, GraphApi> at = getOwner().getAttributes().getAttributeMap().get(key);
            if (at == null) throw new IllegalArgumentException(key);
            XBiConsumer<Entity, Object, MiException> setter = at::set;
            setUps.add(new XConsumer<Entity, MiException>() {
                @Override
                public void accept(Entity t) throws MiException {
                    setter.accept(t, value.get());
                }
                @Override
                public String toString() {
                    String vStr = String.valueOf(value);
                    int dot = vStr.lastIndexOf('.');
                    if (dot > 0) vStr = vStr.substring(dot);
                    return key + " := " + vStr;
                }
            });
        }

        @Override
        public void addTo(Mapping<Entity> builder) {
            builder.initializeWith(this);
        }

        @Override
        public void apply(Entity entity) throws MiException {
            for (XConsumer<Entity, MiException> c: setUps) {
                c.accept(entity);
            }
        }

        @Override
        public void complete() throws MiException { }

        @Override
        public Object copy(Object composer) {
            SetField copy = new SetField();
            copy.setUps.addAll(setUps);
            return copy;
        }

        @Override
        public boolean allowReadOriginal() {
            return true;
        }

        @Override
        public String toString() {
            return "SET " + setUps;
        }
    }
}
