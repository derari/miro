package org.cthul.miro.domain.impl;

import java.util.*;
import jdk.nashorn.internal.runtime.regexp.joni.constants.NodeType;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.domain.*;
import org.cthul.miro.domain.KeyMap.MultiKey;
import org.cthul.miro.entity.*;
import org.cthul.miro.util.Completable;
import org.cthul.miro.util.XSupplier;
import org.cthul.miro.entity.builder.SelectorBuilder;

/**
 * Framework for a {@link NodeType} implementation.
 * 
 * @param <Entity> 
 */
public abstract class AbstractEntityType<Entity> implements EntityType<Entity> {

    private final String shortString;

    public AbstractEntityType() {
        this.shortString = getClass().getSimpleName();
    }
    
    public AbstractEntityType(Object shortString) {
        this.shortString = Objects.toString(shortString);
    }

    //<editor-fold defaultstate="collapsed" desc="EntityType implementation">
    
    @Override
    public abstract void newEntityCreator(Repository repository, SelectorBuilder<Entity> selectorBuilder);
   
    @Override
    public EntityTemplate<Entity> newEntityLookUp(Repository repository, EntitySelector<Entity> selector) {
        return new SetAsType(this, repository, selector);
    }

    @Override
    public MappedTemplate<Entity> newEntityLookUp(Repository repository, MiConnection connection, EntitySelector<Entity> selector) {
        return new MappedTemplateImpl<>(this, repository, connection, newEntityLookUp(repository, selector), Entities.noConfiguration());
    }

    @Override
    public EntityConfiguration<Entity> getPropertyReader(Repository repository, Collection<?> properties) {
        if (properties.isEmpty()) return Entities.noConfiguration();
        return createPropertiesReader(repository, properties);
    }
    
    protected abstract EntityConfiguration<Entity> createPropertiesReader(Repository repository, Collection<?> properties);

    @Override
    public void newPropertyLoader(Repository repository, MiConnection connection, Collection<?> properties, InitializationBuilder<? extends Entity> builder) {
        BatchLoader<Entity> batchLoader = newBatchLoader(repository, connection, properties);
        builder.addInitializer(entity -> {
            Object[] key = getKey(entity, null);
            batchLoader.add(entity, key);
        });
        builder.addCompleteAndClose(batchLoader)
                .addName("properties from " + batchLoader);
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
     * @param repository
     * @return key reader
     * @throws MiException 
     */
    protected abstract ColumnReader newKeyReader(Repository repository, MiResultSet resultSet) throws MiException;
    
    protected abstract BatchLoader<Entity> newBatchLoader(Repository repository, MiConnection connection, Collection<?> properties);
    
    protected EntityTemplate<Entity> newEntityTemplate(Repository repository) {
        return (resultSet, builder) -> newEntityFactory(repository, resultSet, builder);
    }
    
    protected EntityFactory<Entity> newEntityFactory(Repository repository, MiResultSet resultSet) throws MiException {
        return new EntityFactory<Entity>() {
            final ColumnReader keyReader = newKeyReader(repository, resultSet);
            final EntitySelector<Entity> creator = newEntityCreator(repository);
            Object[] array = null;
            @Override
            public Entity newEntity() throws MiException {
                array = keyReader.get(array);
                return creator.get(array);
            }
            @Override
            public void complete() throws MiException {
                creator.complete();
            }
            @Override
            public void close() throws MiException {
                creator.close();
            }
            @Override
            public String toString() {
                return AbstractEntityType.this.toString();
            }
        };
    }
    
    protected void newEntityFactory(Repository repository, MiResultSet resultSet, FactoryBuilder<? super Entity> builder) throws MiException {
        builder.set(newEntityFactory(repository, resultSet));
    }
    
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
        private final EntityTemplate<Entity> template;
        private List<Object[]> keys = new ArrayList<>();

        public AbstractBatchLoader(Repository repository, Collection<?> properties) {
            this(repository, getPropertyReader(repository, properties));
        }
        
        private AbstractBatchLoader(Repository repository, EntityConfiguration<Entity> configuration) {
            this.template = new SelectFromMap(repository, map, configuration);
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
            fillProperties(template, oldKeys);
        }
        
        protected abstract void fillProperties(EntityTemplate<Entity> template, List<Object[]> keys) throws MiException;

        @Override
        public String toString() {
            return "batch" + template;
        }
    }
    
    protected abstract class SimpleBatchLoader extends AbstractBatchLoader {

        public SimpleBatchLoader(Repository repository, Collection<?> properties) {
            super(repository, properties);
        }
        
        @Override
        protected void fillProperties(EntityTemplate<Entity> template, List<Object[]> keys) throws MiException {
            try (MiResultSet resultSet = fetchProperties(keys);
                    EntityFactory<Entity> factory = template.newFactory(resultSet)) {
                while (resultSet.next()) {
                    factory.newEntity();
                }
            }            
        }
        
        protected abstract MiResultSet fetchProperties(List<Object[]> keys) throws MiException;

        @Override
        public String toString() {
            return "simple " + super.toString();
        }
    }
    
    /** Used by AbstractBatchLoader */
    protected class SelectFromMap implements EntityTemplate<Entity> {
        private final Repository repository;
        private final KeyMap.MultiKey<Entity> map;
        private final EntityConfiguration<Entity> configuration;

        public SelectFromMap(Repository repository, MultiKey<Entity> map, EntityConfiguration<Entity> configuration) {
            this.repository = repository;
            this.map = map;
            this.configuration = configuration;
        }

        protected void newNodeSelector(SelectorBuilder<? super Entity> builder) throws MiException {
            builder.setSelector(key -> {
                Entity e = map.get(key);
                if (e == null) throw new IllegalArgumentException(
                        "unexpected key: " + Arrays.toString(key));
                return e;
            });
            builder.addName("SelectByKey");
        }

        @Override
        public void newFactory(MiResultSet resultSet, FactoryBuilder<? super Entity> builder) throws MiException {
            EntitySelector<Entity> sel = builder.nestedSelector(this::newNodeSelector);
            ColumnReader kr = newKeyReader(repository, resultSet);
            selectorAsFactory(kr, sel, builder)
                    .add(configuration, resultSet);
        }

        @Override
        public String toString() {
            return "SelectByKey";
        }
    }
    

    protected static class SetAsType<Entity> implements EntityTemplate<Entity> {
        private final AbstractEntityType<Entity> type;
        private final Repository repository;
        private final EntitySelector<Entity> source;

        public SetAsType(AbstractEntityType<Entity> type, Repository repository, EntitySelector<Entity> source) {
            this.type = type;
            this.repository = repository;
            this.source = source;
        }

        @Override
        public void newFactory(MiResultSet rs, FactoryBuilder<? super Entity> builder) throws MiException {
            ColumnReader cr = type.newKeyReader(repository, rs);
            selectorAsFactory(cr, source, builder);
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }
    
    protected static <Entity> FactoryBuilder<Entity> selectorAsFactory(ColumnReader keyReader, EntitySelector<Entity> source, FactoryBuilder<? super Entity> builder) throws MiException {
        return builder
            .addName(source)
            .setFactory(new XSupplier<Entity, MiException>() {
                Object[] tmpKey = null;
                @Override
                public Entity get() throws MiException {
                    tmpKey = keyReader.get(tmpKey);
                    return source.get(tmpKey);
                }
            });
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
    
    protected static List<String> flattenStr(Collection<?> list) {
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
