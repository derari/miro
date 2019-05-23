package org.cthul.miro.map.node;

import java.util.*;
import org.cthul.miro.composer.node.StatementPart;
import org.cthul.miro.composer.node.Initializable;
import org.cthul.miro.composer.ComposerInternal;
import org.cthul.miro.composer.node.CopyInitializable;
import org.cthul.miro.composer.node.Copyable;
import java.util.function.Supplier;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.map.*;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.domain.*;
import org.cthul.miro.domain.impl.MappedSelectorImpl;
import org.cthul.miro.util.XBiConsumer;
import org.cthul.miro.util.XConsumer;
import static java.util.Arrays.asList;
import org.cthul.miro.entity.EntityTemplate;
import org.cthul.miro.entity.map.MappedProperty;
import org.cthul.miro.domain.impl.MappedTemplateImpl;
import org.cthul.miro.entity.*;

/**
 *
 * @param <Entity>
 */
public class MappedQueryNodeFactory<Entity> implements MappedQueryComposer {
    
    private final AbstractQueryableType<Entity,?> owner;
    private final Object typeKey;
    private final Supplier<? extends EntityTemplate<Entity>> defaultType;
    
    public MappedQueryNodeFactory(AbstractQueryableType<Entity, ?> owner) {
        this(owner, owner.entityClass(), null);
    }
    
    public MappedQueryNodeFactory(AbstractQueryableType<Entity, ?> owner, Object typeKey, Supplier<? extends EntityTemplate<Entity>> defaultType) {
        this.owner = owner;
        this.typeKey = typeKey;
        this.defaultType = defaultType;
    }

    protected AbstractQueryableType<Entity, ?> getOwner() {
        return owner;
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

        private Repository repository = null;
        private EntitySet<? extends Entity> entitySet;
        private boolean useRepository = false;

        public TypePart() {
        }

        protected TypePart(TypePart source) {
            this.repository = source.repository;
            this.entitySet = source.entitySet;
            this.useRepository = source.useRepository;
        }
        
        @Override
        public void setRepository(Repository repository) {
            this.repository = repository;
            useRepository = repository != null;
        }
        
        protected final EntitySet<Entity> templateAsSet(EntityTemplate<? extends Entity> template) {
            return new EntitySet<Entity>() {
                @Override
                public MappedTemplate<Entity> getLookUp() {
                    return new MappedTemplateImpl<>(getOwner(), repository, null, template, Entities.noConfiguration());
                }
                @Override
                public MappedSelector<Entity> getSelector() {
                    return new MappedSelectorImpl<>(getOwner(), repository, null, getOwner().newEntityCreator(repository));
                }
                @Override
                public EntityConfiguration<Entity> readProperties(Collection<?> properties) {
                    return getOwner().getPropertyReader(repository, properties);
                }
                @Override
                public void loadProperties(Collection<?> properties, InitializationBuilder<Entity> initBuilder) {
                     getOwner().newPropertyLoader(repository, null, properties, initBuilder);
                }
            };
        }

        @Override
        public EntitySet<? extends Entity> getEntitySet() {
            if (useRepository) {
                if (repository == null) {
                    throw new IllegalStateException("Graph or entity type required");
                }
                if (typeKey == null) {
                    throw new IllegalStateException("Type key required");
                }
                return repository.<Entity>getEntitySet(typeKey);
            } else if (entitySet != null) {
                return entitySet;
            } else if (defaultType != null) {
                return entitySet = templateAsSet(defaultType.get());
            } else {
//                if (entitySet == null) {
//                    // entityTemplate was explicitly batch to null
                    throw new IllegalStateException("Graph or entity type required");
//                }
            }
        }

        @Override
        public void setTemplate(EntityTemplate<? extends Entity> template) {
            if (template == null) {
                this.entitySet = null;
                useRepository = true;
            } else {
                this.entitySet = templateAsSet(template);
                this.useRepository = false;
            }
        }

        @Override
        public void setEntitySet(EntitySet<? extends Entity> set) {
            if (set == null) {
                this.entitySet = null;
                useRepository = true;
            } else {
                this.entitySet = set;
                this.useRepository = false;
            }
        }

        @Override
        public void addTo(Mapping<? extends Entity> mapping) {
            EntityTemplate et = getEntitySet().getLookUp();
            if (et != null) mapping.setTemplate(et);
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

        private final LinkedHashSet<String> properties = new LinkedHashSet<>();
        private MappedQueryComposer cmp;

        public LoadField() {
        }

        public LoadField(LoadField src) {
            properties.addAll(src.properties);
        }

        @Override
        public void initialize(MappedQueryComposer.Internal composer) {
            this.cmp = composer;
        }
        
        @Override
        public void addTo(Mapping<? extends Entity> mapping) {
            EntitySet<Entity> set = cmp.getType().getEntitySet();
            EntityConfiguration<Entity> cfg = set.readProperties(properties);
            mapping.configureWith(cfg);
        }

        @Override
        public void add(String attribute) {
            properties.add(attribute);
        }

        @Override
        protected LoadField copyInstance() {
            return new LoadField(this);
        }

        @Override
        public String toString() {
            return "LOAD " + properties;
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
            MappedProperty<?> at = getOwner().getAttributes().getAttributeMap().get(entry);
            if (at == null) throw new IllegalArgumentException(entry);
            resultColumns.addAll(at.getMapping().getColumns());
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
            MappedProperty<Entity> at = getOwner().getAttributes().getAttributeMap().get(key);
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
