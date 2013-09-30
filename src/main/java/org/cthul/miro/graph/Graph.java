package org.cthul.miro.graph;

import java.sql.SQLException;
import java.util.*;
import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.View;

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
    
    public Object peekObject(View<? extends SelectByKey<?>> view, Object key) {
        return entitySet(view, key).peek(key);
    }

    public List<Object> getObjects(View<? extends SelectByKey<?>> view, List<?> keys) throws SQLException {
        if (keys.isEmpty()) return Collections.emptyList();
        return entitySet(view, keys.get(0)).getObjects(keys);
    }

//    /* GraphQuery */ <Entity> ResultBuilder.ValueAdapter<Entity> valueAdapter(View<? extends SelectByKey<?>> view, String[] keyFields) {
//        return new InsertIntoGraphAdapter<>(this, view, keyFields);
//    }
//    
    protected class EntitySet implements KeyMap.Fetch<Object, SQLException> {
        
        private final View<? extends SelectByKey<?>> view;
        private final KeyMap<Object,?,SQLException> map;

        public EntitySet(View<? extends SelectByKey<?>> view, Object exampleKey) {
            this.view = view;
            if (exampleKey instanceof Object[]) {
                map = new MultiKeyMap(this);
            } else {
                map = new SingleKeyMap(this);
            }
        }

        @Override
        public Object[] fetchValues(Object[] keys) throws SQLException {
            return view.select(cnn, SELECT_NONE)
                    .byKeys(Graph.this, keys)
                    .asOrderedArray().execute();
        }

        private List<Object> getObjects(List<?> keys) throws SQLException {
            return Arrays.asList(map.getAll(keys.toArray()));
        }

        private Object peek(Object key) {
            return map.peek(key);
        }
    }
    
    private static final String[] SELECT_NONE = {};
    
//    protected static class InsertIntoGraphInit<Entity> extends EntityBuilderBase implements EntityInitializer<Entity> {
//        
//        private final EntitySet es;
//        private final String[] keyFields;
//        private final int[] keyIndices;
//        private final ResultSet rs;
//
//        public InsertIntoGraphInit(Graph graph, View<? extends SelectByKey<?>> view, String[] keyFields, ResultSet rs) throws SQLException {
//            this.keyFields = keyFields;
//            this.es = graph.entitySet(view, keyFields.length > 1 ? keyFields : "");
//            this.rs = rs;
//            this.keyIndices = getFieldIndices(rs, keyFields);
//        }
//
//        @Override
//        public void apply(Entity entity) throws SQLException {
//            if (keyFields.length > 1) {
//                final Object[] key = new Object[keyFields.length];
//                for (int i = 0; i < key.length; i++) {
//                    key[i] = rs.getObject(keyIndices[i]);
//                }
//                es.put(key, entity);
//            } else {
//                Object key = rs.getObject(keyIndices[0]);
//                es.put(key, entity);
//            }
//        }
//
//        @Override
//        public void complete() throws SQLException {
//        }
//
//        @Override
//        public void close() throws SQLException {
//        }        
//    }
//    
}
