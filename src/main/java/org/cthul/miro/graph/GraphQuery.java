package org.cthul.miro.graph;

import org.cthul.miro.MiConnection;
import org.cthul.miro.map.*;
import org.cthul.miro.query.QueryTemplate;

/**
 *
 */
public class GraphQuery<Entity> extends MappedTemplateQuery<Entity> 
                                implements SelectByKey<Entity> {

    public GraphQuery(MiConnection cnn, MappedTemplate<Entity> template) {
        super(cnn, template);
    }

    public GraphQuery(MiConnection cnn, Mapping<Entity> mapping, QueryTemplate template) {
        super(cnn, mapping, template);
    }
//
//    private final GraphQueryTemplate<Entity> t;
//    private final View<? extends SelectByKey<?>> view;
//    private GraphAdapterPart<Entity> graphAdapter = null;
////    private Graph graph = null;
//    private Entity[] intoValues;
//    private Object[] keys;
//    
//    public GraphQuery(MiConnection cnn, Mapping<Entity> mapping, GraphQueryTemplate<Entity> template, View<? extends SelectByKey<?>> view) {
//        super(cnn, mapping, template);
//        this.t = template;
//        this.view= view;
//    }
//
//    @Override
//    public GraphQuery<Entity> into(Graph graph, Entity... values) {
//        graphAdapter().setGraph(graph);
//        this.intoValues = values;
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public GraphQuery<Entity> byKeys(Graph graph, Object... keys) {
//        graphAdapter().setGraph(graph);
//        this.keys = keys;
//        QueryPart<Entity> byKeyCondition = new ByKeyCondition<>(t.getKeys());
//        byKeyCondition.put("", keys);
//        where(byKeyCondition);
//        return this;
//    }
//
//    @Override
//    public SubmittableQuery<Entity[]> asOrderedArray() {
//        ResultBuilder<Entity[], Entity> arrayByKey = new ArrayByKeyResult<>(t.getKeys(), keys, mapping);
//        return as(arrayByKey);
//    }
//    
//    protected GraphAdapterPart<Entity> graphAdapter() {
//        if (graphAdapter == null) {
//            graphAdapter = new GraphAdapterPart<>(view, t.getKeys());
//            addPart(graphAdapter);
//        }
//        return graphAdapter;
//    }
//
//    @Override
//    protected void addPart(QueryPart qp) throws IllegalArgumentException {
//        super.addPart(qp);
//        if (qp instanceof RelationPart) {
//            RelationPart<Entity> rp = (RelationPart) qp;
//            graphAdapter().selectReleation(rp);
//        }
//    }
//    
//    public static class RelationPart<Entity> extends QueryPart<Entity> {
//
//        private final View<? extends SelectByKey<?>> view;
//        private final String[] refFields;
//        
//        public RelationPart(String key, View<? extends SelectByKey<?>> view, String[] refFields) {
//            super(key);
//            this.view = view;
//            this.refFields = refFields;
//        }
//
//        @Override
//        public PartType getPartType() {
//            return PartType.VIRTUAL;
//        }
//
//        public void addValueAdapters(List<ValueAdapter<? super Entity>> adapters, Graph g, Mapping<Entity> m, MiConnection cnn) {
//            adapters.add(new RelationAdatper<>(g, view, m, key, refFields));
//        }
//        
//    }
//    
//    public static class GraphAdapterPart<Entity> extends QueryPart<Entity> {
//        
//        private final List<RelationPart<Entity>> relations = new ArrayList<>();
//        private final View<? extends SelectByKey<?>> view;
//        private final String[] keyFields;
//        Graph graph = null;
//
//        public GraphAdapterPart(View<? extends SelectByKey<?>> view, String[] keyFields) {
//            super(GRAPH_ADAPTER_KEY);
//            this.view = view;
//            this.keyFields = keyFields;
//        }
//
//        @Override
//        public void put(String subKey, Object[] args) {
//            switch (subKey) {
//                // for now, the setter methods should be enough
////                case "graph":
////                    setGraph((Graph) args[0]);
////                    break;
////                case "relation":
////                    for (Object o: args) {
////                        selectReleation((RelationPart) o);
////                    }
////                    break;
//                default:
//                    super.put(subKey, args);
//                    break;
//            }
//        }
//        
//        public void selectReleation(RelationPart<Entity> r) {
//            relations.add(r);
//        }
//
//        public void setGraph(Graph graph) {
//            this.graph = graph;
//        }
//
//        @Override
//        public void addValueAdapters(List<ValueAdapter<? super Entity>> adapters, Mapping<Entity> m, MiConnection cnn) {
//            Graph g = graph != null ? graph : new Graph(cnn);
//            // 1) allow the graph to learn about new object
//            adapters.add(g.<Entity>valueAdapter(view, keyFields));
//            // 2) fill object's relations
//            for (RelationPart<Entity> rp: relations) {
//                rp.addValueAdapters(adapters, g, m, cnn);
//            }
//        }
//
//        @Override
//        public PartType getPartType() {
//            return PartType.VALUE_ADAPTER;
//        }
//    }
//    
//    protected static final String GRAPH_ADAPTER_KEY = "org/cthul/miro/graph/GraphQuery$GraphAdapterPart";
//    
//    public static class ByKeyCondition<Entity> extends QueryPart<Entity> {
//        
//        private final String[] keyFields;
//
//        public ByKeyCondition(String[] keyFields) {
//            super(BYKEY_CONDITION_KEY);
//            this.keyFields = keyFields;
//        }
//
//        @Override
//        public void appendTo(StringBuilder sb) {
//            final int len = arguments.length;
//            if (keyFields.length > 1) {
//                sb.append('(');
//                for (int i = 0; i < len; i++) {
//                    if (i > 0) sb.append(" OR ");
//                    sb.append('(');
//                    boolean first = true;
//                    for (String k: keyFields) {
//                        if (first) first = false;
//                        else sb.append(" AND ");
//                        sb.append(k);
//                        sb.append(" = ?");
//                    }
//                    sb.append(')');
//                }
//                sb.append(')');
//            } else {
//                sb.append(keyFields[0]);
//                sb.append(" IN (");
//                for (int i = 0; i < len-1; i++) {
//                    sb.append("?, ");
//                }
//                sb.append("?)");
//            }
//        }
//
//        @Override
//        public void addArguments(List<Object> args) {
//            if (keyFields.length > 1) {
//                for (Object a: arguments) {
//                    args.addAll(Arrays.asList((Object[]) a));
//                }
//            } else {
//                super.addArguments(args);
//            }
//        }
//        
//    }
//    
//    protected static final String BYKEY_CONDITION_KEY = "org/cthul/miro/graph/GraphQuery$ByKeyCondition";
//    
//    public static class ArrayByKeyResult<Entity> implements ResultBuilder<Entity[], Entity> {
//        
//        private final String[] keyFields;
//        private final Object[] keys;
//        private final Mapping<Entity> mapping;
//        private final boolean compositeKeys;
//
//        public ArrayByKeyResult(String[] keyFields, Object[] keys, Mapping<Entity> mapping) {
//            this.keyFields = keyFields;
//            this.keys = keys;
//            this.mapping = mapping;
//            this.compositeKeys = keyFields.length > 1;
//        }
//
//        @Override
//        public Entity[] adapt(ResultSet rs, ResultBuilder.EntityFactory<Entity> ef, ValueAdapter<? super Entity> va) throws SQLException {
//            final Entity[] result = mapping.newArray(keys.length);
//            final int[] keyIndices = getFieldIndices(rs, keyFields);
//            final Object[] currentKey = new Object[keyIndices.length];
//            final Map<Integer, Integer> positionByKeyHash = buildPositionByHashMap();
//            try (ValueAdapter<?> a1 = va; EntityFactory<?> a2 = ef; ResultSet a3 = rs) {
//                ef.initialize(rs);
//                va.initialize(rs);
//                while (rs.next()) {
//                    final Entity record = ef.newEntity();
//                    va.apply(record);
//                    for (int i = 0; i < keyIndices.length; i++) {
//                        currentKey[i] = rs.getObject(keyIndices[i]);
//                    }
//                    if (compositeKeys) {
//                        addToResult(record, currentKey, positionByKeyHash, result);
//                    } else {
//                        addToResult(record, currentKey[0], positionByKeyHash, result);
//                    }
//                }
//                va.complete();
//            }
//            return result;
//        }
//        
//        protected int[] getFieldIndices(ResultSet rs, String[] fields) throws SQLException {
//            final int[] indices = new int[fields.length];
//            for (int i = 0; i < indices.length; i++) {
//                indices[i] = rs.findColumn(fields[i]);
//            }
//            return indices;
//        }
//
//        private Map<Integer, Integer> buildPositionByHashMap() {
//            final Map<Integer, Integer> result = new HashMap<>();
//            for (int i = 0; i < keys.length; i++) {
//                Object key = keys[i];
//                int hash = hash(key);
//                while (result.containsKey(hash)) hash++;
//                result.put(hash, i);
//            }
//            return result;
//        }
//
//        private int hash(Object key) {
//            if (compositeKeys) {
//                return Arrays.deepHashCode((Object[]) key);
//            } else {
//                return key.hashCode();
//            }
//        }
//
//        private void addToResult(Entity record, Object[] currentKey, Map<Integer, Integer> positionByKeyHash, Entity[] result) {
//            int hash = Arrays.deepHashCode(currentKey);
//            while (true) {
//                Integer i = positionByKeyHash.get(hash);
//                if (i == null) {
//                    throw new RuntimeException("Unexpected key: " + Arrays.toString(currentKey));
//                }
//                Object[] keyAtIndex = (Object[]) keys[i];
//                if (Arrays.deepEquals(currentKey, keyAtIndex)) {
//                    result[i] = record;
//                    return;
//                }
//                hash++;
//            }
//        }
//        
//        private void addToResult(Entity record, Object currentKey, Map<Integer, Integer> positionByKeyHash, Entity[] result) {
//            int hash = currentKey.hashCode();
//            while (true) {
//                Integer i = positionByKeyHash.get(hash);
//                if (i == null) {
//                    throw new RuntimeException("Unexpected key: " + currentKey);
//                }
//                Object keyAtIndex = keys[i];
//                if (currentKey.equals(keyAtIndex)) {
//                    result[i] = record;
//                    return;
//                }
//                hash++;
//            }
//        }
//    }

    @Override
    public SubmittableQuery<Entity[]> asOrderedArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SelectByKey<Entity> into(Graph graph, Entity... values) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SelectByKey<Entity> byKeys(Graph graph, Object... keys) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
