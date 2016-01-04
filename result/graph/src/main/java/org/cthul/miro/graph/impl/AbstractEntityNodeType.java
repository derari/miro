package org.cthul.miro.graph.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.entity.base.ResultColumns;
import org.cthul.miro.graph.EntityNodeType;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.util.Completable;

/**
 * Framework for a {@link NodeType} implementation.
 * 
 * @param <Entity> 
 */
public abstract class AbstractEntityNodeType<Entity> implements EntityNodeType<Entity> {

    private final String shortString;

    public AbstractEntityNodeType() {
        this.shortString = null;
    }
    
    public AbstractEntityNodeType(Object shortString) {
        this.shortString = Objects.toString(shortString);
    }
    
    @Override
    public abstract Entity[] newArray(int length);

    @Override
    public NodeSelector<Entity> newNodeFactory(GraphApi graph) throws MiException {
        return new NodeFactory<>(this);
    }

    @Override
    public EntityInitializer<Entity> newAttributeInitializer(GraphApi graph, List<?> attributes) throws MiException {
        if (attributes.isEmpty()) return EntityTypes.noInitialization();
        return actualNewAttributeInitializer(graph, attributes);
    }
    
    protected EntityInitializer<Entity> actualNewAttributeInitializer(GraphApi graph, List<?> attributes) throws MiException {
        return new AttributeInitializer(newBatchLoader(graph, attributes));
    }

    @Override
    public EntityFactory<Entity> newEntityFactory(GraphApi graph, NodeSelector<Entity> nodeFactory, MiResultSet resultSet) throws MiException {
        return new FactorySelector<>(this.newKeyReader(resultSet), nodeFactory);
    }
    
    @Override
    public EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException {
        KeyReader kr = n -> n;
        return new FactorySelector<>(kr, new NodeFactory<>(this));
    }

    /**
     * Creates a new entity, with the given key if {@code key} is not null.
     * @param key optional key
     * @return new entity
     */
    protected abstract Entity newEntity(Object[] key);
    
    /**
     * Returns the key of an entity.
     * @param e
     * @param array array of appropriate length or {@code null}
     * @return key
     */
    protected abstract Object[] getKey(Entity e, Object[] array);
    
    /**
     * Creates a key reader for the result set.
     * @see #newKeyReader(org.cthul.miro.db.MiResultSet, java.lang.String...) 
     * @see #newKeyReader(org.cthul.miro.db.MiResultSet, int...) 
     * @param resultSet
     * @return key reader
     * @throws MiException 
     */
    protected abstract KeyReader newKeyReader(MiResultSet resultSet) throws MiException;
    
    protected abstract BatchLoader<Entity> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException;

    @Override
    public String toString() {
        return getShortString();
    }

    protected String getShortString() {
        return shortString != null ? shortString : super.toString();
    }
    
    /**
     * A {@link NodeSelector} that always creates new nodes.
     * @param <Entity> 
     */
    protected static class NodeFactory<Entity> implements NodeSelector<Entity> {
        
        private final AbstractEntityNodeType<Entity> nodeType;

        public NodeFactory(AbstractEntityNodeType<Entity> nodeType) {
            this.nodeType = nodeType;
        }

        @Override
        public Entity get(Object... key) throws MiException {
            return nodeType.newEntity(key);
        }

        @Override
        public void complete() throws MiException { }

        @Override
        public String toString() {
            return "new " + nodeType;
        }
    }

    /**
     * Reads key values from a result set.
     */
    protected static interface KeyReader {
    
        /**
         * Returns the current row's key.
         * @param array array of appropriate size or {@code null}.
         * @return keys
         * @throws MiException 
         */
        Object[] getKey(Object[] array) throws MiException;
    }
    
    /**
     * Loads entity attributes in a batch.
     * @param <Entity> 
     */
    protected static interface BatchLoader<Entity> extends Completable {
        
        void add(Entity e, Object[] key) throws MiException;

        @Override
        void complete() throws MiException;
    }
    
    protected abstract class AbstractBatchLoader implements BatchLoader<Entity> {
        
        private List<Object[]> keys = new ArrayList<>();
        private final KeyMap.MultiKey<Entity> map = new KeyMap.MultiKey<>();
        private final EntityType<Entity> type;

        public AbstractBatchLoader() {
            this(EntityTypes.noConfiguration());
        }
        
        public AbstractBatchLoader(EntityConfiguration<Entity> configuration) {
            this.type = new SelectByKey(map).with(configuration);
        }
        
        @Override
        public void add(Entity e, Object[] key) throws MiException {
            map.put(key, e);
            keys.add(key);
            if (keys.size() > 100) {
                complete();
            }
        }
        
        @Override
        public void complete() throws MiException {
            if (keys.isEmpty()) return;
            List<Object[]> oldKeys = keys;
            keys = new ArrayList<>();
            fillAttributes(oldKeys);
        }

        protected EntityType<Entity> getType() {
            return type;
        }
        
        protected EntityFactory<Entity> newFactory(MiResultSet resultSet) throws MiException {
            return getType().newFactory(resultSet);
        }
        
        protected abstract void fillAttributes(List<Object[]> keys) throws MiException;

        @Override
        public String toString() {
            return "batch" + getType();
        }
    }
    
    protected abstract class SimpleBatchLoader extends AbstractBatchLoader {

        public SimpleBatchLoader() {
            super();
        }

        protected abstract EntityInitializer<Entity> attributeInitializer(MiResultSet resultSet) throws MiException;

        @Override
        protected EntityFactory<Entity> newFactory(MiResultSet resultSet) throws MiException {
            return super.newFactory(resultSet).with(attributeInitializer(resultSet));
        }
        
        @Override
        protected void fillAttributes(List<Object[]> keys) throws MiException {
            try (MiResultSet resultSet = fetchAttributes(keys);
                    EntityFactory<Entity> factory = newFactory(resultSet)) {
                while (resultSet.next()) {
                    factory.newEntity();
                }
            }            
        }
        
        protected abstract MiResultSet fetchAttributes(List<Object[]> keys) throws MiException;

        @Override
        public String toString() {
            return "simple batch";
        }
    }
    
    /** Used by AbstractBatchLoader */
    protected class SelectByKey implements NodeSelector<Entity>, EntityType<Entity> {
        private final KeyMap.MultiKey<Entity> map;

        public SelectByKey(KeyMap.MultiKey<Entity> map) {
            this.map = map;
        }

        @Override
        public Entity get(Object... key) throws MiException {
            Entity e = map.get(key);
            if (e == null) throw new IllegalArgumentException(
                    "unexpected key: " + Arrays.toString(key));
            return e;
        }

        @Override
        public void complete() throws MiException { }

        @Override
        public EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException {
            KeyReader kr = newKeyReader(rs);
            return new FactorySelector<>(kr, this);
        }

        @Override
        public Entity[] newArray(int length) {
            return AbstractEntityNodeType.this.newArray(length);
        }

        @Override
        public String toString() {
            return "";
        }
    }
    
    protected class AttributeInitializer implements EntityInitializer<Entity> {

        private final BatchLoader<Entity> attributeQuery;

        public AttributeInitializer(BatchLoader<Entity> attributeQuery) {
            this.attributeQuery = attributeQuery;
        }

        @Override
        public void apply(Entity entity) throws MiException {
            Object[] key = getKey(entity, null);
            attributeQuery.add(entity, key);
        }

        @Override
        public void complete() throws MiException {
            attributeQuery.complete();
        }

        @Override
        public void close() throws MiException {
        }

        @Override
        public String toString() {
            return "attributes from " + attributeQuery;
        }
    }
    
    protected static class FactorySelector<Entity> implements EntityFactory<Entity> {
        
        private final KeyReader keyReader;
        private final NodeSelector<Entity> nodeFactory;
        private Object[] tmpKey = null;

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

        @Override
        public String toString() {
            return nodeFactory.toString();
        }
    }
    
    protected static List<Object> flatten(List<?> list) {
        List<Object> result = new ArrayList<>(list.size());
        flatten(result, list);
        return result;
    }
    
    private static void flatten(List<Object> bag, Iterable<?> list) {
        list.forEach(e -> {
            if (e instanceof Object[]) {
                flatten(bag, Arrays.asList((Object[]) e));
            } else if (e instanceof Iterable) {
                flatten(bag, (Iterable<?>) e);
            } else if (e != null) {
                bag.add(e);
            }
        });
    }
    
    protected static List<String> flattenStr(List<?> list) {
        List<String> result = new ArrayList<>(list.size());
        flattenStr(result, list);
        return result;
    }
    
    private static void flattenStr(List<String> bag, Iterable<?> list) {
        list.forEach(e -> {
            if (e instanceof Object[]) {
                flattenStr(bag, Arrays.asList((Object[]) e));
            } else if (e instanceof Iterable) {
                flattenStr(bag, (Iterable<?>) e);
            } else if (e != null) {
                bag.add(e.toString());
            }
        });
    }
    
    @SuppressWarnings({"ConfusingArrayVararg", "PrimitiveArrayArgumentToVariableArgMethod"})
    protected static KeyReader newKeyReader(MiResultSet resultSet, String... columns) throws MiException {
        int[] indices = ResultColumns.findAllColumns(resultSet, columns);
        return newKeyReader(resultSet, indices);
    }
    
    protected static KeyReader newKeyReader(MiResultSet resultSet, List<String> columns) throws MiException {
        int[] indices = ResultColumns.findAllColumns(resultSet, columns);
        return newKeyReader(resultSet, indices);
    }
    
    protected static KeyReader newKeyReader(MiResultSet resultSet, int... indices) throws MiException {
        return array -> {
            if (array == null) array = new Object[indices.length];
            for (int i = 0; i < indices.length; i++) {
                int index = indices[i];
                array[i] = index < 0 ? null : resultSet.get(index);
            }
            return array;
        };
    }

}
