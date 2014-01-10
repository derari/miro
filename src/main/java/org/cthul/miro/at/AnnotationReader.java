package org.cthul.miro.at;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.cthul.miro.MiConnection;
import org.cthul.miro.at.AnnotatedTemplateProvider.InterfaceCall;
import org.cthul.miro.dml.MappedDataQueryTemplateProvider.Using;
import org.cthul.miro.dml.IncludeMode;
import org.cthul.miro.map.ConfigurationInstance;
import org.cthul.miro.map.ConfigurationProvider;
import org.cthul.miro.map.MappedInternalQueryBuilder;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.query.InternalQueryBuilder;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.template.QueryTemplatePart;
import org.cthul.miro.query.template.Templates;
import org.cthul.miro.query.template.UniqueKey;
import org.cthul.miro.result.EntityConfiguration;
import org.cthul.objects.instance.Arg;
import org.cthul.objects.instance.Instances;
import org.cthul.objects.reflection.Signatures;

public class AnnotationReader {

    private final ConcurrentMap<Method, AnnotatedTemplateProvider.InterfaceCall> handlers = new ConcurrentHashMap<>();
    private final Set<Class<?>> interfaces = new HashSet<>();
    private final Map<Object, QueryTemplatePart> templateParts = new HashMap<>();
    private final AnnotatedTemplateProvider provider;
    private final List<Object> keyBag = new ArrayList<>();
    private boolean collectKeys = false;
    private boolean pauseCollectKeys = false;

    public AnnotationReader(AnnotatedTemplateProvider provider) {
        this.provider = provider;
    }
    
    public ConcurrentMap<Method, AnnotatedTemplateProvider.InterfaceCall> getHandlers() {
        return handlers;
    }

    public Map<Object, QueryTemplatePart> getTemplateParts() {
        return templateParts;
    }

    public synchronized void addInterface(Class<?> iface) {
        if (interfaces.add(iface)) {
            for (Class<?> sup: iface.getInterfaces()) {
                addInterface(sup);
            }
            readClassAnnotations(iface);
        }
    }
    
    void keyAdded(Object key) {
        if (collectKeys && !pauseCollectKeys) {
            keyBag.add(key);
        }
    }
    
    private void beginCollectKeys() {
        keyBag.clear();
        collectKeys = true;
    }
    
    private Object[] endCollectKeys() {
        Object[] result = keyBag.toArray();
        keyBag.clear();
        collectKeys = false;
        return result;
    }
    
    private void pauseCollectKeys() {
        pauseCollectKeys = true;
    }

    private void resumeCollectKeys() {
        pauseCollectKeys = false;
    }

    private void readClassAnnotations(Class<?> iface) {
        MiQuery query = getClassQuery(iface);
        Using<?> using = provider.using(AUTODETECT_DEPENDENCIES);
        using.generatedKeys(query.generatedKeys())
                .naturalKeys(query.naturalKeys())
                .attributes(query.attributes())
                .select(query.select())
                .optionalSelect(query.optional())
                .internalSelect(query.internal());
        String table = query.from();
        if (!table.isEmpty()) {
            provider.table(table);
        }
        readMore(miQueryAsMore(query), null, iface);
        readMore(query.always(), "always", "always", IncludeMode.ALWAYS, iface);
        readMore(query.byDefault(), "default", "default", IncludeMode.DEFAULT, iface);
        readMore(query.more(), null, iface);
        Class<?> impl = query.impl();
        for (Method m: iface.getDeclaredMethods()) {
            readMethodAnnotations(m, impl);
        }
    }
    
    private void readMethodAnnotations(Method method, Class<?> defImpl) {
        Impl atImpl = method.getAnnotation(Impl.class);
        More more = getMethodMore(method);
        final Object key = readMore(more, method.getName(), method);
        final InterfaceCall call;
        if (key != null) {
            if (atImpl != null) {
                throw new IllegalArgumentException(
                        "@Impl not allowed with other annotations: " + method);
            }
            call = new InterfaceCall() {
                @Override
                public Object call(AnnotatedQueryHandler<?, ?> handler, InternalQueryBuilder builder, Object[] args) throws Throwable {
                    builder.put(key, args);
                    return handler;
                }
            };
        } else {
            call = implCall(method, atImpl, defImpl);
        }
        if (call != null) {
            handlers.put(method, call);
        }
    }

    private void readMore(More[] more, String generatedKeyName, Object context) {
        for (More m: more) {
            readMore(m, generatedKeyName, context);
        }
    }
    
    private Object readMore(More[] more, String generatedKeyName, String groupKeyName, IncludeMode groupIncludeMode, Object context) {
        List<Object> keys = new ArrayList<>();
        for (More m: more) {
            Object k = readMore(m, generatedKeyName, context);
            if (k != null) {
                keys.add(k);
            }
        }
        if (keys.isEmpty()) return null;
        Object key = new UniqueKey(groupKeyName);
        provider.virtual(groupIncludeMode, keys.toArray(), key);
        return key;
    }
    
    private Object readMore(More more, String generatedKeyName, Object context) {
        Object key = getKey(more.key(), generatedKeyName);
        Object[] required = more.require();
        if (required != null && required.length > 0) {
            Object reqKey = new UniqueKey(key + "-required");
            QueryTemplatePart putRequired = new Templates.RequireAllTemplate(required);
            provider.add(IncludeMode.EXPLICIT, reqKey, putRequired);
            required = new Object[]{reqKey};
        }
        
        Using<?> using = provider.using(required);
        if (key != null) {
            beginCollectKeys();
        }
        
        using.generatedKeys(more.generatedKeys())
                .naturalKeys(more.naturalKeys())
                .attributes(more.attributes())
                .select(more.select())
                .optionalSelect(more.optional())
                .internalSelect(more.internal());
        readJoins(using, more.join());
        readWheres(using, more.where(), context);
        readOrderBys(using, more.orderBy(), context);
        readPuts(using, more.put(), context);
        readConfigs(using, more.config(), context);
        
        if (key != null) {
            Object[] keys = endCollectKeys();
            if (keys.length == 0 && isBlank(more.key())) {
                return null;
            }
            QueryTemplatePart part = new Templates.ProxyTemplate(keys);
            provider.add(IncludeMode.EXPLICIT, key, part);
        }
        return key;
    }
    
    private void readJoins(Using<?> using, Join[] joins) {
        for (Join j: joins) {
            readJoin(using, j);
        }
    }
    
    private void readJoin(Using<?> using, Join j) {
        String key = j.key();
        if (isBlank(key)) {
            for (String s: j.value()) {
                using.join(s);
            }
        } else {
            String s = getSingle(j.value(), "@Join with key");
            using.join(key, s);
        }
    }
    
    private void readWheres(Using<?> using, Where[] wheres, Object context) {
        for (Where w: wheres) {
            readWhere(using, w, context);
        }
    }
    
    private void readWhere(Using<?> using, Where w, Object context) {
        Object key = getUniqueKey(w.key(), "where");
        pauseCollectKeys();
        using.where(key, w.value());
        resumeCollectKeys();
        Object putKey = getKey(w.key(), key.toString());
        QueryTemplatePart putTemplate = new MappedPut(key, w.args(), w.mapArgs(), context);
        provider.add(IncludeMode.EXPLICIT, putKey, putTemplate);
    }
    
    private void readOrderBys(Using<?> using, OrderBy[] orderBys, Object context) {
        for (OrderBy o: orderBys) {
            readOrderBy(using, o, context);
        }
    }
    
    private void readOrderBy(Using<?> using, OrderBy o, Object context) {
        Object key = getKey(o.key(), "orderBy");
        String[] clauses = o.value();
        if (clauses.length == 1) {
            using.orderBy(key, clauses[0]);
        } else {
            pauseCollectKeys();
            List<Object> keys = new ArrayList<>();
            for (String s: clauses) {
                Object k = getUniqueKey(o.key(), "orderBy");
                keys.add(k);
                using.orderBy(k, s);
            }
            resumeCollectKeys();
            provider.virtual(keys.toArray(), key);
        }
    }
    
    private void readPuts(Using<?> using, Put[] puts, Object context) {
        for (Put p: puts) {
            readPut(using, p, context);
        }
    }
    
    private void readPut(Using<?> using, Put p, Object context) {
        Object putKey = new UniqueKey("put");
        QueryTemplatePart putTemplate = new MappedPut(p.value(), p.args(), p.mapArgs(), context);
        provider.add(IncludeMode.EXPLICIT, putKey, putTemplate);
    }
    
    private void readConfigs(Using<?> using, Config[] configs, Object context) {
        for (Config c: configs) {
            readConfig(using, c, context);
        }
    }
    
    private void readConfig(Using<?> using, Config c, Object context) {
        Object cfgKey = getKey(c.key(), "config");
        ConfigurationProvider<?> cfg = new MappedCfg(c.impl(), c.factory(), c.args(), c.mapArgs(), c.cfgArgs(), c.cfgMapArgs(), context);
        using.configure(cfgKey, cfg);
    }
    
    private InterfaceCall implCall(final Method m, Impl atImpl, Class<?> defImpl) {
        Class impl;
        Class[] params;
        String method;
        final Arg[] args;
        final int[] argMap;
        if (atImpl != null) {
            impl = atImpl.value();
            method = atImpl.method();
            if (isBlank(method)) method = m.getName();
            args = atImpl.args();
            argMap = atImpl.mapArgs();
            if (impl == void.class) impl = defImpl;
            params = getParamTypes(args, argMap, m.getParameterTypes(), m);
        } else {
            impl = defImpl;
            method = m.getName();
            args = null;
            argMap = NO_ARGS_MAP;
            params = m.getParameterTypes();
        }
        Class[] params1 = new Class[params.length+1];
        params1[0] = MappedInternalQueryBuilder.class;
        System.arraycopy(params, 0, params1, 1, params.length);
        Method[] candidates = Signatures.collectMethods(impl, method, Signatures.STATIC | Signatures.PUBLIC, Signatures.NONE);
        final Method implM = Signatures.bestMethod(candidates, params1);
        if (implM == null) {
            if (atImpl != null) {
                throw new IllegalArgumentException(
                        "No implementation for " + m);
            }
            return null;
        }
        implM.setAccessible(true);
        return new InterfaceCall() {
            @Override
            public Object call(AnnotatedQueryHandler<?, ?> handler, InternalQueryBuilder builder, Object[] callArgs) throws Throwable {
                callArgs = getActualArgs(args, argMap, callArgs, m);
                Object[] actualArgs = new Object[callArgs.length+1];
                actualArgs[0] = builder;
                System.arraycopy(callArgs, 0, actualArgs, 1, callArgs.length);
                Object result = implM.invoke(null, actualArgs);
                return result != null ? result : handler;
            }
        };
    }
    
    private Object getKey(String name, String generatedKeyName) {
        if (!isBlank(name)) {
            return name;
        }
        if (!isBlank(generatedKeyName)) {
            return new UniqueKey(generatedKeyName);
        }
        return null;
    }
    
    private Object getUniqueKey(String name, String generatedKeyName) {
        return getUniqueKey(name, "", generatedKeyName);
    }
    
    private Object getUniqueKey(String name, String postFix, String generatedKeyName) {
        if (!isBlank(name)) {
            return new UniqueKey(name + postFix);
        }
        if (!isBlank(generatedKeyName)) {
            return new UniqueKey(generatedKeyName);
        }
        return null;
    }
    
    private MiQuery getClassQuery(final Class<?> c) {
        MiQuery _q = c.getAnnotation(MiQuery.class);
        final MiQuery atQuery = _q != null ? _q : NO_QUERY;
        return new MiQuery() {
            @Override
            public String[] generatedKeys() {
                Keys keys = getAnnotation(c, NO_KEYS);
                return getOneOf(atQuery.generatedKeys(), keys.generated(), 
                        "@Keys.generated and @MiQuery.generatedKeys");
            }
            @Override
            public String[] naturalKeys() {
                Keys keys = getAnnotation(c, NO_KEYS);
                return getOneOf(atQuery.naturalKeys(), keys.natural(), 
                        "@Attributes.natural and @MiQuery.naturalKeys");
            }
            @Override
            public String[] attributes() {
                Attributes attr = getAnnotation(c, NO_ATTRIBUTES);
                return getOneOf(atQuery.attributes(), attr.value(), 
                        "@Attributes and @MiQuery.attributes");
            }
            @Override
            public String[] select() {
                Select sel = getAnnotation(c, NO_SELECT);
                return getOneOf(atQuery.attributes(), sel.value(), 
                        "@Select and @MiQuery.select");
            }
            @Override
            public String[] optional() {
                return atQuery.optional();
            }
            @Override
            public String[] internal() {
                return atQuery.internal();
            }
            @Override
            public String from() {
                From from = getAnnotation(c, NO_FROM);
                return getOneOf(atQuery.from(), from.value(), 
                        "@Select and @MiQuery.select");
            }
            @Override
            public More[] always() {
                Always atAlways = c.getAnnotation(Always.class);
                More[] qryAlways = atQuery.always();
                return addToArray(qryAlways, alwaysAsMore(atAlways));
            }
            @Override
            public More[] byDefault() {
                return atQuery.byDefault();
            }
            @Override
            public Join[] join() {
                Join at = c.getAnnotation(Join.class);
                Join[] qry = atQuery.join();
                return addToArray(qry, at);
            }
            @Override
            public Where[] where() {
                Where at = c.getAnnotation(Where.class);
                Where[] qry = atQuery.where();
                return addToArray(qry, at);
            }
            @Override
            public OrderBy[] orderBy() {
                OrderBy at = c.getAnnotation(OrderBy.class);
                OrderBy[] qry = atQuery.orderBy();
                return addToArray(qry, at);
            }
            @Override
            public Config[] config() {
                Config at = c.getAnnotation(Config.class);
                Config[] qry = atQuery.config();
                return addToArray(qry, at);
            }
            @Override
            public More[] more() {
                More at = c.getAnnotation(More.class);
                More[] qry = atQuery.more();
                return addToArray(qry, at);
            }
            @Override
            public Class<?> impl() {
                Impl at = c.getAnnotation(Impl.class);
                Class<?> qry = atQuery.impl();
                if (at != null && qry != void.class) {
                    throw new IllegalArgumentException(
                            "Expected only one of @Impl and @MiQuery.impl");
                }
                if (at != null) {
                    return at.value();
                }
                return qry;
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return MiQuery.class;
            }
        };
    }
        
    private More miQueryAsMore(final MiQuery atQuery) {
        return new More() {
            @Override
            public String key() { return ""; }
            @Override
            public String[] require() { return NO_STRINGS; }
            @Override
            public String[] generatedKeys() { return NO_STRINGS; }
            @Override
            public String[] naturalKeys() { return NO_STRINGS; }
            @Override
            public String[] attributes() { return NO_STRINGS; }
            @Override
            public String[] select() { return NO_STRINGS; }
            @Override
            public String[] optional() { return NO_STRINGS; }
            @Override
            public String[] internal() { return NO_STRINGS; }
            @Override
            public Join[] join() { return atQuery.join(); }
            @Override
            public Where[] where() { return atQuery.where(); }
            @Override
            public OrderBy[] orderBy() { return atQuery.orderBy(); }
            @Override
            public Config[] config() { return atQuery.config(); }
            @Override
            public Put[] put() { return NO_PUTS; }
            @Override
            public Class<? extends Annotation> annotationType() { return More.class; }
        };
    }
    
    private More getMethodMore(final Method m) {
        More _m = m.getAnnotation(More.class);
        final More atMore = _m != null ? _m : NO_MORE;
        return new More() {
            @Override
            public String key() {
                return atMore.key();
            }
            @Override
            public String[] require() {
                Require req = getAnnotation(m, NO_REQUIRE);
                return getOneOf(atMore.require(), req.value(), 
                        "@Require and @More.require");
            }
            @Override
            public String[] generatedKeys() {
                return atMore.generatedKeys();
            }
            @Override
            public String[] naturalKeys() {
                return atMore.naturalKeys();
            }
            @Override
            public String[] attributes() {
                return atMore.attributes();
            }
            @Override
            public String[] select() {
                Select sel = getAnnotation(m, NO_SELECT);
                return getOneOf(atMore.select(), sel.value(),
                        "@Select and @More.select");
            }
            @Override
            public String[] optional() {
                return atMore.optional();
            }
            @Override
            public String[] internal() {
                return atMore.internal();
            }
            @Override
            public Join[] join() {
                Join at = m.getAnnotation(Join.class);
                Join[] qry = atMore.join();
                return addToArray(qry, at);
            }
            @Override
            public Where[] where() {
                Where at = m.getAnnotation(Where.class);
                Where[] qry = atMore.where();
                return addToArray(qry, at);
            }
            @Override
            public OrderBy[] orderBy() {
                OrderBy at = m.getAnnotation(OrderBy.class);
                OrderBy[] qry = atMore.orderBy();
                return addToArray(qry, at);
            }
            @Override
            public Config[] config() {
                Config at = m.getAnnotation(Config.class);
                Config[] qry = atMore.config();
                return addToArray(qry, at);
            }
            @Override
            public Put[] put() {
                Put at = m.getAnnotation(Put.class);
                Put[] qry = atMore.put();
                return addToArray(qry, at);
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return More.class;
            }
        };
    }
    
    private More alwaysAsMore(final Always atAlways) {
        if (atAlways == null) return null;
        return new More() {
            @Override
            public String key() {
                return atAlways.key();
            }
            @Override
            public String[] require() {
                return atAlways.require();
            }
            @Override
            public String[] generatedKeys() {
                return NO_STRINGS;
            }
            @Override
            public String[] naturalKeys() {
                return NO_STRINGS;
            }
            @Override
            public String[] attributes() {
                return NO_STRINGS;
            }
            @Override
            public String[] select() {
                return atAlways.select();
            }
            @Override
            public String[] optional() {
                return NO_STRINGS;
            }
            @Override
            public String[] internal() {
                return atAlways.internal();
            }
            @Override
            public Join[] join() {
                return atAlways.join();
            }
            @Override
            public Where[] where() {
                return atAlways.where();
            }
            @Override
            public OrderBy[] orderBy() {
                return atAlways.orderBy();
            }
            @Override
            public Config[] config() {
                return atAlways.config();
            }
            @Override
            public Put[] put() {
                return atAlways.put();
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return More.class;
            }
        };
    }

    private static <T> T[] getOneOf(T[] a, T[] b, String name) {
        if (a == null || a.length == 0) {
            return b;
        }
        if (b == null || b.length == 0) {
            return a;
        }
        throw new IllegalArgumentException(
                "Expected only one of " + name);
    }
    
    private static <T> T[] addToArray(T[] array, T element) {
        if (element == null) {
            return array;
        }
        array = Arrays.copyOf(array, array.length+1);
        array[array.length-1] = element;
        return array;
    }
    
    private static String getOneOf(String a, String b, String name) {
        if (isBlank(a)) {
            return b;
        }
        if (isBlank(b)) {
            return a;
        }
        throw new IllegalArgumentException(
                "Expected only one of " + name);
    }
    
    private static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }
    
    private static <T> T getSingle(T[] values, String name) {
        if (values.length != 1) {
            throw new IllegalArgumentException(
                    "Expected exactly one in " + name);
        }
        return values[0];
    }
    
    private static boolean isDefaultArgsMapping(int[] args) {
        return args.length == 1 && args[0] == Integer.MIN_VALUE;
    }
    
    private <T extends Annotation> T getAnnotation(AnnotatedElement ae, T def) {
        Object at = ae.getAnnotation(def.annotationType());
        if (at != null) {
            return (T) at;
        } else {
            return def;
        }
    }
    
    private static final int[] NO_INTS = {}; 
    private static final int[] NO_ARGS_MAP = {Integer.MIN_VALUE}; 
    private static final String[] NO_STRINGS = {};
//    private static final Select[] NO_SELECTS = {};
    private static final Join[] NO_JOINS = {};
    private static final Where[] NO_WHERES = {};
    private static final OrderBy[] NO_ORDERBY = {};
    private static final Config[] NO_CONFIGS = {};
    private static final Put[] NO_PUTS = {}; 
    private static final More[] NO_MORES = {}; 
    private static final Arg[] NO_ARGS = {}; 
    private static final Object[] NO_OBJECTS = {};
    
    private static final Object[] NO_DEPENDENCIES = NO_OBJECTS;
    private static final Object[] AUTODETECT_DEPENDENCIES = null;

    private static final Keys NO_KEYS = new Keys() {
        @Override
        public String[] generated() { return NO_STRINGS; }
        @Override
        public String[] natural() { return NO_STRINGS; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return Keys.class;
        }
    };
    
    private static final Attributes NO_ATTRIBUTES = new Attributes() {
        @Override
        public String[] value() { return NO_STRINGS; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return Attributes.class;
        }
    };
    
    private static final Select NO_SELECT = new Select() {
        @Override
        public String[] value() { return NO_STRINGS; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return Select.class;
        }
    };
    
    private static final From NO_FROM = new From() {
        @Override
        public String value() { return ""; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return From.class;
        }
    };
    
    private static final Impl NO_IMPL = new Impl() {
        @Override
        public Class<?> value() { return void.class; }
        @Override
        public String method() { return ""; }
        @Override
        public Arg[] args() { return NO_ARGS; }
        @Override
        public int[] mapArgs() { return NO_ARGS_MAP; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return Impl.class;
        }
    }; 
    
    private static final Require NO_REQUIRE = new Require() {
        @Override
        public String[] value() { return NO_STRINGS; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return Require.class;
        }
    };

    private static final More NO_MORE = new More() {
        @Override
        public String key() { return ""; }
        @Override
        public String[] require() { return NO_STRINGS; }
        @Override
        public String[] generatedKeys() { return NO_STRINGS; }
        @Override
        public String[] naturalKeys() { return NO_STRINGS; }
        @Override
        public String[] attributes() { return NO_STRINGS; }
        @Override
        public String[] select() { return NO_STRINGS; }
        @Override
        public String[] optional() { return NO_STRINGS; }
        @Override
        public String[] internal() { return NO_STRINGS; }
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
        public String[] generatedKeys() { return NO_STRINGS; }
        @Override
        public String[] naturalKeys() { return NO_STRINGS; }
        @Override
        public String[] attributes() { return NO_STRINGS; }
        @Override
        public String[] select() { return NO_STRINGS; }
        @Override
        public String[] optional() { return NO_STRINGS; }
        @Override
        public String[] internal() { return NO_STRINGS; }
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
        public Class<?> impl() { return void.class; }
        @Override
        public Class<? extends Annotation> annotationType() {
            return MiQuery.class;
        }
    };
    
    protected abstract static class ArgMappedTemplate extends Templates.ConfigurationTemplate {
        
        private final Arg[] args;
        private final int[] mapArgs;
        private final Object context;
        private final boolean directPut;

        public ArgMappedTemplate(Arg[] args, int[] mapArgs, Object context) {
            this.args = args;
            this.mapArgs = mapArgs;
            this.context = context;
            this.directPut = isDirectPut(args, mapArgs);
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            if (directPut) {
                mappedPut(queryBuilder, null, null, NO_DEPENDENCIES);
            }
            return super.addPart(key, queryBuilder);
        }

        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object partKey, Object key, Object... callArgs) {
            Object[] values = getActualArgs(args, mapArgs, callArgs, context);
            return mappedPut(queryBuilder, partKey, key, values);
        }
        
        protected abstract boolean mappedPut(InternalQueryBuilder queryBuilder, Object newKey, Object subKey, Object... callArgs);

        private boolean isDirectPut(Arg[] args, int[] mapArgs) {
            if (!isDefaultArgsMapping(mapArgs)) {
                return false;
            }
            for (Arg a: args) {
                if (!isBlank(a.key())) {
                    return false;
                }
            }
            return true;
        }
    }
    
    protected static class MappedPut extends ArgMappedTemplate {
        
        private final Object key;

        public MappedPut(Object key, Arg[] args, int[] mapArgs, Object context) {
            super(args, mapArgs, context);
            this.key = key;
        }

        @Override
        protected boolean mappedPut(InternalQueryBuilder queryBuilder, Object newKey, Object subKey, Object... callArgs) {
            queryBuilder.put(key, callArgs);
            return true;
        }
    }
    
    protected static class MappedCfg<Entity> implements ConfigurationProvider<Entity> {
        
        private final Class<?> impl;
        private final String factory;
        private final Arg[] args;
        private final int[] mapArgs;
        private final Arg[] cfgArgs;
        private final int[] cfgMapArgs;
        private final Object context;

        public MappedCfg(Class<?> impl, String factory, Arg[] args, int[] mapArgs, Arg[] cfgArgs, int[] cfgMapArgs, Object context) {
            this.impl = impl;
            this.factory = factory;
            this.args = args;
            this.mapArgs = mapArgs;
            this.cfgArgs = cfgArgs;
            this.cfgMapArgs = cfgMapArgs;
            this.context = context;
        }

        @Override
        public <E extends Entity> EntityConfiguration<? super E> getConfiguration(MiConnection cnn, Mapping<E> mapping, Object[] callArgs) {
            Object[] fArgs = getActualArgs(args, mapArgs, callArgs, context);
            Object[] cArgs = getActualArgs(cfgArgs, cfgMapArgs, callArgs, context);
            Object o = Instances.newInstance(impl, factory, (Class[]) null, fArgs);
            return ConfigurationInstance.asConfiguration(o, cnn, mapping, cArgs);
        }
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
            if (values == null) return NO_OBJECTS;
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
    
    private static Class[] getParamTypes(Arg[] args, int[] map, Class[] params, Object context) {
        if (args != null && args.length > 0 && !isDefaultArgsMapping(map)) {
            throw new IllegalArgumentException(
                    "Specifying both args and index mapping not allowed."
                    + "Use @Arg.key to map values instead: " + context);
        }
        if (args == null || args.length == 0) {
            return getMappedParams(map, params, context);
        } else {
            return getAnnotatedParams(args, params);
        }
    }
    
    private static Class[] getMappedParams(int[] map, Class[] params, Object context) {
        if (map == null || isDefaultArgsMapping(map)) {
            return params;
        } else {
            final List<Class> result = new ArrayList<>();
            int p = 0;
            for (int m: map) {
                if (m < 0) {
                    p -= m;
                    for (int i = m; i < 0; i++) {
                        result.add(params[p+i]);
                    }
                } else {
                    p = m;
                    result.add(params[m]);
                }
            }
            return result.toArray(new Class[map.length]);
        }
    }

    private static Class[] getAnnotatedParams(Arg[] args, Class[] params) {
        return Instances.getTypes(null, args, params);
    }
}
