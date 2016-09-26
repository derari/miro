package org.cthul.miro.graph.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.entity.FactoryBuilder;
import org.cthul.miro.entity.InitializationBuilder;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeSet;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.graph.SelectorBuilder;
import org.cthul.miro.graph.impl.KeyMap.MultiKey;
import org.cthul.miro.util.Completable;
import org.cthul.miro.util.XSupplier;

/**
 * Framework for a {@link NodeType} implementation.
 * 
 * @param <Entity> 
 */
public abstract class AbstractNodeType<Entity> implements NodeType<Entity> {

    private final String shortString;

    public AbstractNodeType() {
        this.shortString = null;
    }
    
    public AbstractNodeType(Object shortString) {
        this.shortString = Objects.toString(shortString);
    }

    //<editor-fold defaultstate="collapsed" desc="NodeType implementation">
    @Override
    public abstract void newNodeFactory(GraphApi graph, SelectorBuilder<? super Entity> builder) throws MiException;

//    @Override
//    public void newEntityFactory(MiResultSet rs, GraphApi graph, FactoryBuilder<? super Entity> builder) throws MiException {
//        ColumnReader kr = newKeyReader(rs, graph);
//        NodeSet<Entity> ns = b -> newNodeFactory(graph, b);
//        selectorAsFactory(kr, ns, builder);
//    }

    @Override
    public void newAttributeLoader(GraphApi graph, List<?> attributes, InitializationBuilder<? extends Entity> builder) throws MiException {
        BatchLoader<Entity> batchLoader = newBatchLoader(graph, attributes);
        builder.addInitializer(entity -> {
            Object[] key = getKey(entity, null);
            batchLoader.add(entity, key);
        });
        builder.addCompleteAndClose(batchLoader)
                .addName("attributes from " + batchLoader);
    }
    
    @Override
    public EntityConfiguration<Entity> getAttributeReader(GraphApi graph, List<?> attributes) {
        if (attributes.isEmpty()) return EntityTypes.noConfiguration();
        return createAttributeReader(graph, attributes);
    }
    
    protected abstract EntityConfiguration<Entity> createAttributeReader(GraphApi graph, List<?> attributes);

    @Override
    public EntityType<Entity> asEntityType(GraphApi graph, NodeSet<Entity> nodeSet) {
        return new SetAsType<>(this, graph, nodeSet);
    }
    //</editor-fold>
    
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
     * @param graph
     * @return key reader
     * @throws MiException 
     */
    protected abstract ColumnReader newKeyReader(MiResultSet resultSet, GraphApi graph) throws MiException;
    
    protected abstract BatchLoader<Entity> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException;

    @Override
    public String toString() {
        return getShortString();
    }

    protected String getShortString() {
        return shortString != null ? shortString : super.toString();
    }
    
    /**
     * Loads entity attributes in a batch.
     * @param <Entity> 
     */
    protected static interface BatchLoader<Entity> extends Completable, AutoCloseable {
        
        void add(Entity e, Object[] key) throws MiException;

        @Override
        void complete() throws MiException;

        @Override
        default void close() throws MiException {
            complete();
        }
    }
    
    protected abstract class AbstractBatchLoader implements BatchLoader<Entity> {
        
        private final KeyMap.MultiKey<Entity> map = new KeyMap.MultiKey<>();
        private final EntityType<Entity> type;
        private List<Object[]> keys = new ArrayList<>();

        public AbstractBatchLoader(GraphApi graph) {
            this(graph, EntityTypes.noConfiguration());
        }
        
        public AbstractBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            this(graph, getAttributeReader(graph, attributes));
        }
        
        public AbstractBatchLoader(GraphApi graph, EntityConfiguration<Entity> configuration) {
            this.type = new SelectByKey(graph, map).with(configuration);
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
            this(graph, getAttributeReader(graph, attributes));
        }

        public SimpleBatchLoader(GraphApi graph, EntityConfiguration<Entity> configuration) {
            super(graph, configuration);
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
            return "simple " + super.toString();
        }
    }
    
    /** Used by AbstractBatchLoader */
    protected class SelectByKey implements NodeSet<Entity>, EntityType<Entity> {
        private final GraphApi graph;
        private final KeyMap.MultiKey<Entity> map;

        public SelectByKey(GraphApi graph, MultiKey<Entity> map) {
            this.graph = graph;
            this.map = map;
        }

        @Override
        public void newNodeSelector(SelectorBuilder<? super Entity> builder) throws MiException {
            builder.setFactory(key -> {
                Entity e = map.get(key);
                if (e == null) throw new IllegalArgumentException(
                        "unexpected key: " + Arrays.toString(key));
                return e;
            });
            builder.addName("SelectByKey");
        }

        @Override
        public void newFactory(MiResultSet rs, FactoryBuilder<? super Entity> builder) throws MiException {
            ColumnReader kr = newKeyReader(rs, graph);
            selectorAsFactory(kr, this, builder);
        }

        @Override
        public String toString() {
            return "SelectByKey";
        }
    }
    
    protected static class SetAsType<Entity> implements EntityType<Entity> {
        private final AbstractNodeType<Entity> type;
        private final GraphApi graph;
        private final NodeSet<Entity> nodeSet;

        public SetAsType(AbstractNodeType<Entity> type, GraphApi graph, NodeSet<Entity> nodeSet) {
            this.type = type;
            this.graph = graph;
            this.nodeSet = nodeSet;
        }

        @Override
        public void newFactory(MiResultSet rs, FactoryBuilder<? super Entity> builder) throws MiException {
            ColumnReader kr = type.newKeyReader(rs, graph);
            selectorAsFactory(kr, nodeSet, builder);
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }
    
    protected static <Entity> void selectorAsFactory(ColumnReader keyReader, NodeSet<Entity> nodeSet, FactoryBuilder<? super Entity> builder) throws MiException {
        NodeSelector<Entity> nodeFactory = CompositeSelector.buildNestedFrom(builder, nodeSet);
        builder.setFactory(new XSupplier<Entity, MiException>() {
            Object[] tmpKey = null;
            @Override
            public Entity get() throws MiException {
                tmpKey = keyReader.get(tmpKey);
                return nodeFactory.get(tmpKey);
            }
        });
        builder.addName(nodeFactory);
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
