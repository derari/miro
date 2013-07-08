package org.cthul.miro.graph;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.View;
import org.cthul.miro.map.ResultBuilder;
import org.cthul.miro.map.ValueAdapterBase;

/**
 *
 */
public class Graph {

    private final MiConnection cnn;
    private final Map<View<?>, EntitySet> entitySets = new HashMap<>();

    public Graph(MiConnection cnn) {
        this.cnn = cnn;
    }

    protected EntitySet entitySet(View<? extends SelectByKey<?>> view, Object exampleKey) {
        EntitySet es = entitySets.get(view);
        if (es == null) {
            es = new EntitySet(view, exampleKey);
            entitySets.put(view, es);
        }
        return es;
    }

    public List<Object> getObjects(View<? extends SelectByKey<?>> view, List<Object> keys) throws SQLException {
        if (keys.isEmpty()) return keys;
        return entitySet(view, keys.get(0)).getObjects(keys);
    }

    /* GraphQuery */ <Entity> ResultBuilder.ValueAdapter<Entity> valueAdapter(View<? extends SelectByKey<?>> view, String[] keyFields) {
        return new InsertIntoGraphAdapter<>(this, view, keyFields);
    }
    
    protected class EntitySet {
        
        private final View<? extends SelectByKey<?>> view;
        private final Map<Object, Object> entities = new HashMap<>();
        private final boolean compositeKey;

        public EntitySet(View<? extends SelectByKey<?>> view, Object exampleKey) {
            this.view = view;
            this.compositeKey = exampleKey instanceof Object[]; 
        }

        public List<Object> getObjects(List<Object> keys) throws SQLException {
            final Object[] result = new Object[keys.size()];

            int missingCount = fill(result, keys);

            if (missingCount > 0) {
                fillMissing(result, keys);
            }

            return Arrays.asList(result);
        }

        private int fill(final Object[] values, final List<Object> keys) {
            int missingCount = 0;
            final MultiKey mk = compositeKey ? new MultiKey() : null;
            for (int i = 0; i < values.length; i++) {
                if (values[i] == null) {
                    final Object o;
                    if (mk == null) {
                        o = get(keys.get(i));
                    } else {
                        mk.become(keys.get(i));
                        o = get(mk);
                    }
                    if (o == null) {
                        missingCount++;
                    } else if (o != NULL) {
                        values[i] = o;
                    }
                }
            }
            return missingCount;
        }
        
        private void put(Object key, Object entity) {
            assert !compositeKey;
            entities.put(key, entity);
        }

        private void put(Object[] key, Object entity) {
            assert compositeKey;
            entities.put(new MultiKey(key), entity);
        }

        private Object get(Object key) {
            return entities.get(key);
        }

        private void fillMissing(Object[] result, List<Object> keys) throws SQLException {
            Set<Object> missingKeySet = new HashSet<>();
            for (int i = 0; i < result.length; i++) {
                if (result[i] == null) {
                    Object key = keys.get(i);
                    if (compositeKey) {
                        key = new MultiKey((Object[]) key);
                    }
                    missingKeySet.add(key);
                }
            }

            final Object[] missingKeys = missingKeySet.toArray(new Object[missingKeySet.size()]);
            fetchValues(missingKeys);
            for (Object key: missingKeys) {
                if (!entities.containsKey(key)) {
                    entities.put(key, NULL);
                }
            }

//            final MultiKey mk = compositeKey ? new MultiKey() : null;
//            for (int i = 0; i < missingKeys.length; i++) {
//                Object o = missingValues[i];
//                if (o == null) {
//                    o = NULL;
//                }
//                entities.put(missingKeys[i], o);
//            }

            int missingCount = fill(result, keys);
            assert missingCount == 0;
        }
        
        private Object[] rawKeys(final Object[] keys) {
            final Object[] result = new Object[keys.length];
            for (int i = 0; i < keys.length; i++) {
                result[i] = ((MultiKey) keys[i]).keys;
            }
            return result;
        }
        
        private void fetchValues(Object[] missingKeys) throws SQLException {
            if (compositeKey) missingKeys = rawKeys(missingKeys);
            view.select(cnn, null)
                .byKeys(Graph.this, missingKeys)
                .asList().execute();
        }
    }

    protected static class MultiKey {

        private Object[] keys;
        private int hash = -1;

        public MultiKey(Object[] ids) {
            this.keys = ids.clone();
        }

        public MultiKey() {
            keys = null;
        }

        private void become(Object key) {
            this.keys = (Object[]) key;
            hash = -1;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MultiKey)) {
                return false;
            }
            final MultiKey o = (MultiKey) obj;
            return Arrays.equals(keys, o.keys);
        }

        @Override
        public int hashCode() {
            if (hash == -1) {
                hash = Arrays.hashCode(keys);
                if (hash == -1) {
                    hash = 0;
                }
            }
            return hash;
        }
    }
    
    private static final Object NULL = new Object();
    
    protected static class InsertIntoGraphAdapter<Entity> extends ValueAdapterBase<Entity> {
        
        private final EntitySet es;
        private final String[] keyFields;
        private int[] keyIndices;
        private ResultSet rs;

        public InsertIntoGraphAdapter(Graph graph, View<? extends SelectByKey<?>> view, String[] keyFields) {
            this.keyFields = keyFields;
            this.es = graph.entitySet(view, keyFields.length > 1 ? keyFields : "");
        }

        @Override
        public void initialize(ResultSet rs) throws SQLException {
            keyIndices = getFieldIndices(rs, keyFields);
            this.rs = rs;
        }

        @Override
        public void apply(Entity entity) throws SQLException {
            if (keyFields.length > 1) {
                final Object[] key = new Object[keyFields.length];
                for (int i = 0; i < key.length; i++) {
                    key[i] = rs.getObject(keyIndices[i]);
                }
                es.put(key, entity);
            } else {
                Object key = rs.getObject(keyIndices[0]);
                es.put(key, entity);
            }
        }

        @Override
        public void complete() throws SQLException {
        }

        @Override
        public void close() throws SQLException {
        }        
    }
    
}
