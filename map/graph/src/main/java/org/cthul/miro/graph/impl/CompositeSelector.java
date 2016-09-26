package org.cthul.miro.graph.impl;

import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.entity.FactoryBuilder;
import org.cthul.miro.entity.InitializationBuilder;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.graph.NodeSet;
import org.cthul.miro.graph.SelectorBuilder;
import org.cthul.miro.util.Closeables;
import org.cthul.miro.util.Completable;
import org.cthul.miro.util.CompletableBuilder;
import org.cthul.miro.util.CompleteAndClose;
import org.cthul.miro.util.XConsumer;
import org.cthul.miro.util.XFunction;
import org.cthul.miro.util.XSupplier;

/**
 *
 * @param <Node>
 */
public class CompositeSelector<Node> implements NodeSelector<Node>, EntityFactory<Node> {
    
    private final XFunction<Object[], ? extends Node, ?> supplier;
    private final Object factoryName;
    private final EntityInitializer<? super Node> setup;

    public CompositeSelector(XFunction<Object[], ? extends Node, ?> supplier, Object factoryName, EntityInitializer<? super Node> setup) {
        this.supplier = supplier;
        this.factoryName = factoryName;
        this.setup = setup;
    }

    @Override
    public Node get(Object... key) throws MiException {
        Node n;
        try {
            n = supplier.apply(key);
        } catch (Throwable t) {
            throw Closeables.exceptionAs(t, MiException.class);
        }
        setup.apply(n);
        return n;
    }

    @Override
    public Node newEntity() throws MiException {
        Object[] key = null;
        return get(key);
    }

    @Override
    public void complete() throws MiException {
        setup.complete();
    }

    @Override
    public void close() throws MiException {
        setup.close();
    }

    @Override
    public String toString() {
        if (setup == EntityTypes.noInitialization()) {
            return String.valueOf(factoryName);
        }
        return String.valueOf(factoryName) + " with " + setup.toString();
    }
    
    public static <Node, T extends Throwable> CompositeSelector<Node> buildSelector(XConsumer<? super SelectorBuilder<Node>, T> action) throws T {
        SelBuilder<Node> b = new SelBuilder<>();
        action.accept(b);
        return b.buildSelector();
    }
    
    public static <Node, T extends Throwable> CompositeSelector<Node> buildNestedSelector(CompletableBuilder parent, XConsumer<? super SelectorBuilder<Node>, T> action) throws T {
        NestedSelBuilder<Node> b = new NestedSelBuilder<>(parent);
        action.accept(b);
        return b.buildSelector();
    }
    
    public static <Node, T extends Throwable> CompositeSelector<Node> buildNestedFrom(CompletableBuilder parent, NodeSet<Node> nodeSet) throws MiException {
        return buildNestedSelector(parent, b -> nodeSet.newNodeSelector(b));
    }
    
    public static <Node, T extends Throwable> void buildAsFactory(FactoryBuilder<Node> fBuilder, XConsumer<? super SelectorBuilder<Node>, T> action) throws T {
        if (fBuilder instanceof SelectorBuilder) {
            action.accept((SelectorBuilder<Node>) fBuilder);
        } else {
            action.accept(new SelectorAsFactory<>(fBuilder));
        }
    }
    
    protected static class NestedSelBuilder<Node> 
                    extends EntityTypes.NestedInitBuilder<Node> 
                    implements SelectorBuilder<Node> {

        private XFunction<Object[], ? extends Node, ?> factory;
        private Object factoryName = null;

        public NestedSelBuilder(CompletableBuilder completableBuilder) {
            super(completableBuilder);
        }

        @Override
        public <E extends Node> SelectorBuilder<E> set(NodeSelector<E> factory) {
            if (factory instanceof CompositeSelector) {
                CompositeSelector<E> cf = (CompositeSelector<E>) factory;
                setFactory(cf.supplier).add(cf.setup);
            } else {
                setFactory((XFunction<Object[], Node, ?>) factory::get);
                addCompletable(factory);
                addCloseable(factory);
            }
            return (SelectorBuilder) this;
        }

        @Override
        public <E extends Node> FactoryBuilder<E> set(EntityFactory<E> factory) {
            setFactory(factory::newEntity);
            addCompleteAndClose(factory);
            return (FactoryBuilder<E>) this;
        }

        @Override
        public <N extends Node> SelectorBuilder<N> setFactory(XFunction<Object[], N, ?> factory) {
            this.factory = factory;
            return (SelectorBuilder) this;
        }

        @Override
        public <E extends Node> FactoryBuilder<E> setFactory(XSupplier<E, ?> factory) {
            setFactory(k -> factory.get());
            return (FactoryBuilder<E>) this;
        }

        @Override
        public InitializationBuilder<Node> addName(Object name) {
            if (factoryName == null) {
                factoryName = name;
                return this;
            } else {
                return super.addName(name);
            }
        }
        
        public CompositeSelector<Node> buildSelector() {
            if (factory == null) throw new NullPointerException("factory");
            return new CompositeSelector<>(factory, factoryName, buildInitializer());
        }

        @Override
        public String toString() {
            return (factoryName != null ? factoryName : "?") + 
                    " with " + super.toString();
        }
    }
    
    protected static class SelBuilder<Node> extends NestedSelBuilder<Node> {
        
        private final CompleteAndClose.Builder<?> ccBuilder;

        public SelBuilder() {
            this(new CompleteAndClose.Builder<>());
        }

        private SelBuilder(CompleteAndClose.Builder<?> ccBuilder) {
            super(ccBuilder);
            this.ccBuilder = ccBuilder;
        }

        @Override
        protected void addNestedName(Object name) {
            ccBuilder.addName(name);
        }

        @Override
        public CompleteAndClose buildCompleteAndClose() {
            return ccBuilder.buildCompleteAndClose();
        }
    }
    
    protected static class SelectorAsFactory<Node> implements SelectorBuilder<Node> {
        
        private final FactoryBuilder<Node> builder;

        public SelectorAsFactory(FactoryBuilder<Node> builder) {
            this.builder = builder;
        }

        @Override
        public <N extends Node> SelectorBuilder<N> set(NodeSelector<N> factory) {
            Object[] key = null;
            return (SelectorBuilder<N>) setFactory(() -> factory.get(key))
                    .addCompleteAndClose(factory);
        }

        @Override
        public <N extends Node> SelectorBuilder<N> setFactory(XFunction<Object[], N, ?> factory) {
            Object[] key = null;
            builder.setFactory(() -> factory.apply(key));
            return (SelectorBuilder<N>) this;
        }

        @Override
        public <E extends Node> FactoryBuilder<E> set(EntityFactory<E> factory) {
            builder.set(factory);
            return (FactoryBuilder<E>) this;
        }

        @Override
        public <E extends Node> FactoryBuilder<E> setFactory(XSupplier<E, ?> factory) {
            builder.setFactory(factory);
            return (FactoryBuilder<E>) this;
        }

        @Override
        public InitializationBuilder<Node> add(EntityInitializer<? super Node> initializer) {
            builder.add(initializer);
            return this;
        }

        @Override
        public InitializationBuilder<Node> addInitializer(XConsumer<? super Node, ?> initializer) {
            builder.addInitializer(initializer);
            return this;
        }

        @Override
        public InitializationBuilder<Node> addCompletable(Completable completable) {
            builder.addCompletable(completable);
            return this;
        }

        @Override
        public InitializationBuilder<Node> addCloseable(AutoCloseable closeable) {
            builder.addCloseable(closeable);
            return this;
        }

        @Override
        public InitializationBuilder<Node> addName(Object name) {
            builder.addName(name);
            return this;
        }
    }
}
