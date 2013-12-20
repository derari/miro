package org.cthul.miro.view;

import java.lang.reflect.*;
import java.util.Arrays;
import org.cthul.miro.at.AnnotatedQueryHandler;
import org.cthul.miro.at.AnnotatedTemplateProvider;
import org.cthul.miro.map.MappedQueryStringView;
import org.cthul.miro.map.MappedTemplateProvider;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.query.QueryType;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.template.QueryTemplateProvider;
import org.cthul.miro.result.ResultBuilder;
import org.cthul.miro.result.Results;
import org.cthul.miro.util.GenericsUtils;

public class Views {
    
    public static <V extends View> V newView(Class<V> clazz, Object... args) {
        Class[] ifaces = new Class[4];
        Object[][] ifaceArgs = new Object[4][];
        QueryFactory<?> select = null;
        QueryFactory<?> insert = null;
        QueryFactory<?> update = null;
        QueryFactory<?> delete = null;
        if (ViewC.class.isAssignableFrom(clazz)) {
            Class<?> q = GenericsUtils.returnType(clazz, "insert", String[].class);
            if (q.isInterface()) {
                insert = interfaceFactory(ifaces, ifaceArgs, q, DataQuery.INSERT, args);
            } else {
                insert = new ReflectiveQueryFactory<>(q, DataQuery.INSERT, args);
            }
        }
        if (ViewR.class.isAssignableFrom(clazz)) {
            Class<?> q = GenericsUtils.returnType(clazz, "select", String[].class);
            if (q.isInterface()) {
                select = interfaceFactory(ifaces, ifaceArgs, q, DataQuery.SELECT, args);
            } else {
                select = new ReflectiveQueryFactory<>(q, DataQuery.SELECT, args);
            }
        }
        if (ViewU.class.isAssignableFrom(clazz)) {
            Class<?> q = GenericsUtils.returnType(clazz, "update", String[].class);
            if (q.isInterface()) {
                update = interfaceFactory(ifaces, ifaceArgs, q, DataQuery.UPDATE, args);
            } else {
                update = new ReflectiveQueryFactory<>(q, DataQuery.UPDATE, args);
            }
        }
        if (ViewD.class.isAssignableFrom(clazz)) {
            Class<?> q = GenericsUtils.returnType(clazz, "delete");
            if (q.isInterface()) {
                delete = interfaceFactory(ifaces, ifaceArgs, q, DataQuery.DELETE, args);
            } else {
                delete = new ReflectiveQueryFactory<>(q, DataQuery.DELETE, args);
            }
        }
        Class[] viewApi = {clazz};
        InvocationHandler handler = new ProxyHandlerView(clazz, insert, select, update, delete);
        return (V) Proxy.newProxyInstance(clazz.getClassLoader(), viewApi, handler);
    }
    
    public static <Result> MappedQueryStringView<Result> query(Mapping<?> mapping, ResultBuilder<Result, ?> resultBuilder, String query) {
        return new MappedQueryStringView<>(query, mapping, resultBuilder);
    }
    
    public static MappedQueryStringView<Results> query(Mapping<?> mapping, String query) {
        return new MappedQueryStringView<>(query, mapping);
    }

//    public static class ViewBuilder<E, C, R, U, D, RS> {
//        
//        private final MappedDataQueryTemplateProvider<E> provider;
//        private final ResultBuilder<RS, E> resultBuilder;
//        private QueryFactory<C> insert;
//        private QueryFactory<R> select;
//        private QueryFactory<U> update;
//        private QueryFactory<D> delete;
//
//        public ViewBuilder(MappedDataQueryTemplateProvider<E> provider, ResultBuilder<RS, E> resultBuilder) {
//            this.provider = provider;
//            this.resultBuilder = resultBuilder;
//        }
//
//        public ViewBuilder(MappedDataQueryTemplateProvider<E> provider) {
//            this(provider, null);
//        }
//        
//        private <T> T self() {
//            return (T) this;
//        }
//        
//        private <Q> QueryFactory<Q> qf(Class<Q> queryClass) {
//            return new ReflectiveQueryFactory<>(queryClass, DataQuery.SELECT, provider, resultBuilder);
//        }
//        
//        public <C2> ViewBuilder<E, C2, R, U, D, RS> c(Class<C2> queryClass) {
//            ViewBuilder<E, C2, R, U, D, RS> self = self();
//            self.insert = qf(queryClass);
//            return self;
//        }
//        
//        public <R2> ViewBuilder<E, C, R2, U, D, RS> r(Class<R2> queryClass) {
//            ViewBuilder<E, C, R2, U, D, RS> self = self();
//            self.select = qf(queryClass);
//            return self;
//        }
//        
//        public <U2> ViewBuilder<E, C, R, U2, D, RS> u(Class<U2> queryClass) {
//            ViewBuilder<E, C, R, U2, D, RS> self = self();
//            self.update = qf(queryClass);
//            return self;
//        }
//        
//        public <D2> ViewBuilder<E, C, R, U, D2, RS> d(Class<D2> queryClass) {
//            ViewBuilder<E, C, R, U, D2, RS> self = self();
//            self.delete = qf(queryClass);
//            return self;
//        }
//        
//        public <Q> ViewBuilder<E, Q, Q, Q, Q, RS> crud(Class<Q> queryClass) {
//            return c(queryClass).r(queryClass).u(queryClass).d(queryClass);
//        }
//        
//    }
    
    protected static QueryFactory<?> interfaceFactory(Class<?>[] prevIfaces, Object[][] cachedArgs, Class<?> iface, QueryType<?> type, Object[] args) {
        int index = 0;
        for (; index < prevIfaces.length; index++) {
            if (prevIfaces[index] == iface) {
                return new InterfaceQueryFactory<>(iface, type, cachedArgs[index]);
            }
            if (prevIfaces[index] == null) {
                break;
            }
        }
        args = args.clone();
        boolean success = false;
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            if (a instanceof MappedTemplateProvider) {
                a = new AnnotatedTemplateProvider<>((MappedTemplateProvider) a, iface);
                args[i] = a;
                success = true;
                break;
            }
        }
        if (!success) {
            for (int i = 0; i < args.length; i++) {
            Object a = args[i];
                if (a instanceof Mapping) {
                    a = new AnnotatedTemplateProvider<>((Mapping) a, iface);
                    args[i] = a;
                    success = true;
                    break;
                }
            }
        }
        if (!success) {
            throw new IllegalArgumentException("Mapping required");
        }
        if (index < prevIfaces.length) {
            prevIfaces[index] = iface;
            cachedArgs[index] = args;
        }
        return new InterfaceQueryFactory<>(iface, type, args);
    }
    
    public static interface QueryFactory<Query> {
        Query newQuery(String[] args);
    }
    
    protected static class InterfaceQueryFactory<Query> implements QueryFactory<Query> {
        
        private final ReflectiveQueryFactory<AnnotatedQueryHandler> handlerFactory;
        private final Class[] api;

        public InterfaceQueryFactory(Class<Query> iface, QueryType<?> type, Object... args) {
            handlerFactory = new ReflectiveQueryFactory<>(AnnotatedQueryHandler.class, type, args);
            this.api = new Class[]{iface};
        }

        @Override
        public Query newQuery(String[] args) {
            AnnotatedQueryHandler handler = handlerFactory.newQuery(args);
            Object proxy = Proxy.newProxyInstance(api[0].getClassLoader(), api, handler);
            return (Query) proxy;
        }
    }
    
    protected static class ReflectiveQueryFactory<Query> implements QueryFactory<Query> {
        
        private final QueryType<?> type; 
        private final Constructor<Query> constructor;
        private final Object[] args;
        private final ArgValue[] argmap;

        public ReflectiveQueryFactory(Class<Query> clazz, QueryType<?> type, Object... args) {
            this.type = type;
            this.args = args;
            Constructor<?> c2 = null;
            ArgValue[] argValues = null;
            constructors: for (Constructor<?> c: clazz.getConstructors()) {
                final Class<?>[] params = c.getParameterTypes();
                argValues = new ArgValue[params.length];
                for (int i = 0; i < params.length; i++) {
                    argValues[i] = getArgValue(params[i], i, type, args);
                    if (argValues[i] == null) {
                        continue constructors;
                    }
                }
                c2 = c;
                break;
            }
            if (c2 == null) {
                throw new IllegalArgumentException(clazz.toString());
            }
            this.constructor = (Constructor) c2;
            this.argmap = argValues;
        }
        
        @Override
        public Query newQuery(String[] select) {
            final Object[] cArgs = new Object[argmap.length];
            for (int i = 0; i < cArgs.length; i++) {
                cArgs[i] = argmap[i].get(i, select, type, args);
            }
            try {
                constructor.setAccessible(true);
                return constructor.newInstance(cArgs);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static ArgValue getArgValue(Class<?> param, int i, QueryType<?> type, Object[] args) {
        for (FactoryArg a: ARGS) {
            ArgValue av = a.get(param, i, type, args);
            if (av != null) {
                return av;
            }
        }
        return null;
    }
    
    private interface ArgValue {
        Object get(int i, String[] select, QueryType<?> type, Object[] args);
    }
    
    private static final FactoryArg[] ARGS = FactoryArg.values();
    private static final String[] NO_STRINGS = {};
    
    private static enum FactoryArg implements ArgValue {
        ARG_0 {
            @Override
            public Object get(int i, String[] select, QueryType<?> type, Object[] args) {
                if (args == null || args.length < 1) return null;
                return args[0];
            }
        },
        ARG_1 {
            @Override
            public Object get(int i, String[] select, QueryType<?> type, Object[] args) {
                if (args == null || args.length < 2) return null;
                return args[1];
            }
        },
        ARG_N {
            @Override
            public ArgValue get(Class<?> paramType, int p, QueryType<?> type, Object[] args) {
                if (args == null) return null;
                if (args.length > p && paramType.isInstance(args[p])) {
                    return this;
                }
                int index = -1;
                for (int i = 2; i < args.length; i++) {
                    if (paramType.isInstance(args[i])) {
                        index = i;
                        break;
                    }
                }
                if (index < 0) return null;
                final int n = index;
                return new ArgValue() {
                    @Override
                    public Object get(int i, String[] select, QueryType<?> type, Object[] args) {
                        return args[n];
                    }
                };
            }
            @Override
            public Object get(int i, String[] select, QueryType<?> type, Object[] args) {
                return args[i];
            }
        },
        QUERY_TYPE {
            @Override
            public Object get(int i, String[] select, QueryType<?> type, Object[] args) {
                return type;
            }
        },
        STRINGS {
            @Override
            public Object get(int i, String[] select, QueryType<?> type, Object[] args) {
                return select;
            }
        },
        TEMPLATE {
            @Override
            public ArgValue get(Class<?> paramType, int p, QueryType<?> type, Object[] args) {
                if (!QueryTemplate.class.isAssignableFrom(paramType)) {
                    return null;
                }
                final ArgValue getProvider = getArgValue(QueryTemplateProvider.class, p, type, args);
                if (getProvider == null) {
                    return null;
                }
                ArgValue av = new ArgValue() {
                    @Override
                    public Object get(int i, String[] select, QueryType<?> type, Object[] args) {
                        QueryTemplateProvider qtp = (QueryTemplateProvider) getProvider.get(i, NO_STRINGS, type, args);
                        QueryTemplate qt = qtp.getTemplate(type);
                        return qt;
                    }
                };
                if (paramType.isInstance(av.get(p, NO_STRINGS, type, args))) {
                    return av;
                }
                return null;
            }
            @Override
            public Object get(int i, String[] select, QueryType<?> type, Object[] args) {
                throw new UnsupportedOperationException();
            }
        },
        RESULTS {
            @Override
            public Object get(int i, String[] select, QueryType<?> type, Object[] args) {
                return Results.getBuilder();
            }
        };
        
        public ArgValue get(Class<?> paramType, int p, QueryType<?> type, Object[] args) {
            if (paramType.isInstance(get(p, NO_STRINGS, type, args))) {
                return this;
            }
            return null;
        }
    }
    
    private static class QueryFactoryView<C,R,U,D> extends AbstractViewCRUD<C, R, U, D> {

        private final QueryFactory<C> insert;
        private final QueryFactory<R> select;
        private final QueryFactory<U> update;
        private final QueryFactory<D> delete;

        public QueryFactoryView(QueryFactory<C> insert, QueryFactory<R> select, QueryFactory<U> update, QueryFactory<D> delete) {
            this.insert = insert;
            this.select = select;
            this.update = update;
            this.delete = delete;
        }
        
        @Override
        public C insert(String... attributes) {
            return insert.newQuery(attributes);
        }

        @Override
        public R select(String... attributes) {
            return select.newQuery(attributes);
        }

        @Override
        public U update(String... attributes) {
            return update.newQuery(attributes);
        }

        @Override
        public D delete() {
            return delete.newQuery(NO_STRINGS);
        }
    }
    
    private static class ProxyHandlerView extends QueryFactoryView implements InvocationHandler {
        private final Class<?> view;
        public ProxyHandlerView(Class<?> view, QueryFactory<?> insert, QueryFactory<?> select, QueryFactory<?> update, QueryFactory<?> delete) {
            super(insert, select, update, delete);
            this.view = view;
        }
        
        private String[] getStrings(Object[] args) {
            if (args == null || args.length == 0) {
                return NO_STRINGS;
            }
            if (args.length > 1) {
                throw new IllegalArgumentException(Arrays.toString(args));
            }
            return (String[]) args[0];
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                switch (method.getName()) {
                    case "select":
                        return select(getStrings(args));
                    case "insert":
                        return insert(getStrings(args));
                    case "update":
                        return update(getStrings(args));
                    case "delete":
                        return delete();
                }
                return method.invoke(this, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        @Override
        public String toString() {
            return view.getSimpleName() + "@" + Integer.toHexString(hashCode());
        }
    }
    
//    static Class<?> lookUpType(Type type, Class<?> actualClass, Class<?> declaringClass) {
//        int index = -1;
//        for (Class<?> iface: actualClass.getInterfaces()) {
//            if (declaringClass.isAssignableFrom(iface)) {
//                index = lookUpTypeVariable(type, iface, declaringClass);
//            }
//        }
//        return null;
//    }
//    
//    private static Type lookUpTypeVariable(Type type, Class<?> actualClass, Class<?> declaringClass) {
//        if (actualClass.equals(declaringClass)) {
//            for (TypeVariable<?> tv: actualClass.getTypeParameters()) {
//                //if (tv.getName().equals(type.))
//            }
//            throw new AssertionError(type);
//        }
//        int index = -1;
//         else {
//            for (Class<?> iface: actualClass.getInterfaces()) {
//                if (declaringClass.isAssignableFrom(iface)) {
//                    index = lookUpTypeVariable(type, iface, declaringClass);
//                    break;
//                }
//            }
//            return index;
//        }
//    }
}
