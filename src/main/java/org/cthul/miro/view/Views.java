package org.cthul.miro.view;

import java.lang.reflect.*;
import java.util.Arrays;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.sql.DataQuery;

public class Views {
    
    public static <V extends View> V newView(Class<V> clazz, Object... args) {
        try {
            QueryFactory<?> select = null;
            QueryFactory<?> insert = null;
            QueryFactory<?> update = null;
            QueryFactory<?> delete = null;
            if (ViewC.class.isAssignableFrom(clazz)) {
                Class<?> q = clazz.getMethod("insert", String[].class).getReturnType();
                insert = new ReflectiveQueryFactory<>(q, DataQuery.INSERT, args);
            }
            if (ViewR.class.isAssignableFrom(clazz)) {
                Class<?> q = clazz.getMethod("select", String[].class).getReturnType();
                select = new ReflectiveQueryFactory<>(q, DataQuery.SELECT, args);
            }
            if (ViewU.class.isAssignableFrom(clazz)) {
                Class<?> q = clazz.getMethod("update", String[].class).getReturnType();
                update = new ReflectiveQueryFactory<>(q, DataQuery.UPDATE, args);
            }
            if (ViewD.class.isAssignableFrom(clazz)) {
                Class<?> q = clazz.getMethod("delete").getReturnType();
                delete = new ReflectiveQueryFactory<>(q, DataQuery.DELETE, args);
            }
            Class[] ifaces = {clazz};
            InvocationHandler handler = new ProxyHandlerView(clazz, insert, select, update, delete);
            return (V) Proxy.newProxyInstance(clazz.getClassLoader(), ifaces, handler);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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
    
    public static interface QueryFactory<Query> {
        Query newQuery(String[] args);
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
                    for (FactoryArg a: ARGS) {
                        ArgValue av = a.get(params[i], i, type, args);
                        if (av != null) {
                            argValues[i] = av;
                            break;
                        }
                    }
                    if (args[i] == null) {
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
            for (int i = 0; i < args.length; i++) {
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
}
