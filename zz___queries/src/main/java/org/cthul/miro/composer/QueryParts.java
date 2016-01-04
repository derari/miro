package org.cthul.miro.composer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.composer.template.InternalQueryComposer;
import org.cthul.miro.composer.template.QueryPartType;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.util.Key;

/**
 *
 */
public class QueryParts {
    
//    public static <B> ComposableTemplate<B> constPart(QueryPart<? super B> part) {
//        return (key, query) -> query.addPart(key, part);
//    }
//    
//    public static <B> ComposableTemplate<B> createPart(Supplier<? extends QueryPart<? super B>> factory) {
//        return (key, query) -> query.addPart(key, factory.get());
//    }
//    
//    public static <B> ComposableTemplate<B> createPart(Function<? super InternalQueryComposer<? extends B>, ? extends QueryPart<? super B>> factory) {
//        return (key, query) -> query.addPart(key, factory.apply(query));
//    }
//    
    
    public static <B> ComposableTemplate<B> constNodePart(QueryPart<? super B> nodePart) {
        return (key, query) -> {
            QueryPart<? super B> qp = nodePart;
            query.addPart(qp);
            query.addNode((Key) key, qp);
        };
    }
    
    public static <B> ComposableTemplate<B> newNodePart(Function<? super InternalQueryComposer<? extends B>, ? extends QueryPart<? super B>> factory) {
        return (key, query) -> {
            QueryPart<? super B> qp = factory.apply(query);
            query.addPart(qp);
            query.addNode((Key) key, qp);
        };
    }
    
    public static <B> ComposableTemplate<B> newNodePart(Supplier<? extends QueryPart<? super B>> factory) {
        return (key, query) -> {
            QueryPart<? super B> qp = factory.get();
            query.addPart(qp);
            query.addNode((Key) key, qp);
        };
    }
    
    public static ComposableTemplate<Object> require(Object key) {
        return (k, query) -> { 
            query.require(key); 
        };
    }
    
    public static ComposableTemplate<Object> require(Object... keys) {
        return (k, query) -> { 
            query.requireAll(keys); 
        };
    }
    
    public static ComposableTemplate<Object> require(Iterable<?> keys) {
        return (k, query) -> { 
            query.requireAll(keys);
        };
    }
    
    public static <V> KeyTemplate<Object, V> link(Key<V> key) {
        return new KeyTemplate<Object, V>() {
            @Override
            public Key<V> getKey() { return key; }

            @Override
            public void addTo(Object k, InternalQueryComposer<? extends Object> query) {
                query.addNode((Key) k, query.part(key));
            }
        };
    }
    
    public static <B, V> KeyTemplate<B, V> redirect(Key<V> key, Template<? super B> template) {
        return new Redirect<>(template, key);
    }
    
    public static <B> ComposableTemplate<B> compose(Template<? super B> template) {
        if (template instanceof ComposableTemplate) {
            return (ComposableTemplate<B>) template;
        }
        return template::addTo;
    }
    
    public static <V> KeyTemplate<Object, V> setUp(Key<V> key, Consumer<? super V> action) {
        return new KeyTemplate<Object, V>() {
            @Override
            public Key<V> getKey() { return key; }
            @Override
            public void addTo(Object k, InternalQueryComposer<? extends Object> query) {
                action.accept(query.part(key));
            }
        };
    }

    public static ComposableTemplate<Object> noOp() {
        return Special.NO_OP;
    }
//    
//    public static NoOp noOpIgnoreArgs() {
//        return NO_OP;
//    }
//    
//    public static ProxyTemplate proxy(Object... keys) {
//        return new ProxyTemplate(keys);
//    }
//    
//    public static ProxyTemplate proxy(Iterable<Object> keys) {
//        List<Object> list = new ArrayList<>();
//        keys.forEach(list::add);
//        return new ProxyTemplate(list.toArray());
//    }
    
    public static interface ComposableTemplate<Builder> extends QueryPartType<Builder> {
        
        default <B extends Builder, Value> KeyTemplate<B, Value> transform(Key<Value> key, BiFunction<KeyQueryComposer<? extends Builder, Value>, ? super Value, ? extends Value> action) {
            return hook(key, (q, v) -> {
               v = action.apply(q, v);
               if (v != null) q.addNode(key, v);
            });
        }
        
        default <B extends Builder, Value> KeyTemplate<B, Value> transform(Key<Value> key, Function<? super Value, ? extends Value> action) {
            return hook(key, (q, v) -> {
               v = action.apply(v);
               if (v != null) q.addNode(key, v);
            });
        }
        
        @SuppressWarnings("Convert2Lambda")
        default <B extends Builder, Value> KeyTemplate<B, Value> hook(Key<Value> key, BiConsumer<? super KeyQueryComposer<? extends B, Value>, ? super Value> action) {
            return wrap(key, new NodeWrapper<B>() {
                @Override
                public <V> void addNode(Key<V> k, V v, InternalQueryComposer<? extends B> q) {
                   if (k == key) {
                       action.accept(new KeyQueryComposerImpl<>(key, q), (Value) v);
                   }  else {
                       q.addNode(k, v);
                   }
                }
            });
        }
        
        default <B extends Builder> ComposableTemplate<B> wrap(NodeWrapper<B> action) {
            return new TemplateWrapper<>(this, action);
        }
        
        default <Value, B extends Builder> KeyTemplate<B, Value> wrap(Key<Value> key, NodeWrapper<B> action) {
            return new KeyTemplateWrapper<>(key, this, action);
        }
        
        default <B extends Builder> ComposableTemplate<B> and(Template<? super B> template) {
            return new MultiTemplate<>(this, template);
        }
        
        default <B extends Builder> ComposableTemplate<B> andNode(Template<? super B> template) {
            return new ComposableNodeTemplate<>(this, template);
        }
        
        default <B extends Builder, Value> KeyTemplate<B, Value> andNode(Key<Value> key, Template<? super B> template) {
            return new ComposableKeyNodeTemplate<>(key, this, template);
        }
        
        default <B extends Builder, Value> KeyTemplate<B, Value> andNode(KeyTemplate<? super B, Value> template) {
            return andNode(template.getKey(), template);
        }
        
        default <B extends Builder> ComposableTemplate<B> andNodePart(QueryPart<? super B> part) {
            return andNode(constNodePart(part));
        }
        
        default <B extends Builder> ComposableTemplate<B> andNewNodePart(Supplier<? extends QueryPart<? super B>> factory) {
            return andNode(newNodePart(factory));
        }
        
        default <B extends Builder> ComposableTemplate<B> andNewNodePart(Function<? super InternalQueryComposer<? extends B>, ? extends QueryPart<? super B>> factory) {
            return andNode(newNodePart(factory));
        }
        
        default <B extends Builder> ComposableTemplate<B> andRequire(Object... keys) {
            return and(require(keys));
        }
        
        default <B extends Builder> ComposableTemplate<B> andRequire(Iterable<Object> keys) {
            return and(require(keys));
        }
//        
//        default <B extends Builder> ComposableTemplate<B> andProxy(Object... keys) {
//            return and(proxy(keys));
//        }
//        
//        default <B extends Builder> ComposableTemplate<B> andProxy(Iterable<Object> keys) {
//            return and(proxy(keys));
//        }
//        
        default <B extends Builder, V> KeyTemplate<B, V> andSetUp(Key<V> key, Consumer<? super V> action) {
            return andNode(setUp(key, action));
        }
        
        default <B extends Builder, V> KeyTemplate<B, V> andLink(Key<V> key) {
            return andNode(link(key));
        }
    }
    
    public static interface KeyTemplate<Builder, Value> extends ComposableTemplate<Builder> {
        
        Key<Value> getKey();

        @Override
        default <B extends Builder> KeyTemplate<B, Value> andNode(Template<? super B> template) {
            return andNode(getKey(), template);
        }
        
        default <B extends Builder> KeyTemplate<B, Value> hook(BiConsumer<? super QueryParts.KeyQueryComposer<? extends B, Value>, ? super Value> action) {
            return this.hook(getKey(), action);
        }

        default <B extends Builder> KeyTemplate<B, Value> transform(Function<? super Value, ? extends Value> action) {
            return this.transform(getKey(), action);
        }

        default <B extends Builder> KeyTemplate<B, Value> transform(BiFunction<QueryParts.KeyQueryComposer<? extends Builder, Value>, ? super Value, ? extends Value> action) {
            return this.transform(getKey(), action);
        }
        
        default <B extends Builder> KeyTemplate<B, Value> andSetUp(Consumer<? super Value> action) {
            return this.andSetUp(getKey(), action);
        }
    }
    
    private static enum Special implements ComposableTemplate<Object> {
        
        NO_OP {
            @Override
            public void addTo(Object key, InternalQueryComposer<? extends Object> query) { }
        };
    }
    
    public static final class MultiTemplate<Builder> implements ComposableTemplate<Builder> {
        private final List<Template<? super Builder>> templates = new ArrayList<>();

        public MultiTemplate(Template<? super Builder>... templates) {
            for (Template<? super Builder> t: templates) {
                if (t instanceof MultiTemplate) {
                    this.templates.addAll(((MultiTemplate<Builder>) t).templates);
                } else {
                    this.templates.add(t);
                }
            }
        }

        @Override
        public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
            templates.forEach(t -> t.addTo(key, query));
        }
    }
    
    public static class TemplateWrapper<Builder> implements ComposableTemplate<Builder> {
        
        private final Template<? super Builder> actualTemplate;
        private final NodeWrapper<? super Builder> wrapper;

        public TemplateWrapper(Template<? super Builder> actualTemplate, NodeWrapper<? super Builder> wrapper) {
            this.actualTemplate = actualTemplate;
            this.wrapper = wrapper;
        }

        @Override
        public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
            InternalQueryComposer<? extends Builder> q2 = new InternalQueryComposerDelegator<Builder>(query, null) {
                @Override
                public <V> void addNode(Key<V> key, V node) {
                    wrapper.addNode(key, node, query);
                }
            };
            actualTemplate.addTo(key, q2);
        }
    }
    
    public static class KeyTemplateWrapper<Builder, Value> extends TemplateWrapper<Builder> implements KeyTemplate<Builder, Value> {

        private final Key<Value> key;

        public KeyTemplateWrapper(Key<Value> key, Template<? super Builder> actualTemplate, NodeWrapper<? super Builder> wrapper) {
            super(actualTemplate, wrapper);
            this.key = key;
        }
        
        @Override
        public Key<Value> getKey() {
            return key;
        }
    }
    
    public static interface NodeWrapper<Builder> {
        
        <Value> void addNode(Key<Value> key, Value value, InternalQueryComposer<? extends Builder> query);
    }
    
    public static interface KeyQueryComposer<Builder, Value> extends InternalQueryComposer<Builder> {
        
        Key<Value> getKey();
        
        void addNode(Value node);
    }
    
    public static class InternalQueryComposerDelegator<Builder> implements InternalQueryComposer<Builder> {
        private final InternalQueryComposer<?> delegatee;
        private final InternalQueryComposer<? extends Builder> partDelegatee;

        public InternalQueryComposerDelegator(InternalQueryComposer<? extends Builder> delegatee) {
            this(delegatee, delegatee);
        }

        public InternalQueryComposerDelegator(InternalQueryComposer<?> delegatee, InternalQueryComposer<? extends Builder> partDelegatee) {
            this.delegatee = delegatee;
            this.partDelegatee = partDelegatee;
        }
        
        public InternalQueryComposer<?> getDelegatee() {
            return delegatee;
        }
        
        public InternalQueryComposer<? extends Builder> getPartDelegatee() {
            if (partDelegatee == null) {
                throw new UnsupportedOperationException();
            }
            return partDelegatee;
        }
        @Override
        public void addPart(QueryPart<? super Builder> part) {
            getPartDelegatee().addPart(part);
        }
        @Override
        public <V> void addNode(Key<V> key, V node) {
            getDelegatee().addNode(key, node);
        }
        @Override
        public void require(Object key) {
            getDelegatee().require(key);
        }
        @Override
        public <V> V part(Key<V> key) {
            return getDelegatee().part(key);
        }
    }
    
    public static class KeyQueryComposerImpl<Builder, Value> 
                    extends InternalQueryComposerDelegator<Builder> 
                    implements KeyQueryComposer<Builder, Value> {

        private final Key<Value> key;

        public KeyQueryComposerImpl(Key<Value> key, InternalQueryComposer<?> delegatee) {
            super(delegatee, null);
            this.key = key;
        }

        @Override
        public Key<Value> getKey() {
            return key;
        }

        @Override
        public void addNode(Value node) {
            addNode(key, node);
        }
    }
    
    public static interface ComposableNode<Other> {
        
        Object and(Other other);
    }
    
    public static class ComposableNodeTemplate<Other, Builder> implements ComposableTemplate<Builder> {

        private final Template<? super Builder> t1;
        private final Template<? super Builder> t2;

        public ComposableNodeTemplate(Template<? super Builder> t1, Template<? super Builder> t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        @Override
        public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
            AtomicBoolean usedKey = new AtomicBoolean(false);
            InternalQueryComposer<Builder> q2 = new InternalQueryComposerDelegator<Builder>(query) {
                @Override
                public <V1> void addNode(Key<V1> key1, V1 node1) {
                    if (key == key1) usedKey.set(true);
                    if (node1 instanceof ComposableNode) {
                        AtomicBoolean usedNode = new AtomicBoolean(false);
                        InternalQueryComposer<Builder> q3 = new InternalQueryComposerDelegator<Builder>(query) {
                            @Override
                            public <V2> void addNode(Key<V2> key2, V2 node2) {
                                if (key1 == key2) {
                                    usedNode.set(true);
                                    Object node3 = ((ComposableNode<V2>) node1).and(node2);
                                    super.addNode(key2, (V2) node3);
                                } else {
                                    super.addNode(key2, node2);
                                }
                            }
                        };
                        t2.addTo(key1, q3);
                        if (usedNode.get() == false) {
                            super.addNode(key1, node1);
                        } 
                    } else {
                        super.addNode(key1, node1);
                        t2.addTo(key1, query);
                    }
                }
            };
            t1.addTo(key, q2);
            if (usedKey.get() == false) {
                t2.addTo(key, query);
            }
        }
    }
    
    public static class ComposableKeyNodeTemplate<Other, Builder, Value> extends ComposableNodeTemplate<Other, Builder> implements KeyTemplate<Builder, Value> {
        
        private final Key<Value> key;

        public ComposableKeyNodeTemplate(Key<Value> key, Template<? super Builder> t1, Template<? super Builder> t2) {
            super(t1, t2);
            this.key = key;
        }

        @Override
        public Key<Value> getKey() {
            return key;
        }
    }
    
    public static class Redirect<Builder, Value> implements KeyTemplate<Builder, Value> {

        private final Template<? super Builder> template;
        private final Key<Value> actualKey;

        public Redirect(Template<? super Builder> parent, Key<Value> actualKey) {
            this.template = parent;
            this.actualKey = actualKey;
        }

        @Override
        public Key<Value> getKey() {
            return actualKey;
        }

        @Override
        public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
            template.addTo(actualKey, query);
        }
    }
    
//    public static class CfgSetUp extends NoOp {
//        private final Consumer<? super Object[]> action;
//        public CfgSetUp(Consumer<? super Object[]> action) {
//            this.action = action;
//        }
//        @Override
//        public void setUp(Object... args) {
//            action.accept(args);
//        }
//    }
//    
//    public static class CfgPut extends NoOp {
//        private final BiConsumer<Object, ? super Object[]> action;
//        public CfgPut(BiConsumer<Object, ? super Object[]> action) {
//            this.action = action;
//        }
//        @Override
//        public void put(Object key, Object... args) {
//            action.accept(key, args);
//        }
//        @Override
//        public void setUp(Object... args) {
//            action.accept(null, args);
//        }
//    }
//    
//    public static class CfgDo<Builder> implements ComposableTemplate<Builder> {
//        private final BiConsumer<Object, ? super InternalQueryComposer<? extends Builder>> action;
//        public CfgDo(BiConsumer<Object, ? super InternalQueryComposer<? extends Builder>> action) {
//            this.action = action;
//        }
//        @Override
//        public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
//            noOp().addTo(key, query);
//            action.accept(key, query);
//        }
//    }
//    
//    public static class MultiTemplate<Builder> implements ComposableTemplate<Builder> {
//
//        private final QueryPartType<? super Builder>[] templates;
//
//        public MultiTemplate(QueryPartType<? super Builder>... templates) {
//            this.templates = templates;
//        }
//        
//        @Override
//        public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
//            List<Object> keys = new ArrayList<>(templates.length);
//            for (QueryPartType<? super Builder> t: templates) {
//                Object k = new Object();
//                keys.add(k);
//                t.addTo(k, query);
//            }
//            query.addPart(key, new ProxyPart(keys.toArray(), query));
//        }
//
//        @Override
//        public <B extends Builder> ComposableTemplate<B> and(QueryPartType<? super B> template) {
//            QueryPartType<? super B>[] more = Arrays.copyOf(templates, templates.length+1);
//            more[templates.length] = template;
//            return new MultiTemplate<>(more);
//        }
//    }
//    
//    public static class ProxyTemplate implements ComposableTemplate<Object> {
//        
//        private final Object[] keys;
//
//        public ProxyTemplate(Object... keys) {
//            this.keys = keys;
//        }
//
//        @Override
//        public void addTo(Object key, InternalQueryComposer<? extends Object> query) {
//            query.requireAll(keys);
//            query.addPart(key, new ProxyPart(keys, query));
//        }
//    }
//    
//    public static class ProxyPart implements QueryPart<Object> {
//        
//        private final Object[] keys;
//        private final InternalQueryComposer<? extends Object> query;
//
//        public ProxyPart(Object[] keys, InternalQueryComposer<? extends Object> query) {
//            this.keys = keys;
//            this.query = query;
//        }
//
//        @Override
//        public void put(Object key, Object... args) {
//            for (Object k : keys) {
//                query.put2(k, key, args);
//            }
//        }
//
//        @Override
//        public void setUp(Object... args) {
//            for (Object k : keys) {
//                query.put(k, args);
//            }
//        }
//        
//        @Override
//        public void addTo(Object builder) {
//        }
//    }
}
