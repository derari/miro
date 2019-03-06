package org.cthul.miro.composer;

import java.util.function.Function;
import org.cthul.miro.composer.node.Copyable;
import org.cthul.miro.composer.node.Initializable;
import org.cthul.miro.composer.node.StatementPart;

/**
 *
 * @param <Builder>
 * @param <Adapted>
 * @param <Composer>
 */
public abstract class AbstractComposer<Builder,Adapted,Composer> 
            implements RequestComposer<Builder>, StatementPart<Builder>, Extensible,
                        Initializable<Composer>, Copyable<Composer> {
    
    private static final KeyIndex INDEX = new KeyIndex();
    
    protected static KeyIndex index() {
        return INDEX.extend();
    }
    
    private final AbstractComposer<?,?,?> parent;
    private final Object factory;
    private final Function<? super Builder, ? extends Adapted> builderAdapter;
    private final int count;
    private boolean extensible;
    private Object[] nodes = null;
    private Composer rootComposer = (Composer) this;

    public AbstractComposer(int count, Object factory, Function<? super Builder, ? extends Adapted> builderAdapter) {
        this.parent = null;
        this.count = count;
        this.factory = factory;
        this.builderAdapter = builderAdapter;
        this.extensible = false;
    }

    public AbstractComposer(KeyIndex indexBuilder, Object factory, Function<? super Builder, ? extends Adapted> builderAdapter) {
        this(indexBuilder.count, factory, builderAdapter);
    }
    
//    protected AbstractComposer(AbstractComposer<? super Builder, ? extends Adapted> src) {
//        this.parent = src;
//        this.nodes = new Object[src.nodes.length];
//        this.factory = src.factory;
//        this.builderAdapter = src.builderAdapter;
//    }
//    
    protected AbstractComposer(AbstractComposer<?, Adapted, ?> src, Function<? super Builder, ? extends Adapted> builderAdapter) {
        this.parent = src.nodes != null ? src : src.parent;
        this.count = src.count;
        this.factory = src.factory;
        this.extensible = src.extensible;
        this.builderAdapter = builderAdapter;
    }
    
    private Object[] nodes() {
        if (nodes == null) {
            nodes = new Object[count];
        }
        return nodes;
    }
    
    protected void beforeBuild() {
    }
    
    protected <T> T extend(T node) {
        return (T) ((Extensible) node).extend();
    }
    
    protected <T> T adapt(T node, Function<? super Adapted, ?> adapter) {
        return (T) ((RequestComposer) node).adapt(adapter);
    }
    
    protected <T> T getNode(NodeKey key) {
        int i = key.getIndex();
        Object node = nodes()[i];
        if (node == null) {
            node = newNode(key);
        }
        if (node == NULL) return null;
        return (T) node;
    }
    
    protected void putNode(NodeKey key, Object value) {
        putNode(key.index, value);
    }
    
    private void putNode(int index, Object value) {
        nodes()[index] = value != null ? value : NULL;
        if (value instanceof Initializable) {
            ((Initializable) value).initialize(rootComposer);
        }
    }
    
    protected void putAll(Object... keyValues) {
        for (int i = 0; i < keyValues.length; i += 2) {
            int index = ((NodeKey) keyValues[i]).index;
            nodes()[index] = keyValues[i+1];
        }
        for (int i = 0; i < keyValues.length; i += 2) {
            Object value = keyValues[i+1];
            if (value instanceof Initializable) {
                ((Initializable) value).initialize(rootComposer);
            }
        }
    }
    
    private Object newNode(NodeKey key) {
        Object value = null;
        if (parent != null) {
            value = inherit(key.index);
        }
        if (value == null) {
            value = key.create(factory, rootComposer);
            putNode(key, value != null ? value : NULL);
        }
        return value;
    }
    
    private Object inherit(int index) {
        AbstractComposer ancestor = parent;
        Object node = null;
        while (ancestor != null) {
            if (ancestor.nodes == null) continue;
            node = ancestor.nodes[index];
            if (node != null) break;
            ancestor = ancestor.parent;
        }
        if (node == NULL || !(node instanceof Copyable)) return null;
        Copyable<Object> copyable = (Copyable<Object>) node;
        return nodes()[index] = copyable.copy(rootComposer);
    }

    protected Composer getRootComposer() {
        return rootComposer;
    }

    @Override
    public Extensible extend() {
        AbstractComposer<?,?,?> copy = _copy();
        copy.extensible = true;
        return copy;
    }

    @Override
    public void initialize(Composer composer) {
        if (extensible) {
            rootComposer = composer;
        }
    }
    
    protected abstract Object copy(Function<?, ? extends Adapted> builderAdapter);
    
    protected <T> T _copy() {
        return (T) copy(builderAdapter);
    }

    @Override
    public RequestComposer<Builder> copy() {
        return _copy();
    }

    @Override
    public Object copy(Composer composer) {
        Initializable<Composer> copy = _copy();
        copy.initialize(composer);
        return copy;
    }

    @Override
    public <Builder2> RequestComposer<Builder2> adapt(Function<? super Builder2, ? extends Builder> builderAdapter) {
        if (builderAdapter == Function.identity()) {
            return (RequestComposer) copy((Function) builderAdapter);
        }
        Function<? super Builder2, Adapted> adapter = builderAdapter.andThen(this.builderAdapter);
        return (RequestComposer<Builder2>) copy(adapter);
    }

    @Override
    public void addTo(Builder builder) {
        build(builder);
    }

    @Override
    public void build(Builder builder) {
        beforeBuild();
        Adapted actual = builderAdapter.apply(builder);
        nodes();
        for (int i = 0; i < nodes.length; i++) {
            Object n = nodes[i];
            if (n == null) {
                n = inherit(i);
            }
            if (n instanceof StatementPart) {
                ((StatementPart<Adapted>) n).addTo(actual);
            }
        }
    }
    
    private static final Object NULL = new Object();
    
    protected static class NodeKey {
        private final int index;
        private final boolean impl;
        private final Function<Object, ?> factoryFunction;

        protected NodeKey(int index) {
            this.index = index;
            this.factoryFunction = o -> null;
            this.impl = true;
        }

        protected NodeKey(int index, boolean impl, Function<Object, ?> factoryFunction) {
            this.index = index;
            this.impl = impl;
            this.factoryFunction = factoryFunction;
        }

        public int getIndex() {
            return index;
        }

        protected Object create(Object factory, Object rootComposer) {
            return factoryFunction.apply(impl ? rootComposer : factory);
        }
    }
    
    protected static class KeyIndex {
        private int count = 0;

        private KeyIndex() {
        }

        private KeyIndex(KeyIndex src) {
            this.count = src.count;
        }
        
        public NodeKey key() {
            return new NodeKey(count++);
        }
        
        public <T> NodeKey factory(Function<T,?> factory) {
            return new NodeKey(count++, false, (Function) factory);
        }
        
        public <T> NodeKey impl(Function<T,?> factory) {
            return new NodeKey(count++, true, (Function) factory);
        }
        
        public KeyIndex extend() {
            return new KeyIndex(this);
        }
    }
    
    protected static abstract class AbstractRequest<Builder, Composer> 
            implements RequestComposer.Delegator<Builder>, StatementPart<Builder>,
                       Copyable<Composer>, Initializable<Composer> {

        private final RequestComposer<? super Builder> composer;

        public AbstractRequest(RequestComposer<? super Builder> composer) {
            this.composer = composer;
        }
        
        @Override
        public RequestComposer<? super Builder> getRequestComposerDelegate() {
            return composer;
        }

        @Override
        public Object copy(Composer composer) {
            Object copy = copy();
            ((Initializable) copy).initialize(composer);
            return copy;
        }

        @Override
        public void initialize(Composer composer) {
            if (this.composer instanceof Initializable) {
                ((Initializable) this.composer).initialize(composer);
            }
        }
        
        @Override
        public void addTo(Builder builder) {
            build(builder);
        }
    }
}
