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
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.util.Completable;

/**
 * Framework for a {@link NodeType} implementation.
 * 
 * @param <Entity> 
 */
public abstract class AbstractEntityNodeType<Entity> implements EntityType<Entity>, NodeType<Entity> {

    private final String shortString;

    public AbstractEntityNodeType() {
        this.shortString = null;
    }
    
    public AbstractEntityNodeType(Object shortString) {
        this.shortString = Objects.toString(shortString);
    }
    
    @Override
    public abstract Entity[] newArray(int length);

    //<editor-fold defaultstate="collapsed" desc="NodeType implementation">
    @Override
    public NodeSelector<Entity> newNodeFactory(GraphApi graph) throws MiException {
        return new NodeFactory<>(this);
    }
    
    @Override
    public EntityInitializer<Entity> newAttributeLoader(GraphApi graph, List<?> attributes) throws MiException {
        if (attributes.isEmpty()) return EntityTypes.noInitialization();
        return createAttributeLoader(graph, attributes);
    }
    
    protected EntityInitializer<Entity> createAttributeLoader(GraphApi graph, List<?> attributes) throws MiException {
        return new AttributeLoader(newBatchLoader(graph, attributes));
    }
    
    @Override
    public EntityConfiguration<Entity> getAttributeReader(GraphApi graph, List<?> attributes) {
        if (attributes.isEmpty()) return EntityTypes.noConfiguration();
        return createAttributeReader(graph, attributes);
    }
    
    protected abstract EntityConfiguration<Entity> createAttributeReader(GraphApi graph, List<?> attributes);

    @Override
    public EntityType<Entity> asEntityType(GraphApi graph, NodeSelector<Entity> nodeSelector) {
        return new SelectorAsType<>(this, nodeSelector);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="EntityType implementation">
    protected ColumnReader getConstructorArguments(MiResultSet rs) throws MiException {
        return n -> null;
    }
    
    @Override
    public EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException {
        ColumnReader kr = getConstructorArguments(rs);
        return new SelectorAsFactory<>(kr, new NodeFactory(this));
    }
    //</editor-fold>

    /**
     * Creates a new entity, with the given key if {@code key} is not null.
     * @param key optional key
     * @return new entity
     * @throws MiException
     */
    protected abstract Entity newEntity(Object[] key) throws MiException;
    
    /**
     * Returns the key of an entity.
     * @param e
     * @param array array of appropriate length or {@code null}
     * @return key
     * @throws MiException
     */
    protected abstract Object[] getKey(Entity e, Object[] array) throws MiException;
    
    /**
     * Creates a key reader for the result set.
     * @param resultSet
     * @return key reader
     * @throws MiException 
     */
    protected abstract ColumnReader newKeyReader(MiResultSet resultSet) throws MiException;
    
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
        
        public AbstractBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            this(getAttributeReader(graph, attributes));
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
            fillAttributes(type, oldKeys);
        }
        
        protected abstract void fillAttributes(EntityType<Entity> type, List<Object[]> keys) throws MiException;

        @Override
        public String toString() {
            return "batch" + type;
        }
    }
    
    protected abstract class SimpleBatchLoader extends AbstractBatchLoader {

        public SimpleBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            this(getAttributeReader(graph, attributes));
        }

        public SimpleBatchLoader(EntityConfiguration<Entity> configuration) {
            super(configuration);
        }
        
        @Override
        protected void fillAttributes(EntityType<Entity> type, List<Object[]> keys) throws MiException {
            try (MiResultSet resultSet = fetchAttributes(keys);
                    EntityFactory<Entity> factory = type.newFactory(resultSet)) {
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
            ColumnReader kr = newKeyReader(rs);
            return new SelectorAsFactory<>(kr, this);
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
    
    protected class AttributeLoader implements EntityInitializer<Entity> {

        private final BatchLoader<Entity> attributeQuery;

        public AttributeLoader(BatchLoader<Entity> attributeQuery) {
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
            complete();
        }

        @Override
        public String toString() {
            return "attributes from " + attributeQuery;
        }
    }
    
    protected static class SelectorAsType<Entity> implements EntityType<Entity> {
        private final AbstractEntityNodeType<Entity> type;
        private final NodeSelector<Entity> nodeSelector;

        public SelectorAsType(AbstractEntityNodeType<Entity> type, NodeSelector<Entity> nodeSelector) {
            this.type = type;
            this.nodeSelector = nodeSelector;
        }

        @Override
        public Entity[] newArray(int length) {
            return type.newArray(length);
        }

        @Override
        public EntityFactory<Entity> newFactory(MiResultSet rs) throws MiException {
            ColumnReader kr = type.newKeyReader(rs);
            return new SelectorAsFactory<>(kr, nodeSelector);
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }
    
    protected static class SelectorAsFactory<Entity> implements EntityFactory<Entity> {
        
        private final ColumnReader keyReader;
        private final NodeSelector<Entity> nodeFactory;
        private Object[] tmpKey = null;

//        public SelectorAsFactory(KeyReader keyReader, AbstractEntityNodeType<Entity> type) {
//            this(keyReader, new NodeFactory<>(type));
//        }
        
        public SelectorAsFactory(ColumnReader keyReader, NodeSelector<Entity> nodeFactory) {
            this.keyReader = keyReader;
            this.nodeFactory = nodeFactory;
        }

        @Override
        public Entity newEntity() throws MiException {
            tmpKey = keyReader.get(tmpKey);
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
}
