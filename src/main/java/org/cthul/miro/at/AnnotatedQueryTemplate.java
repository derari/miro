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
import org.cthul.objects.reflection.Signatures;

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
        MiQuery query = getClassQuery(iface);
        if (query != null) {
            readSelects(AUTODETECT_DEPENDENCIES, query.select(), Include.DEFAULT);
            readSelects(AUTODETECT_DEPENDENCIES, query.optional(), Include.OPTIONAL);
            readInternalSelects(AUTODETECT_DEPENDENCIES, query.internal());
            List<PartTemplate> always = readMore(query.always(), iface);
            always(getPartKeys(always));
            List<PartTemplate> byDefault = readMore(query.byDefault(), iface);
            byDefault(getPartKeys(byDefault));
            
            String from = query.from();
            if (!from.isEmpty()) {
                from(from);
            }
            
            readJoins(NO_DEPENDENCIES, query.join());
            readWhere(NO_DEPENDENCIES, query.where(), iface);
            readOrderBy(AUTODETECT_DEPENDENCIES, query.orderBy());
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
    
    private void readSelects(String[] required, Select[] selects, Include include) {
        for (Select s: selects) {
            readSelect(required, s, include);
        } 
    }
    
    private void readSelects(List<PartTemplate> bag, String[] required, Select[] selects, Include include) {
        for (Select s: selects) {
            bag.addAll(readSelect(required, s, include));
        } 
    }
    
    private List<PartTemplate> readSelect(String[] required, Select select, Include include) {
        String key = select.key();
        if (key.isEmpty()) {
            return select(required, include, select.value());
        } else {
            String[] selects = select.value();
            if (selects.length != 1) {
                throw new IllegalArgumentException(
                        "Key given, expected exactly one select: " + key);
            }
            return Arrays.asList(select(key, required, include, selects[0]));
        }
    }
    
    private void readInternalSelects(String[] required, Select[] selects) {
        for (Select s: selects) {
            readInternalSelect(required, s);
        } 
    }
    
    private void readInternalSelects(List<PartTemplate> bag, String[] required, Select[] selects) {
        for (Select s: selects) {
            bag.addAll(readInternalSelect(required, s));
        } 
    }
    
    private List<PartTemplate> readInternalSelect(String[] required, Select select) {
        String key = select.key();
        if (key.isEmpty()) {
            return internal_select(required, select.value());
        } else {
            String[] selects = select.value();
            if (selects.length != 1) {
                throw new IllegalArgumentException(
                        "Key given, expected exactly one select: " + key);
            }
            return Arrays.asList(internal_select(key, required, selects[0]));
        }
    }
    
    private void readJoins(String[] required, Join[] joins) {
        for (Join j: joins) {
            readJoin(required, j);
        } 
    }
    
    private void readJoins(List<PartTemplate> bag, String[] required, Join[] joins) {
        for (Join j: joins) {
            bag.addAll(readJoin(required, j));
        } 
    }
    
    private List<PartTemplate> readJoin(String[] required, Join join) {
        String key = join.key();
        if (key.isEmpty()) {
            final List<PartTemplate> list = new ArrayList<>();
            for (String j: join.value()) {
                list.add(join(required, j));
            }
            return list;
        } else {
            String[] joins = join.value();
            KeyMapper km = DEFAULT_KEY;
            if (joins.length != 1 || isKeyTemplate(key)) {
                km = insertKey(key);
            }
            final List<PartTemplate> list = new ArrayList<>();
            for (String j: join.value()) {
                list.add(join(required, km, j));
            }
            return list;
        }
    }
    
    private List<PartTemplate> readWhere(String[] required, Where[] where, Class<?> iface) {
        List<PartTemplate> list = new ArrayList<>();
        for (Where w: where) {
            if (!isDefaultArgsMapping(w.mapArgs())) {
                throw new IllegalArgumentException(
                        "No args mapping in @Where allowed: " + iface);
            }
            if (w.args().length > 0) {
                throw new IllegalArgumentException(
                        "No args in @Where allowed: " + iface);
            }
            list.addAll(readWhere(required, w));
        }
        return list;
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
    
    private void readOrderBy(String[] required, OrderBy[] orderBy) {
        for (OrderBy o: orderBy) {
            readOrderBy(required, o);
        }
    }
    
    private void readOrderBys(List<PartTemplate> bag, String[] required, OrderBy[] orderBy) {
        for (OrderBy o: orderBy) {
            bag.addAll(readOrderBy(required, o));
        }
    }
    
    private List<PartTemplate> readOrderBy(String[] required, OrderBy atOrderBy) {
        String key = atOrderBy.key();
        String[] clauses = atOrderBy.value();
        if (clauses.length == 1) {
            if (key.isEmpty()) {
                return Arrays.asList(orderBy(required, clauses[0]));
            } else if (isKeyTemplate(key)) {
                return Arrays.asList(orderBy(required, insertKey(key), clauses[0]));
            } else {
                return Arrays.asList(orderBy(required, key, clauses[0]));
            }
        } else if (clauses.length == 0) {
            return Collections.emptyList();
        } else {
            if (key.isEmpty()) {
                return orderBy(required, clauses);
            } else {
                KeyMapper km = insertKey(key);
                return orderBy(required, km, clauses);
            }
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
    
    private List<PartTemplate> readMore(More[] more, Class<?> iface) {
        final List<PartTemplate> list = new ArrayList<>();
        for (More m: more) {
            list.add(readMore(m, iface, null));
        }
        return list;
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
        More more = getMethodMore(m);
        readMore(more, null, m);
    }
    
    protected PartTemplate readMore(More more, Class<?> iface, Method m) {
        InvocationBuilder<Entity> invBuilder = null;
        List<PartTemplate> parts = new ArrayList<>();
        String[] required = more.using();
        if (required.length > 0) {
            required = readRequired(required);
            PartTemplate pt = virtualPart(required);
            // make sure all query parts depend on `required`,
            // but it is also added by the method call
            parts.add(pt);
            required = new String[]{pt.getKey()};
        }

        readSelects(parts, required, more.select(), Include.DEFAULT);
        readSelects(parts, required, more.optional(), Include.OPTIONAL);
        readInternalSelects(parts, required, more.internal());
        
        readJoins(parts, required, more.join());
        readOrderBys(parts, required, more.orderBy());
        
        Where[] wheres = more.where();
        Config[] configs = more.config();
        Put[] puts = more.put();
        List<PartTemplate> whereParts = null;
        if (wheres.length > 0) {
            if (iface != null) {
                readWhere(required, wheres, iface);
            } else {
                whereParts = readWhere(required, wheres);
            }
        }
        List<PartTemplate> configParts = null;
        if (configs.length > 0) {
            configParts = readConfig(required, configs);
        }
        if (m == null && puts.length > 0) {
            throw new IllegalArgumentException("@Put not allowed at class");
        }
        if (m != null) {
            Impl impl = m.getAnnotation(Impl.class);
            if (wheres.length > 0 || configs.length > 0 || puts.length > 0) {
                if (impl != null) {
                    throw new IllegalArgumentException(
                            "No other annotations allowed with @Impl: " + m);
                }
                invBuilder = buildInvocation(m, wheres, whereParts, configs, configParts, puts);
            } else if (impl != null) {
                invBuilder = buildInvocation(m, impl);
            }
        }
        
        final PartTemplate requiredPart;
        if (parts.isEmpty()) {
            requiredPart = null;
        } else if (parts.size() == 1) {
            requiredPart = parts.get(0);
        } else {
            String[] keys = getPartKeys(parts);
            requiredPart = virtualPart(keys);
        }
        
        if (m != null) {
            if (requiredPart != null) {
                if (invBuilder == null) {
                    invBuilder = new CallPutTemplate<>();
                }
                invBuilder.setRequired(requiredPart.getKey());
            }
            if (invBuilder != null) {
                handlers.put(m, invBuilder);
            }
        }
        
        return requiredPart;
    }
    
    private boolean isKeyTemplate(String key) {
        return key.contains("$1");
    }
    
    private KeyMapper insertKey(final String template) {
        if (!isKeyTemplate(template)) {
            throw new IllegalArgumentException(
                    "Expected key template containing '$1': " + template);
        }
        return new KeyMapper() {
            @Override
            public String map(String key) {
                return template.replace("$1", key);
            }
        };
    }
    
    private MiQuery getClassQuery(final Class<?> c) {
        MiQuery _q = c.getAnnotation(MiQuery.class);
        final MiQuery query = _q != null ? _q : NO_QUERY;
        return new MiQuery() {
            @Override
            public Select[] select() {
                return getAnnotations(c, Select.class, query.select());
            }
            @Override
            public Select[] optional() {
                return query.optional();
            }
            @Override
            public Select[] internal() {
                return query.internal();
            }
            @Override
            public String from() {
                From atFrom = c.getAnnotation(From.class);
                String from = query.from();
                if (atFrom != null && !from.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Expected only one of @From an @MiQuery.from: " + c);
                }
                if (atFrom != null) {
                    return atFrom.value();
                }
                return from;
            }
            @Override
            public More[] always() {
                return query.always();
            }
            @Override
            public More[] byDefault() {
                return query.byDefault();
            }
            @Override
            public Join[] join() {
                return getAnnotations(c, Join.class, query.join());
            }
            @Override
            public Where[] where() {
                return getAnnotations(c, Where.class, query.where());
            }
            @Override
            public OrderBy[] orderBy() {
                return getAnnotations(c, OrderBy.class, query.orderBy());
            }
            @Override
            public Config[] config() {
                return getAnnotations(c, Config.class, query.config());
            }
            @Override
            public More[] more() {
                return getAnnotations(c, More.class, query.more()); 
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return MiQuery.class;
            }
        };
    }
    
    private More getMethodMore(final Method m) {
        More _m = m.getAnnotation(More.class);
        final More more = _m != null ? _m : NO_MORE;
        return new More() {
            @Override
            public String key() {
                return more.key();
            }
            @Override
            public String[] using() {
                org.cthul.miro.at.Using atUsing = m.getAnnotation(org.cthul.miro.at.Using.class);
                String[] using = more.using();
                if (atUsing != null && using.length > 0) {
                    throw new IllegalArgumentException(
                    "Expected only one of @Using"
                    + " and @More.using: " + m);
                }
                if (atUsing != null) {
                    return atUsing.value();
                } else {
                    return using;
                }
            }
            @Override
            public Select[] select() {
                return getAnnotations(m, Select.class, more.select());
            }
            @Override
            public Select[] optional() {
                return more.optional();
            }
            @Override
            public Select[] internal() {
                return more.internal();
            }
            @Override
            public Join[] join() {
                return getAnnotations(m, Join.class, more.join());
            }
            @Override
            public Where[] where() {
                return getAnnotations(m, Where.class, more.where());
            }
            @Override
            public OrderBy[] orderBy() {
                return getAnnotations(m, OrderBy.class, more.orderBy());
            }
            @Override
            public Config[] config() {
                return getAnnotations(m, Config.class, more.config());
            }
            @Override
            public Put[] put() {
                return getAnnotations(m, Put.class, more.put());
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return More.class;
            }
        };
    }
    
    private <T extends Annotation> T[] getAnnotations(Method m, Class<T> clazz, T[] all) {
        T at = m.getAnnotation(clazz);
        if (at != null && all.length > 0) {
            String n = clazz.getSimpleName();
            throw new IllegalArgumentException(
                    "Expected only one of @" + n
                    + " and @More." + n.toLowerCase() + ": " + m);
        }
        if (at != null) {
            all = Arrays.copyOf(all, 1);
            all[0] = at;
        }
        return all;
    }
    
    private <T extends Annotation> T[] getAnnotations(Class<?> iface, Class<T> clazz, T[] all) {
        T at = iface.getAnnotation(clazz);
        if (at != null && all.length > 0) {
            String n = clazz.getSimpleName();
            throw new IllegalArgumentException(
                    "Expected only one of @" + n
                    + " and @MiQuery." + n.toLowerCase() + ": " + iface);
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
        return new CallPutTemplate<>(
                keys.toArray(new String[keys.size()]),
                args.toArray(new Arg[args.size()][]),
                maps.toArray(new int[maps.size()][]));
    }
    
    private InvocationBuilder<Entity> buildInvocation(Method m, Impl impl) {
        return new CallImplTemplate<>(m, impl);
    }
        
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
    
    private static final String[] NO_STRINGS = {};
    private static final Select[] NO_SELECTS = {};
    private static final Join[] NO_JOINS = {};
    private static final Where[] NO_WHERES = {};
    private static final OrderBy[] NO_ORDERBY = {};
    private static final Config[] NO_CONFIGS = {};
    private static final Put[] NO_PUTS = {}; 
    private static final More[] NO_MORES = {}; 

    private static final More NO_MORE = new More() {
        @Override
        public String key() { return ""; }
        @Override
        public String[] using() { return NO_STRINGS; }
        @Override
        public Select[] select() { return NO_SELECTS; }
        @Override
        public Select[] optional() { return NO_SELECTS; }
        @Override
        public Select[] internal() { return NO_SELECTS; }
        @Override
        public Join[] join() { return NO_JOINS; }
        @Override
        public Where[] where() { return NO_WHERES; }
        @Override
        public OrderBy[] orderBy() { return NO_ORDERBY; }
        @Override
        public Config[] config() { return NO_CONFIGS; }
        @Override
        public Put[] put() { return NO_PUTS; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return More.class;
        }
    };
    
    private static final MiQuery NO_QUERY = new MiQuery() {
        @Override
        public Select[] select() { return NO_SELECTS; }
        @Override
        public Select[] optional() { return NO_SELECTS; }
        @Override
        public Select[] internal() { return NO_SELECTS; }
        @Override
        public String from() { return ""; }
        @Override
        public More[] always() { return NO_MORES; }
        @Override
        public More[] byDefault() { return NO_MORES; }
        @Override
        public Join[] join() { return NO_JOINS; }
        @Override
        public Where[] where() { return NO_WHERES; }
        @Override
        public OrderBy[] orderBy() { return NO_ORDERBY; }
        @Override
        public Config[] config() { return NO_CONFIGS; }
        @Override
        public More[] more() { return NO_MORES; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return MiQuery.class;
        }
    };

    private String[] getPartKeys(List<PartTemplate> parts) {
        String[] keys = new String[parts.size()];
        int i = 0;
        for (PartTemplate pt: parts) {
            keys[i++] = pt.getKey();
        }
        return keys;
    }
    
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
            return new CallPut(this, query);
        }
    }
    
    private static class CallPut implements InvocationHandler {
        private final String[] keys;
        private final Arg[][] args;
        private final int[][] argIndices;
        private final String required;
        private final AnnotatedQueryHandler<?> handler;

        public CallPut(CallPutTemplate<?> template, AnnotatedQueryHandler<?> handler) {
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
            }
            return proxy;
        }
    }
    
    private static class CallImplTemplate<Entity> implements InvocationBuilder<Entity> {
        private final Class<?> impl;
        private final Method[] methods;
        private final Arg[] args;
        private final int[] mapArgs;
        private String required = null;

        public CallImplTemplate(Method m, Impl impl) {
            String m0 = impl.method();
            if (m0.isEmpty()) {
                m0 = m.getName();
            }
            this.impl = impl.value();
            this.methods = Signatures.collectMethods(
                    this.impl, m0, 
                    Signatures.STATIC | Signatures.PUBLIC, 
                    Signatures.NONE);
            this.args = impl.args();
            this.mapArgs = impl.mapArgs();
        }

        @Override
        public InvocationHandler build(AnnotatedQueryHandler<Entity> query) {
            return new CallImpl(this, query);
        }

        @Override
        public void setRequired(String key) {
            this.required = key;
        }
    }
    
    private static class CallImpl implements InvocationHandler {
        private final CallImplTemplate<?> template;
        private final AnnotatedQueryHandler<?> handler;

        public CallImpl(CallImplTemplate<?> template, AnnotatedQueryHandler<?> handler) {
            this.template = template;
            this.handler = handler;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            args = getActualArgs(template.args, template.mapArgs, args, method);
            Object[] actualArgs = new Object[args.length+1];
            actualArgs[0] = handler;
            System.arraycopy(args, 0, actualArgs, 1, args.length);
            
            Method m = Signatures.bestMethod(template.methods, actualArgs);
            if (m == null) {
                throw new RuntimeException(
                        "Impl not found: " + method.getName() +
                        "(" + Arrays.toString(actualArgs) + ")");
            }
            m.setAccessible(true);
            m.invoke(null, actualArgs);
            
            return proxy;
        }   
    }
}
