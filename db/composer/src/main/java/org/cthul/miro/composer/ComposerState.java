package org.cthul.miro.composer;

import org.cthul.miro.composer.node.StatementPart;
import org.cthul.miro.composer.node.Copyable;
import org.cthul.miro.composer.node.Initializable;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import static java.util.Arrays.asList;

/**
 *
 */
public class ComposerState
        implements InvocationHandler, RequestComposer<Object>, NoOverride /*ComposerInternal<Object>, NewComposer<Object, Object>*/ {
    
    public static <B> RequestComposer<B> asRequestComposer(Object composer) {
        return (RequestComposer<B>) composer;
    }
    
    public static <T> T copy(T composer) {
        return (T) asRequestComposer(composer).copy();
    }
    
    public static <T> T adapt(Object composer, Function<?,?> builderAdapter) {
        return (T) asRequestComposer(composer).adapt((Function) builderAdapter);
    }
    
    public static void put(Object composer, String key, Object value) {
        ComposerState state = (ComposerState) Proxy.getInvocationHandler(composer);
        state.nodes.putNode(key, value);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static <T> T fromFactory(Object factory) {
        return builder().setFactory(factory).create();
    }
    
    private final Behavior<Object> impl;
    private final Object factory;
    private final Class[] interfaces;
    private final ConcurrentMap<String, MethodHandler> handlers;
    private final ComposerNodeSet nodes;

    private boolean initialized;
    private boolean noOverride;
    private final Function<Object,Object> builderAdapter;
    private final Object proxy;
    private Object root;
    
    protected ComposerState(Behavior<?> impl, Object factory, Class[] interfaces) {
        this.nodes = new ComposerNodeSet();
        this.impl = (Behavior) impl;
        this.factory = factory;
        this.interfaces = interfaces;
        this.handlers = new ConcurrentHashMap<>(DEFAULT_HANDLERS);
        this.initialized = false;
        this.noOverride = false;
        this.builderAdapter = Function.identity();
        this.proxy = newComposerProxy();
    }
    
    protected ComposerState(ComposerState parent) {
        this(parent, parent.builderAdapter);
    }
    
    public ComposerState(ComposerState parent, Function<?,?> builderAdapter) {
        this.nodes = new ComposerNodeSet(parent.nodes);
        this.impl = (Behavior) parent.impl.copy();
        this.factory = parent.factory;
        this.interfaces = parent.interfaces;
        this.handlers = parent.handlers;
        this.initialized = parent.initialized;
        this.noOverride = parent.noOverride;
        this.builderAdapter = (Function) builderAdapter;
        this.proxy = newComposerProxy();
    }
    
    private <T> T newComposerProxy() {
        Object newProxy = Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, this);
        this.initialize(newProxy);
        impl.initialize(newProxy);
        return (T) newProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null || args.length == 0) {
            String key = method.getName();
            Object value = nodes.peekValue(key);
            if (value != null) return value;
            return handlers
                    .computeIfAbsent(key, k -> defaultHandler(method))
                    .call(this);
        } else {
            switch (method.getName()) {
                case "adapt":
                    return adapt((Function) args[0]);
                case "addTo":
                case "build":
                    build(args[0]); 
                    return null;
                case "copy":
                    Object copy = copy();
                    ((Initializable) copy).initialize(args[0]);
                    return copy;
                case "equals":
                    return proxy == args[0];
                case "initialize":
                    initialize(args[0]);
                    return null;
            }
        }
        throw new UnsupportedOperationException(method.toString());
    }
    
    private MethodHandler defaultHandler(Method method) {
        MethodHandler def = null;
        if (impl != null) {
            try {
                Method implMethod = impl.getClass().getMethod(method.getName(), method.getParameterTypes());
                def = implHandler(implMethod);
            } catch (NoSuchMethodException e) {
                // continue
            }
        }
        if (def == null) {
            def = factoryHandler(method);
        }
        return nodeHandler(method, def);
    }
    
    private static MethodHandler implHandler(Method method) {
        return new MethodHandler() {
            boolean recursionGuard = false;
            boolean recursion = false;
            @Override
            public Object call(ComposerState state) throws Throwable {
                if (recursionGuard) {
                    recursion = true;
                    MethodHandler factoryHandler = nodeHandler(method, state.factoryHandler(method));
                    state.handlers.put(method.getName(), factoryHandler);
                    return factoryHandler.call(state);
                }
                try {
                    recursionGuard = true;
                    Object value = method.invoke(state.impl);
                    state.nodes.putValue(method.getName(), value);
                    if (!recursion) {
                        MethodHandler implHandler = nodeHandler(method, implHandlerNoCheck(method));
                        state.handlers.put(method.getName(), implHandler);
                    }
                    return value;
                } finally {
                    recursionGuard = false;
                }
            }
        };
    }
    
    private static MethodHandler implHandlerNoCheck(Method method) {
        return state -> {
            Object value = method.invoke(state.impl);
            state.nodes.putValue(method.getName(), value);
            return value;
        };
    }
    
    private MethodHandler factoryHandler(Method method) {
        Method factoryMethod;
        try {
            factoryMethod = factory.getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            if (method.getReturnType().isInstance(proxy)) {
                return state -> state.proxy;
            }
            throw new RuntimeException(e);
        }
        return state -> {
            try {
                String key = factoryMethod.getName();
                Object value = factoryMethod.invoke(state.factory);
                state.nodes.putNode(key, value);
                return value;
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };      
    }
    
    private static MethodHandler nodeHandler(Method method, MethodHandler def) {
        return state -> state.nodes.getValue(method.getName(), def);
    }
    
    private void initialize(Object composer) {
        if (noOverride && root != null) {
            return;
        }
        for (Class<?> i: interfaces) {
            if (i != ComposerInternal.class && !i.isInstance(composer)) {
                throw new IllegalArgumentException(
                        "Root does not implement " + i);
            }
        }
        root = composer;
//        impl.initialize(composer);
    }
    
    private void makeInitialized() {
        if (!initialized) {
            initialized = true;
            if (proxy instanceof ComposerInternal) {
                ((ComposerInternal) proxy).getAlways();
            }
        }
    }

//    @Override
//    protected Object getInitializationArg() {
//        return root;
//    }
//
//    @Override
//    protected void newEntry(String key, MethodHandler hint) {
//        makeInitialized();
//        try {
//            hint.call(this);
//        } catch (Throwable t) {
//            throw new RuntimeException(t);
//        }
//    }
    
    @Override
    public void build(Object builder) {
        makeInitialized();
        Object adapted = ((Function) builderAdapter).apply(builder);
        nodes.addPartsTo(adapted);
    }

    @Override
    public RequestComposer<Object> copy() {
        makeInitialized();
        return (RequestComposer) new ComposerState(this).proxy;
    }

    @Override
    public <Builder2> RequestComposer<Builder2> adapt(Function<? super Builder2, ? extends Object> builderAdapter) {
        makeInitialized();
        return (RequestComposer) new ComposerState(this, builderAdapter).proxy;
    }

    @Override
    public NoOverride noOverride() {
        makeInitialized();
        ComposerState state = new ComposerState(this);
        state.noOverride = true;
        return (NoOverride) state.proxy;
    }

    @Override
    public String toString() {
        List<Class> all = Arrays.asList(interfaces);
        List<String> lowest = new ArrayList<>();
        for (Class<?> c: all) {
            if (c.getPackage().getName().startsWith("org.cthul.miro.request")) continue;
            if (all.stream().anyMatch(c2 -> c != c2 && c.isAssignableFrom(c2))) continue;
            String name = c.getName();
            if (name.endsWith("$Delegator")) name = name.substring(0, name.length() - "$Delegator".length());
            int i = name.lastIndexOf('.');
            if (i > 0) name = name.substring(i+1);
            lowest.add(name);
        }
//        all.stream()
//                .filter(c -> !all.stream().anyMatch(c2 -> c != c2 && c2.isAssignableFrom(c)))
//                .filter(c -> !c.getPackage().getName().startsWith("org.cthul.miro.request"))
//                .forEach(c -> lowest.add(c.getName()));
        return lowest.toString();
    }
    
    private static final Map<String, MethodHandler> DEFAULT_HANDLERS = new HashMap<>();
    private static final List<Class<?>> DEFAULT_INTERFACES = asList(Initializable.class, Copyable.class, RequestComposer.class, StatementPart.class, NoOverride.class);
    
    static {
        DEFAULT_HANDLERS.put("copy", h -> h.copy());
        DEFAULT_HANDLERS.put("noOverride", h -> h.noOverride());
        DEFAULT_HANDLERS.put("toString", h -> h.toString());
        DEFAULT_HANDLERS.put("allowReadOriginal", h -> false);
    }
    
    public static interface MethodHandler {
        Object call(ComposerState handler) throws Throwable;
    }
    
    public static interface Behavior<Composer> extends Initializable<Composer> {
        
        Object copy();
    }
    
    private class ComposerNodeSet extends CopyableNodeSet<String, ComposerState.MethodHandler, Object> {

        public ComposerNodeSet() {
        }

        public ComposerNodeSet(CopyableNodeSet<String, MethodHandler, Object> parent) {
            super(parent);
        }

        @Override
        protected void newEntry(String key, MethodHandler hint) {
            makeInitialized();
            try {
                hint.call(ComposerState.this);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @Override
        protected Object getInitializationArg() {
            return root;
        }
    }
    
    private static final Behavior<Object> NO_IMPL = new Behavior<Object>() {
        @Override
        public Object copy() { return this; }
        @Override
        public void initialize(Object composer) { }
        @Override
        public String toString() {
            return "NO_IMPL";
        }
    };
    
    public static class Builder {
        private Behavior<?> impl = NO_IMPL;
        private Object factory = NO_IMPL;
        private final Set<Class<?>> interfaces = new LinkedHashSet<>(DEFAULT_INTERFACES);
        private final Map<String, Object> nodes = new HashMap<>();

        public Builder setImpl(Behavior<?> impl) {
            this.impl = impl;
            return this;
        }

        public Builder setFactory(Object factory) {
            this.factory = factory;
            return this;
        }
        
        private void addExceptRequestComposer(Class<?> iface) {
            if (RequestComposer.class.isAssignableFrom(iface)) {
                addExceptRequestComposer(iface.getInterfaces());
            } else {
                interfaces.add(iface);
            }
        }
        
        private void addExceptRequestComposer(Class<?>[] interfaces) {
            Arrays.stream(interfaces).forEach(this::addExceptRequestComposer);
        }
        
        public Builder addComposerInterface(Object iface) {
            Class<?> c = iface instanceof Class ? (Class) iface : iface.getClass();
            if (c.isInterface()) {
                addExceptRequestComposer(c);
            } else {
                addExceptRequestComposer(c.getInterfaces());
            }
            return this;
        }
        
        public Builder addComposerInterfaces(Object... more) {
            if (more == null) return this;
            for (Object o: more) {
                addComposerInterface(o);
            }
            return this;
        }
        
        public Builder addRequestInterface(Object iface) {
            Class<?> c = iface instanceof Class ? (Class) iface : iface.getClass();
            if (c.isInterface()) {
                interfaces.add(c);
            } else {
                interfaces.addAll(asList(c.getInterfaces()));
            }
            return this;
        }
        
        public Builder addRequestInterfaces(Object... more) {
            if (more == null) return this;
            for (Object o: more) {
                addRequestInterface(o);
            }
            return this;
        }
        
        public Builder put(String key, Object node) {
            nodes.put(key, node);
            return this;
        }
        
        public Builder putNoOverride(String key, Object node) {
            return put(key, NoOverride.noOverride(node));
        }
        
        public Builder putAdapted(String key, Object node, Function<?,?> adapter) {
            return put(key, adapt(node, adapter));
        }
        
        public Builder putAdaptedNoOverride(String key, Object node, Function<?,?> adapter) {
            return putAdapted(key, NoOverride.noOverride(node), adapter);
        }
        
        public <T> T create(Class<T> requestInterface) {
            addRequestInterface(requestInterface);
            return create();
        }
        
        public <T> T create() {
            Set<Class<?>> ifaces = new LinkedHashSet<>(interfaces);
            ifaces.remove(ComposerInternal.class);
            ifaces.addAll(asList(impl.getClass().getInterfaces()));
            ifaces.addAll(asList(factory.getClass().getInterfaces()));
            ifaces.remove(Behavior.class);
            ComposerState state = new ComposerState(impl, factory, ifaceArray(ifaces));
            nodes.entrySet().forEach(e -> state.nodes.putNode(e.getKey(), e.getValue()));
            return (T) state.proxy;
        }
        
        private Class<?>[] ifaceArray(Collection<Class<?>> interfaces) {
            return interfaces.stream()
                    .filter(c -> !interfaces.stream().anyMatch(c2 -> c != c2 && c.isAssignableFrom(c2)))
                    .collect(Collectors.toList()).toArray(new Class[0]);
        }
    }
}
