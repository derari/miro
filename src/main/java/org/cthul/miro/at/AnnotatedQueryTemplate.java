package org.cthul.miro.at;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import org.cthul.miro.MiConnection;
import org.cthul.miro.graph.GraphQueryTemplate;
import org.cthul.miro.map.ConfigurationProvider;
import org.cthul.miro.map.ConfigurationInstance;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.query.QueryBuilder;
import org.cthul.miro.result.EntityConfiguration;
import org.cthul.objects.instance.Arg;
import org.cthul.objects.instance.*;

/**
 *
 */
public class AnnotatedQueryTemplate<Entity> extends GraphQueryTemplate<Entity> {
    
    private final Map<Method, InvocationBuilder<Entity>> handlers = new HashMap<>();
    private final Set<Class<?>> interfaces = new HashSet<>();
    private int generatedIDs = 0;

    public AnnotatedQueryTemplate(Mapping<Entity> mapping) {
        super(mapping);
    }

    public AnnotatedQueryTemplate() {
    }
    
    protected String newKey(String name) {
        return name + "$" + (generatedIDs++);
    }
    
    public Map<Method, InvocationHandler> getHandlers(AnnotatedQueryHandler<Entity> handler) {
        Map<Method, InvocationHandler> map = new HashMap<>();
        for (Map.Entry<Method, InvocationBuilder<Entity>> e: handlers.entrySet()) {
            map.put(e.getKey(), e.getValue().build(handler));
        }
        return map;
    }
    
    protected void addInterface(Class<?> iface) {
        readClassAnnotations(iface);
    }

    protected void readClassAnnotations(Class<?> iface) {
        if (!interfaces.add(iface)) {
            return;
        }
        MiQuery query = iface.getAnnotation(MiQuery.class);
        if (query != null) {
            select(query.select());
            optional_select(query.opt_select());
            internal_select(query.int_select());
            always(query.always());
            byDefault(query.byDefault());
            
            String from = query.from();
            if (!from.isEmpty()) {
                from(from);
            }
            
            readJoins(NO_DEPENDENCIES, query.join());
            readWhere(NO_DEPENDENCIES, query.where(), iface);
            readConfig(NO_DEPENDENCIES, query.config());
            readMore(query.more(), iface);
        }
        for (Class<?> supI: iface.getInterfaces()) {
            readClassAnnotations(supI);
        }
        for (Method m: iface.getDeclaredMethods()) {
            readMethodAnnotations(m);
        }
    }
    
    private void readJoins(String[] required, Join[] joins) {
        for (Join j: joins) {
            readJoin(required, j);
        } 
    }
    
    private void readJoins(List<PartTemplate> bag, String[] required, Join[] joins) {
        for (Join j: joins) {
            bag.add(readJoin(required, j));
        } 
    }
    
    private PartTemplate readJoin(String[] required, Join join) {
        String key = join.key();
        if (key.isEmpty()) {
            return join(required, join.value());
        } else {
            return join(required, key, join.value());
        }
    }
    
    private void readWhere(String[] required, Where[] where, Class<?> iface) {
        for (Where w: where) {
            if (!isDefaultArgsMapping(w.mapArgs())) {
                throw new IllegalArgumentException(
                        "No args mapping in @Where allowed: " + iface);
            }
            if (w.args().length > 0) {
                throw new IllegalArgumentException(
                        "No args in @Where allowed: " + iface);
            }
            readWhere(required, w);
        }
    }
    
    private List<PartTemplate> readWhere(String[] required, Where[] where) {
        List<PartTemplate> list = new ArrayList<>();
        for (Where w: where) {
            list.addAll(readWhere(required, w));
        }
        return list;
    }
    
    private List<PartTemplate> readWhere(String[] required, Where atWhere) {
        String key = atWhere.key();
        String[] clauses = atWhere.value();
        if (clauses.length == 1) {
            if (key.isEmpty()) {
                return Arrays.asList(where(required, clauses[0]));
            } else {
                return Arrays.asList(where(required, key, clauses[0]));
            }
        } else if (clauses.length == 0) {
            return Collections.emptyList();
        } else {
            if (!key.isEmpty()) {
                throw new IllegalArgumentException(
                        "No key for multiple clauses allowed: " + key);
            }
            if (!isDefaultArgsMapping(atWhere.mapArgs())) {
                throw new IllegalArgumentException(
                        "Invalid args mapping in @Where, use @All instead: " + clauses[0]);
            }
            return where(required, clauses);
        }
    }
    
    private List<PartTemplate> readConfig(String[] required, Config[] configs) {
        List<PartTemplate> list = new ArrayList<>();
        for (Config c: configs) {
            list.add(readConfig(required, c));
        }
        return list;
    }
    
    private PartTemplate readConfig(String[] required, Config atConfig) {
        String key = atConfig.key();
        if (key.isEmpty()) {
            key = newKey("config");
        }
        PartTemplate cfgTemplate = new AtConfigPartTemplate(atConfig, key, Include.OPTIONAL, required);
        return addPart(cfgTemplate);
    }
    
    private void readMore(More[] more, Class<?> iface) {
        for (More m: more) {
            String[] required = readRequired(m.using());
            select(required, m.select());
            optional_select(required, m.opt_select());
            internal_select(required, m.int_select());
            readJoins(required, m.join());
            readWhere(required, m.where(), iface);
            readConfig(required, m.config());
        }
    }
    
    private String[] readRequired(String... required) {
        for (String s: required) {
            if (s.contains(",")) {
                return splitRequired(required);
            }
        }
        return required;
    }

    private String[] splitRequired(String[] required) {
        List<String> list = new ArrayList<>();
        for (String s: required) {
            String[] parts = s.split(",");
            for (String p: parts) {
                list.add(p.trim());
            }
        }
        return list.toArray(new String[list.size()]);
    }
    
    protected void readMethodAnnotations(Method m) {
        InvocationBuilder<Entity> invBuilder = null;
        List<PartTemplate> parts = new ArrayList<>();
        String[] required = NO_DEPENDENCIES;
        org.cthul.miro.at.Using atUsing = m.getAnnotation(org.cthul.miro.at.Using.class);
        if (atUsing != null) {
            required = readRequired(atUsing.value());
            PartTemplate pt = virtualPart(required);
            // make sure all query parts depend on `required`,
            // but it is also added by the method call
            parts.add(pt);
            required = new String[]{pt.getKey()};
        }
        All atAll = getAtAll(m);
        Select[] selects = getAnnotations(m, Select.class, atAll.select());
        for (Select atSelect: selects) {
            String key = atSelect.key();
            if (key.isEmpty()) {
                parts.addAll(select(required, atSelect.value()));
            } else {
                parts.add(select(key, required, atSelect.value()));
            }
        }
        
        Join[] joins = getAnnotations(m, Join.class, atAll.join());
        readJoins(parts, required, joins);
        
        Where[] wheres = getAnnotations(m, Where.class, atAll.where());
        Config[] configs = getAnnotations(m, Config.class, atAll.config());
        Put[] puts = getAnnotations(m, Put.class, atAll.put());
        List<PartTemplate> whereParts = null;
        if (wheres.length > 0) {
            whereParts = readWhere(required, wheres);
        }
        List<PartTemplate> configParts = null;
        if (configs.length > 0) {
            configParts = readConfig(required, configs);
        }
        if (wheres.length > 0 || configs.length > 0 || puts.length > 0) {
            invBuilder = buildInvocation(m, wheres, whereParts, configs, configParts, puts);
        }
        
        final String requiredKey;
        if (parts.isEmpty()) {
            requiredKey = null;
        } else if (parts.size() == 1) {
            requiredKey = parts.get(0).getKey();
        } else {
            String[] keys = new String[parts.size()];
            int i = 0;
            for (PartTemplate pt: parts) {
                keys[i++] = pt.getKey();
            }
            requiredKey = virtualPart(keys).getKey();
        }
        
        if (requiredKey != null) {
            if (invBuilder == null) {
                invBuilder = new CallPutTemplate<>();
            }
            invBuilder.setRequired(requiredKey);
        }
        if (invBuilder != null) {
            handlers.put(m, invBuilder);
        }
    }
    
    private All getAtAll(Method m) {
        All atAll = m.getAnnotation(All.class);
        if (atAll != null) {
            return atAll;
        } else {
            return NO_ALL;
        }
    }
    
    private <T extends Annotation> T[] getAnnotations(Method m, Class<T> clazz, T[] all) {
        T at = m.getAnnotation(clazz);
        if (at != null && all.length > 0) {
            String n = clazz.getSimpleName();
            throw new IllegalArgumentException(
                    "Expected only one of @" + n
                    + " and @All." + n.toLowerCase() + ": " + m);
        }
        if (at != null) {
            all = Arrays.copyOf(all, 1);
            all[0] = at;
        }
        return all;
    }
    
    private static boolean isDefaultArgsMapping(int[] args) {
        return args.length == 1 && args[0] == Integer.MIN_VALUE;
    }

    private InvocationBuilder<Entity> buildInvocation(Method m, Where[] wheres, List<PartTemplate> whereParts, Config[] configs, List<PartTemplate> configParts, Put[] puts) {
        final List<String> keys = new ArrayList<>();
        final List<int[]> maps = new ArrayList<>();
        final List<Arg[]> args = new ArrayList<>();
        for (int i = 0; i < wheres.length; i++) {
            keys.add(whereParts.get(i).getKey());
            maps.add(wheres[i].mapArgs());
            args.add(wheres[i].args());
        }
        for (int i = 0; i < configs.length; i++) {
            keys.add(configParts.get(i).getKey());
            maps.add(configs[i].mapArgs());
            args.add(configs[i].args());
        }
        for (Put p: puts) {
            keys.add(p.value());
            maps.add(p.mapArgs());
            args.add(p.args());
        }
//        int paramC = m.getParameterTypes().length;
//        collectArgIndices(paramC, maps);
        return new CallPutTemplate<>(
                keys.toArray(new String[keys.size()]),
                args.toArray(new Arg[args.size()][]),
                maps.toArray(new int[maps.size()][]));
    }
    
//    private void collectArgIndices(int paramC, List<int[]> allArgs) {
//        int c = 0;
//        int len = allArgs.size();
//        for (int i = 0; i < len; i++) {
//            int[] args = allArgs.get(i);
//            if (args.length == 1) {
//                int a = args[0];
//                if (a == Integer.MIN_VALUE) {
//                    args = argsMap(c, paramC-c);
//                    c = paramC;
//                } else if (a < 0) {
//                    args = argsMap(c, -a);
//                    c -= a;
//                }
//                allArgs.set(i, args);
//            }
//        }
//    }
//    
//    private int[] argsMap(int start, int len) {
//        int[] ary = new int[len];
//        for (int i = 0; i < len; i++) {
//            ary[i] = start + i;
//        }
//        return ary;
//    }
    
    private static Object[] getActualArgs(Arg[] args, int[] map, Object[] values, Object context) {
        if (args != null && args.length > 0 && !isDefaultArgsMapping(map)) {
            throw new IllegalArgumentException(
                    "Specifying both args and index mapping not allowed."
                    + "Use @Arg.key to map values instead: " + context);
        }
        if (args == null || args.length == 0) {
            return getMappedArgs(map, values, context);
        } else {
            return getAnnotatedArgs(args, values);
        }
    }
    
    private static Object[] getMappedArgs(int[] map, Object[] values, Object context) {
        if (map == null || isDefaultArgsMapping(map)) {
            return values;
        } else {
            final List<Object> result = new ArrayList<>();
            int p = 0;
            for (int m: map) {
                if (m < 0) {
                    p -= m;
                    for (int i = m; i < 0; i++) {
                        result.add(values[p+i]);
                    }
                } else {
                    p = m;
                    result.add(values[m]);
                }
            }
            return result.toArray();
        }
    }
    
    private static Object[] getAnnotatedArgs(Arg[] args, Object[] values) {
        final Object[] result = new Object[args.length];
        Class[] tmp = new Class[args.length];
        Instances.fillArgs(null, args, result, tmp, values);
        return result;
    }
    
    private static final All NO_ALL = new All() {
        private final Select[] select = {};
        private final Join[] join = {};
        private final Where[] where = {};
        private final Put[] put = {};
        private final Config[] config = {};
        @Override
        public Select[] select() { return select; }
        @Override
        public Join[] join() { return join; }
        @Override
        public Where[] where() { return where; }
        @Override
        public Put[] put() { return put; }
        @Override
        public Config[] config() { return config; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return All.class;
        }
    };
    
    protected interface InvocationBuilder<Entity> {
        
        InvocationHandler build(AnnotatedQueryHandler<Entity> query);
        
        void setRequired(String key);
    }
    
    private static class AtConfigPartTemplate extends PartTemplate {
        private final Config config;

        public AtConfigPartTemplate(Config config, String key, Include include, String[] required) {
            super(key, include, required);
            this.config = config;
        }
        
        @Override
        public QueryBuilder.QueryPart createPart(String alias) {
            return new AtConfigQueryPart(config, alias);
        }
    }
    
    private static class AtConfigQueryPart<Entity> extends QueryBuilder.QueryPart implements ConfigurationProvider<Entity> {
        private final Config config;

        public AtConfigQueryPart(Config config, String key) {
            super(key);
            this.config = config;
        }

        @Override
        public <E extends Entity> EntityConfiguration<? super E> getConfiguration(MiConnection cnn, Mapping<E> mapping) {
            Class[] unknownTypes = null;
            Object o = Instances.newInstance(config.impl(), config.factory(), unknownTypes, arguments);
            return ConfigurationInstance.asConfiguration(o, cnn, mapping);
        }

        @Override
        public QueryBuilder.PartType getPartType() {
            return QueryBuilder.PartType.OTHER;
        }
    }
    
    private static class CallPutTemplate<Entity> implements InvocationBuilder<Entity> {
        private final String[] keys;
        private final Arg[][] args;
        private final int[][] argIndices;
        private String required = null;

        public CallPutTemplate() {
            this(new String[0], null, null);
        }

        public CallPutTemplate(String[] keys, Arg[][] args, int[][] argIndices) {
            this.keys = keys;
            this.args = args;
            this.argIndices = argIndices;
        }
        
        @Override
        public void setRequired(String required) {
            this.required = required;
        }

        @Override
        public InvocationHandler build(AnnotatedQueryHandler<Entity> query) {
            return new CallPut<>(this, query);
        }
    }
    
    private static class CallPut<Entity> implements InvocationHandler {
        private final String[] keys;
        private final Arg[][] args;
        private final int[][] argIndices;
        private final String required;
        private final AnnotatedQueryHandler<Entity> handler;

        public CallPut(CallPutTemplate<Entity> template, AnnotatedQueryHandler<Entity> handler) {
            this.keys = template.keys;
            this.args = template.args;
            this.argIndices = template.argIndices;
            this.required = template.required;
            this.handler = handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] callArgs) throws Throwable {
            if (required != null) {
                handler.put(required, (Object[]) null);
            }
            for (int i = 0; i < keys.length; i++) {
                Object[] values = getActualArgs(args[i], argIndices[i], callArgs, method);
                handler.put(keys[i], values);
//                int[] indices = argIndices[i];
//                Object[] values = new Object[indices.length];
//                for (int j = 0; j < indices.length; j++) {
//                    values[j] = args[indices[j]];
//                }
//                handler.put(keys[i], values);
            }
            return proxy;
        }
    }
}
//    private int generatedIDs = 0;
//    
//    private String generateKey(Object owner) {
//        if (owner instanceof Method) {
//            return generateKey((Method) owner);
//        } else if (owner instanceof Class) {
//            return generateKey((Class) owner);
//        } else {
//            return "generated$" + (generatedIDs++);
//        }
//    }
//    
//    private String generateKey(Class c) {
//        String n = c.getSimpleName();
//        if (n == null || n.isEmpty()) n = "generated";
//        return n + "$" + (generatedIDs++);
//    }
//    
//    private String generateKey(Method m) {
//        return m.getName() + "$" + (generatedIDs++);
//    }
