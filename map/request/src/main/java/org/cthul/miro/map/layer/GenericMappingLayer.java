package org.cthul.miro.map.layer;

import java.util.LinkedHashSet;
import java.util.function.Predicate;
import org.cthul.miro.request.ComposerKey;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.impl.AbstractTemplateLayer;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.MappingKey;

/**
 *
 * @param <Entity>
 */
public class GenericMappingLayer<Entity> extends AbstractTemplateLayer<Mapping<? extends Entity>> {
    
    private final Object typeKey;
    private final EntityType<Entity> defaultType;

    public GenericMappingLayer() {
        this.typeKey = null;
        this.defaultType = null;
    }

    public GenericMappingLayer(Object typeKey, EntityType<Entity> defaultType) {
        this.typeKey = typeKey;
        this.defaultType = defaultType;
    }

    @Override
    protected Template<? super Mapping<? extends Entity>> createPartTemplate(Parent<Mapping<? extends Entity>> parent, Object key) {
        switch (ComposerKey.key(key)) {
            case ALWAYS:
                if (defaultType != null) {
                    return parent.andRequire(MappingKey.TYPE);
                }
        }
        switch (MappingKey.key(key)) {
            case TYPE:
                return Templates.newNodePart(() -> new TypePart());
        }
        return null;
    }
    
    protected class TypePart implements MappingKey.Type, StatementPart<Mapping<? extends Entity>>, Copyable<Object> {

        private Graph graph = null;
        private EntityType<?> entityType = defaultType;
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
        public void setType(EntityType<?> type) {
            this.entityType = type;
            useGraph = type == null && graph != null;
        }
        
        protected EntityType entityType() {
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
                    throw new IllegalStateException("Graph or entity type required");
                }
                return entityType;
            }
        }

        @Override
        public void addTo(Mapping<? extends Entity> builder) {
            EntityType et = entityType();
            if (et != null) builder.setType(et);
        }

        @Override
        public Object copyFor(InternalComposer<Object> ic) {
            return new TypePart(this);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
    
    protected class ConfigurationPart implements MappingKey.Configuration, StatementPart<Mapping<? extends Entity>>, Copyable<Object> {
        
        private final LinkedHashSet<EntityConfiguration<?>> list;

        public ConfigurationPart() {
            this.list = new LinkedHashSet<>();
        }

        public ConfigurationPart(ConfigurationPart source) {
            this.list = new LinkedHashSet<>(source.list);
        }
        
        @Override
        public void configureWith(EntityConfiguration<?> config) {
            list.add(config);
        }

        @Override
        public void addTo(Mapping<? extends Entity> builder) {
            Mapping m = builder;
            list.forEach(cfg -> m.configureWith(cfg));
        }

        @Override
        public Object copyFor(InternalComposer<Object> iqc) {
            return new ConfigurationPart(this);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
}
