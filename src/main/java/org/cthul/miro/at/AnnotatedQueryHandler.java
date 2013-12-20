package org.cthul.miro.at;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.cthul.miro.MiConnection;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;
import org.cthul.miro.at.AnnotatedTemplateProvider.InterfaceCall;
import org.cthul.miro.dml.*;
import org.cthul.miro.graph.EntityGraphAdapter;
import org.cthul.miro.map.*;
import org.cthul.miro.query.InternalQueryBuilder;
import org.cthul.miro.query.QueryType;
import org.cthul.miro.query.adapter.DBAdapter;
import org.cthul.miro.query.adapter.QueryString;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.result.EntityType;
import org.cthul.miro.result.FutureResults;
import org.cthul.miro.result.QueryWithResult;
import org.cthul.miro.result.ResultBuilder;
import org.cthul.miro.result.ResultBuilders;
import org.cthul.miro.result.Results;

/**
 *
 */
public class AnnotatedQueryHandler<Entity, Result> 
                extends AbstractMappedQuery<Entity> 
                implements InvocationHandler, QueryWithResult<Result> {
    
    private final ResultBuilder<Result, Entity> resultBuilder;
    private final ConcurrentMap<Method, InterfaceCall> handlers;

    public AnnotatedQueryHandler(QueryType<?> type, AnnotatedTemplateProvider<Entity> templateProvider, ResultBuilder<Result, Entity> resultBuilder, Object... fields) {
        super(type, templateProvider);
        this.handlers = templateProvider.getHandlers();
        put(DataQueryKey.PUT_STRINGS, fields);
        if (type == DataQuery.INSERT) {
            put(DataQueryKey.ADD_GENERATED_KEYS_TO_RESULT);
            resultBuilder = (ResultBuilder) ResultBuilders.getListResult();
        }
        this.resultBuilder = resultBuilder;
    }

    public AnnotatedQueryHandler<Entity, Result> into(Entity... entities) {
        return into(Arrays.asList(entities));
    }
    
    public AnnotatedQueryHandler<Entity, Result> into(List<Entity> entities) {
        if (getQueryType() != DataQuery.SELECT) {
            throw new IllegalStateException("SELECT only");
        }
        EntityGraphAdapter<Entity> ga = mappedProvider.getGraphAdapter();
        Object[] empty = {};
        put(DataQueryKey.ALL_KEY_DEPENDENCIES);
        addToGraph(entities);
        for (Entity e: entities) {
            Object[] key = ga.getKey(e, empty);
            put2(DataQueryKey.KEYS_IN, DataQuerySubkey.ADD, key);
        }
        return this;
    }
    
    public AnnotatedQueryHandler<Entity, Result> values(Entity... args) {
        addToGraph(args);
        put2(MappedDataQueryKey.ENTITIES, DataQuerySubkey.ADD_ALL, args);
        return this;
    }
    
    public AnnotatedQueryHandler<Entity, Result> where(String key, Object... args) {
        put(key, args);
        return this;
    }
    
    @Override
    public Result execute(MiConnection cnn) throws SQLException {
        ResultSet rs = executeJdbc(cnn);
        if (rs != null) {
            return resultBuilder.build(rs, getEntityType(), getConfiguration(cnn));
        }
        return null;
    }

    @Override
    public Result _execute(MiConnection cnn) {
        try {
            return execute(cnn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public MiFuture<Result> submit(final MiConnection cnn) {
        MiFuture<Result> f = cnn.submit(null, new MiFutureAction<Void, Result>() {
            @Override
            public Result call(Void arg) throws Exception {
                return execute(cnn);
            }
        });
        if (resultBuilder instanceof Results) {
            f = (MiFuture) new FutureResults<>((MiFuture) f);
        }
        return f;
    }
    
    public QueryString<?> toQueryString(MiConnection cnn) {
        return toQueryString(cnn.getJdbcAdapter());
    }
    
    public QueryString<?> toQueryString(DBAdapter adapter) {
        return getAdapter(adapter);
    }

    @Override
    protected EntityType<Entity> typeForEntities(List<Entity> entities) {
        if (getQueryType() != DataQuery.SELECT) {
            return entitiesInOrder(entities);
        }
        return graphTypeForEntities(entities);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InterfaceCall handler = getHandler(proxy, method, args);
        Object result = handler.call(this, internal(), args);
        if (result == this) {
            return proxy;
        } else {
            return result;
        }
    }
    
    protected InterfaceCall getHandler(Object proxy, Method method, Object[] args) {
        InterfaceCall handler = handlers.get(method);
        if (handler != null) {
            return handler;
        }
        String mName = method.getName();
        Class[] paramTypes = method.getParameterTypes();
        try {
            Method myMethod = getClass().getMethod(mName, paramTypes);
            handler = new CallOther(myMethod);
            handlers.putIfAbsent(method, handler);
            return handler;
        } catch (NoSuchMethodException e) { }
        if (paramTypes.length == 0 && method.getReturnType().isInstance(proxy)) {
            handler = NO_OP;
            handlers.putIfAbsent(method, handler);
            return handler;
        }
        throw new RuntimeException("Unexpected method: " + method);    
    }
    
    protected static final InterfaceCall NO_OP = new InterfaceCall() {
        @Override
        public Object call(AnnotatedQueryHandler<?, ?> handler, InternalQueryBuilder builder, Object[] args) throws Throwable {
            return handler;
        }
    };
    
    private static class CallOther implements InterfaceCall {
        private final Method myMethod;

        public CallOther(Method myMethod) {
            this.myMethod = myMethod;
        }

        @Override
        public Object call(AnnotatedQueryHandler<?, ?> handler, InternalQueryBuilder builder, Object[] args) throws Throwable {
            return myMethod.invoke(handler, args);
        }
    }
}
