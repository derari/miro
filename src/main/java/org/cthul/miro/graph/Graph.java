package org.cthul.miro.graph;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.map.MappedTemplateProvider;
import org.cthul.miro.result.EntityFactory;
import org.cthul.miro.result.EntityType;

public class Graph {

    @SuppressWarnings("rawtypes")
    private final Map<Object, EntitySet> entitySets = new HashMap<>();

    public Graph() {
    }
    
    public <E> EntitySet<E> entityType(EntityGraphAdapter<E> ga) {
        return entitySet(ga);
    }

    public <E> EntitySet<E> entityType(MappedTemplateProvider<E> mtp) {
        return entityType(mtp.getGraphAdapter());
    }

    @SuppressWarnings("rawtypes")
    protected <E> EntitySet<E> entitySet(EntityGraphAdapter<E> ga) {
        EntitySet es = entitySets.get(ga);
        if (es == null) {
            es = new EntitySet(ga);
            entitySets.put(ga, es);
        }
        return es;
    }
    
    public class EntitySet<Entity> implements EntityType<Entity> {
        
        private final EntityGraphAdapter<Entity> ga;
        private final KeyMap<?,Entity> entities;

        public EntitySet(EntityGraphAdapter<Entity> ga) {
            this.ga = ga;
//            if (exampleKey instanceof Object[]) {
                entities = new KeyMap.MultiKey<>();
//            } else {
//                entities = new KeyMap.SingleKey<>();
//            }
        }    
        
        public Entity get(Object[] key) {
            return entities.get(key);
        }
        
        public void put(Object[] key, Entity e) {
            entities.put(key, e);
        }
        
        public Object[] getKey(Entity e) {
            return ga.getKey(e, EMPTY);
        }
        
        public void put(Entity e) {
            Object[] key = ga.getKey(e, EMPTY);
            put(key, e);
        }

        @Override
        public EntityFactory<Entity> newFactory(ResultSet rs) throws SQLException {
            return new CachedEntityFactory<>(rs, this);
        }

        @Override
        public Entity[] newArray(int length) {
            return ga.newArray(length);
        }
    }
    
    protected class CachedEntityFactory<Entity> implements EntityFactory<Entity> {
        
        private final EntitySet<Entity> cache;
        private final EntityFactory<Entity> factory;
        private final EntityGraphAdapter.KeyReader keyReader;
        private Object[] buf = EMPTY;

        public CachedEntityFactory(ResultSet rs, EntitySet<Entity> cache) throws SQLException {
            this.cache = cache;
            this.factory = cache.ga.newFactory(rs);
            this.keyReader = cache.ga.newKeyReader(rs);
        }

        @Override
        public Entity newEntity() throws SQLException {
            buf = keyReader.getKey(buf);
            Object[] keys = buf;
            Entity e = cache.get(keys);
            if (e != null) return e;
            e = factory.newEntity();
            cache.put(keys, e);
            return e;
        }

        @Override
        public Entity newCursorValue(ResultCursor<? super Entity> rc) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Entity copy(Entity e) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws SQLException {
        }
    }
    
    private static final Object[] EMPTY = {};
}
