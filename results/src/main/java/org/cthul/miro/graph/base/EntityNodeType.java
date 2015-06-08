package org.cthul.miro.graph.base;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.graph.base.EntityKeyAdapter.KeyReader;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.util.Completable;

public abstract class EntityNodeType<Entity> implements NodeType<Entity> {

    @Override
    public abstract Entity[] newArray(int length);
    
    protected abstract EntityKeyAdapter<Entity> getKeyAdapter();

    @Override
    public NodeSelector<Entity> newNodeFactory(GraphApi graph) throws MiException {
        return new NodeFactory<>(getKeyAdapter());
    }

    @Override
    public NodeSelector<Entity> newAttributeLoader(GraphApi graph, List<String> attributes, NodeSelector<Entity> nodeFactory) throws MiException {
        if (attributes.isEmpty()) {
            return nodeFactory;
        } else {
            return newAttributesSelector(graph, attributes, nodeFactory);
        }
    }
    
    protected NodeSelector<Entity> newAttributesSelector(GraphApi graph, List<String> attributes, NodeSelector<Entity> nodeFactory) throws MiException {
        AttributeQuery<Entity> aq = EntityNodeType.this.newAttributeQuery(graph, attributes);
        return new AttributeNodeSelector(aq, nodeFactory);
    }

    @Override
    public EntityFactory<Entity> newEntityFactory(GraphApi graph, NodeSelector<Entity> nodeFactory, MiResultSet resultSet) throws MiException {
        return new FactorySelector<>(resultSet, getKeyAdapter(), nodeFactory);
    }

    @Override
    public EntityConfiguration<Entity> newAttributeLoader(GraphApi graph, List<String> attributes) throws MiException {
        AttributeQuery<Entity> aq = EntityNodeType.this.newAttributeQuery(graph, attributes);
        return new AttributeLoadingConfiguration(aq);
    }

    @Override
    public abstract EntityConfiguration<Entity> newAttributeSetter(GraphApi graph, List<String> attributes) throws MiException;
    
    protected abstract AttributeQuery<Entity> newAttributeQuery(GraphApi graph, List<String> attributes) throws MiException;
    
    protected BatchLoader newBatchLoader(GraphApi graph, List<String> attributes, BatchQueryBuilder queryBuilder) throws MiException {
        EntityConfiguration<Entity> configuration = newAttributeSetter(graph, attributes);
        return newBatchLoader(graph, configuration, queryBuilder);
    }
    
    protected BatchLoader newBatchLoader(GraphApi graph, EntityConfiguration<Entity> configuration, BatchQueryBuilder queryBuilder) {
        return new BatchLoader(graph, configuration) {
            @Override
            protected MiResultSet batchQuery(GraphApi graph, List<Object[]> keys) throws MiException {
                return queryBuilder.batchQuery(graph, keys);
            }
        };
    }
    
    protected static class NodeFactory<Entity> implements NodeSelector<Entity> {
        
        private final EntityKeyAdapter<Entity> keyAdapter;
        private final EntityInitializer<Entity> init;

        public NodeFactory(EntityKeyAdapter<Entity> keyAdapter) {
            this.keyAdapter = keyAdapter;
            this.init = EntityTypes.noInitialization();
        }

        public NodeFactory(EntityKeyAdapter<Entity> keyAdapter, EntityInitializer<Entity> init) {
            this.keyAdapter = keyAdapter;
            this.init = init;
        }

        @Override
        public Entity get(Object... key) throws MiException {
            Entity e = keyAdapter.newEntity(key);
            init.apply(e);
            return e;
        }

        @Override
        public void complete() throws MiException {
            init.complete();
        }

        @Override
        public void close() throws MiException {
            init.close();
        }
    }
    
    protected abstract class BatchLoader implements AttributeQuery<Entity> {
        
        private List<Object[]> keys = new ArrayList<>();
        private final KeyMap.MultiKey<Entity> map = new KeyMap.MultiKey<>();
        private final GraphApi graph;
        private final EntityKeyAdapter<Entity> keyAdapter;
        private final EntityConfiguration<Entity> configuration;

        public BatchLoader(GraphApi graph, EntityConfiguration<Entity> configuration) {
            this(graph, getKeyAdapter(), configuration);
        }

        public BatchLoader(GraphApi graph, EntityKeyAdapter<Entity> keyAdapter, EntityConfiguration<Entity> configuration) {
            this.graph = graph;
            this.keyAdapter = keyAdapter;
            this.configuration = configuration;
        }
        
        @Override
        public boolean add(Entity e, Object[] key) throws MiException {
            map.put(key, e);
            keys.add(key);
            return keys.size() > 100;
        }
        
        @Override
        public void complete() throws MiException {
            if (keys.isEmpty()) return;
            try (MiResultSet resultSet = batchQuery(graph, keys);
                    EntityInitializer<Entity> initializer = configuration.newInitializer(resultSet)) {
                keys = new ArrayList<>();
                KeyReader keyReader = keyAdapter.newKeyReader(resultSet);
                Object[] tmpKey = null;
                while (resultSet.next()) {
                    tmpKey = keyReader.getKey(tmpKey);
                    Entity e = map.get(tmpKey);
                    initializer.apply(e);
                }
            }
        }
        
        protected abstract MiResultSet batchQuery(GraphApi graph, List<Object[]> keys) throws MiException;
    }
    
    protected static interface BatchQueryBuilder {
        
        MiResultSet batchQuery(GraphApi graph, List<Object[]> keys) throws MiException;
    }

    protected static interface AttributeQuery<Entity> extends Completable {
        
        boolean add(Entity e, Object[] key) throws MiException;

        @Override
        void complete() throws MiException;
    }
    
    protected class AttributeNodeSelector implements NodeSelector<Entity> {

        private final AttributeQuery<Entity> attributeQuery;
        private final NodeSelector<Entity> nodeFactory;

        public AttributeNodeSelector(AttributeQuery<Entity> attributeQuery, NodeSelector<Entity> nodeFactory) {
            this.attributeQuery = attributeQuery;
            this.nodeFactory = nodeFactory;
        }

        @Override
        public Entity get(Object... key) throws MiException {
            Entity e = nodeFactory.get(key);
            if (attributeQuery.add(e, key)) {
                complete();
            }
            return e;
        }

        @Override
        public void complete() throws MiException {
            nodeFactory.complete();
            attributeQuery.complete();
        }

        @Override
        public void close() throws MiException {
            nodeFactory.close();
        }
    }
    
    protected class AttributeLoadingConfiguration implements EntityConfiguration<Entity> {
        
        private final EntityKeyAdapter<Entity> keyAdapter;
        private final AttributeQuery<Entity> attributeQuery;

        public AttributeLoadingConfiguration(AttributeQuery<Entity> attributeQuery) {
            this(getKeyAdapter(), attributeQuery);
        }
        
        public AttributeLoadingConfiguration(EntityKeyAdapter<Entity> keyAdapter, AttributeQuery<Entity> attributeQuery) {
            this.keyAdapter = keyAdapter;
            this.attributeQuery = attributeQuery;
        }

        @Override
        public EntityInitializer<Entity> newInitializer(MiResultSet resultSet) throws MiException {
            return new EntityInitializer<Entity>() {
                @Override
                public void apply(Entity entity) throws MiException {
                    Object[] key = keyAdapter.getKey(entity, null);
                    if (attributeQuery.add(entity, key)) {
                        complete();
                    }
                }
                @Override
                public void complete() throws MiException {
                    attributeQuery.complete();
                }
            };
        }
    }
    
    protected static class FactorySelector<Entity> implements EntityFactory<Entity> {
        
        private final KeyReader keyReader;
        private final NodeSelector<Entity> nodeFactory;
        private Object[] tmpKey = null;

        public FactorySelector(MiResultSet resultSet, EntityKeyAdapter<Entity> keyAdapter, NodeSelector<Entity> nodeFactory) throws MiException {
            this(keyAdapter.newKeyReader(resultSet), nodeFactory);
        }

        public FactorySelector(KeyReader keyReader, NodeSelector<Entity> nodeFactory) {
            this.keyReader = keyReader;
            this.nodeFactory = nodeFactory;
        }

        @Override
        public Entity newEntity() throws MiException {
            tmpKey = keyReader.getKey(tmpKey);
            return nodeFactory.get(tmpKey);
        }

        @Override
        public void complete() throws MiException {
            nodeFactory.complete();
        }

        @Override
        public void close() throws MiException {
            nodeFactory.close();
        }
    }
}
