package org.cthul.miro.at;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.View;
import org.cthul.miro.graph.*;
import org.cthul.miro.map.Mapping;

/**
 *
 */
public class AnnotatedQueryHandler<Entity> extends GraphQuery<Entity> implements InvocationHandler {
    
    private final AnnotatedQueryTemplate<Entity> template;
    private final Map<Method, InvocationHandler> handlers;

    @SuppressWarnings("LeakingThisInConstructor")
    public AnnotatedQueryHandler(MiConnection cnn, Mapping<Entity> mapping, AnnotatedQueryTemplate<Entity> template, View<? extends SelectByKey<?>> view) {
        super(cnn, mapping, template, view);
        this.template = template;
        this.handlers = template.getHandlers(this);
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public AnnotatedQueryHandler(MiConnection cnn, AnnotatedQueryTemplate<Entity> template, View<? extends SelectByKey<?>> view) {
        super(cnn, template, view);
        this.template = template;
        this.handlers = template.getHandlers(this);
    }
    
    public AnnotatedQueryHandler(MiConnection cnn, Mapping<Entity> mapping, AnnotatedQueryTemplate<Entity> template, View<? extends SelectByKey<?>> view, String... fields) {
        this(cnn, mapping, template, view);
        select(fields);
    }
    
    public AnnotatedQueryHandler(MiConnection cnn, AnnotatedQueryTemplate<Entity> template, View<? extends SelectByKey<?>> view, String... fields) {
        this(cnn, template, view);
        select(fields);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InvocationHandler handler = handlers.get(method);
        if (handler != null) {
            return handler.invoke(proxy, method, args);
        }
        String mName = method.getName();
        Class[] paramTypes = method.getParameterTypes();
        if (handler == null) {
            try {
                Method myMethod = getClass().getMethod(mName, paramTypes);
                handler = new CallOther(this, myMethod);
            } catch (NoSuchMethodException e) { }
        }
        if (handler == null) {
            try {
                Method myMethod = query().getClass().getMethod(mName, paramTypes);
                handler = new CallOther(query(), myMethod);
            } catch (NoSuchMethodException e) { }
        }
        if (handler == null) {
            if (paramTypes.length == 0 && method.getReturnType().isInstance(proxy)) {
                handler = new NoOp();
            }
        }
        if (handler == null) {
            throw new RuntimeException("Unexpected method: " + method);    
        }
        handlers.put(method, handler);
        return handler.invoke(proxy, method, args);
    }
    
    private static class NoOp implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return proxy;
        }
    }
    
    private static class CallOther implements InvocationHandler {
        private final Object other;
        private final Method myMethod;

        public CallOther(Object other, Method myMethod) {
            this.other = other;
            this.myMethod = myMethod;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = myMethod.invoke(other, args);
            if (result == other) {
                return proxy;
            } else {
                return result;
            }
        }
    }
}
