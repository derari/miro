package org.cthul.miro.request.template;

import org.cthul.miro.request.part.CopyManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.impl.AdaptedTemplate;
import org.cthul.miro.util.Key;

/**
 * Provides some common implementations for templates and parts.
 */
public class Templates { 
    
    /**
     * Creates a template that always adds {@code nodePart} for any key.
     * @param <B>
     * @param nodePart
     * @return template
     */
    public static <B> ComposableTemplate<B> constNodePart(StatementPart<? super B> nodePart) {
        return new ComposableTemplate<B>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends B> composer) {
                composer.addPart((Key) key, nodePart);
            }
            @Override
            public String toString() {
                return "node part: " + nestedString(nodePart);
            }
        };
    }
    
    /**
     * Creates a template that always adds {@code nodePart} for any key.
     * @param node
     * @return template
     */
    public static ComposableTemplate<?> constNode(Object node) {
        return new ComposableTemplate<Object>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends Object> composer) {
                composer.addNode((Key) key, node);
            }
            @Override
            public String toString() {
                return "node: " + nestedString(node);
            }
        };
    }
    
    /**
     * Creates a template that always creates new node parts for any key.
     * @param <B>
     * @param factory
     * @return template
     */
    public static <B> ComposableTemplate<B> newNodePart(Function<? super InternalComposer<? extends B>, ? extends StatementPart<? super B>> factory) {
        return new ComposableTemplate<B>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends B> composer) {
                StatementPart<? super B> qp = factory.apply(composer);
                composer.addPart((Key) key, qp);
            }
            @Override
            public String toString() {
                return "new node part: " + nestedString(factory);
            }
        };
    }
    
    /**
     * Creates a template that always creates new node parts for any key.
     * @param <B>
     * @param factory
     * @return template
     */
    public static <B> ComposableTemplate<B> newNodePart(Supplier<? extends StatementPart<? super B>> factory) {
        return new ComposableTemplate<B>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends B> composer) {
                StatementPart<? super B> qp = factory.get();
                composer.addPart((Key) key, qp);
            }
            @Override
            public String toString() {
                return "new node part: " + nestedString(factory);
            }
        };
    }
    
    /**
     * Creates a template that always creates new parts.
     * @param <B>
     * @param factory
     * @return template
     */
    public static <B> ComposableTemplate<B> newPart(Function<? super InternalComposer<? extends B>, ? extends StatementPart<? super B>> factory) {
        return new ComposableTemplate<B>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends B> composer) {
                StatementPart<? super B> qp = factory.apply(composer);
                composer.addPart(qp);
            }
            @Override
            public String toString() {
                return "new part: " + nestedString(factory);
            }
        };
    }
    
    /**
     * Creates a template that always creates new parts.
     * @param <B>
     * @param factory
     * @return template
     */
    public static <B> ComposableTemplate<B> newPart(Supplier<? extends StatementPart<? super B>> factory) {
        return new ComposableTemplate<B>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends B> composer) {
                StatementPart<? super B> qp = factory.get();
                composer.addPart(qp);
            }
            @Override
            public String toString() {
                return "new part: " + nestedString(factory);
            }
        };
    }
    
    /**
     * Creates a template that always creates new node parts for any key.
     * @param <B>
     * @param factory
     * @return template
     */
    public static <B> ComposableTemplate<B> newNode(Function<? super InternalComposer<? extends B>, ?> factory) {
        return new ComposableTemplate<B>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends B> composer) {
                Object qp = factory.apply(composer);
                composer.addNode((Key) key, qp);
            }
            @Override
            public String toString() {
                return "new node: " + nestedString(factory);
            }
        };
    }
    
    /**
     * Creates a template that always creates new node parts for any key.
     * @param <B>
     * @param factory
     * @return template
     */
    public static <B> ComposableTemplate<B> newNode(Supplier<?> factory) {
        return new ComposableTemplate<B>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends B> composer) {
                Object qp = factory.get();
                composer.addNode((Key) key, qp);
            }
            @Override
            public String toString() {
                return "new node: " + nestedString(factory);
            }
        };
    }
    
    /**
     * Creates a template that will require the {@code key}.
     * @param key
     * @return template
     */
    public static ComposableTemplate<Object> require(Object key) {
        return new ComposableTemplate<Object>() {
            @Override
            public void addTo(Object k, InternalComposer<? extends Object> composer) {
                composer.require(key); 
            }
            @Override
            public String toString() {
                return "require " + key;
            }
        };
    }
    
    /**
     * Creates a template that will require all {@code keys}.
     * @param keys
     * @return template
     */
    public static ComposableTemplate<Object> require(Object... keys) {
        return new ComposableTemplate<Object>() {
            @Override
            public void addTo(Object k, InternalComposer<? extends Object> composer) {
                composer.requireAll(keys); 
            }
            @Override
            public String toString() {
                return "require " + Arrays.toString(keys);
            }
        };
    }
    
    /**
     * Creates a template that will require all {@code keys}.
     * @param keys
     * @return template
     */
    public static ComposableTemplate<Object> require(Iterable<?> keys) {
        return new ComposableTemplate<Object>() {
            @Override
            public void addTo(Object k, InternalComposer<? extends Object> composer) {
                composer.requireAll(keys); 
            }
            @Override
            public String toString() {
                return "require " + StreamSupport.stream(keys.spliterator(), false)
                        .map(Object::toString).collect(Collectors.joining(", "));
            }
        };
    }
    
    /**
     * Creates a template that will link any key to the node at {@code key}.
     * @param <V>
     * @param key
     * @return template
     */
    public static <V> KeyTemplate<Object, V> link(Key<V> key) {
        return new KeyTemplate<Object, V>() {
            @Override
            public Key<V> getKey() { return key; }
            @Override
            public void addTo(Object k, InternalComposer<? extends Object> query) {
                query.addNode((Key) k, query.node(key));
            }
            @Override
            public String toString() {
                return "link to " + key;
            }
        };
    }
    
    /**
     * Creates a template that will call {@code template} with the specified {@code key}.
     * @param <B>
     * @param <V>
     * @param key
     * @param template
     * @return template
     */
    public static <B, V> KeyTemplate<B, V> redirect(Key<V> key, Template<? super B> template) {
        return new KeyRedirect<>(template, key);
    }
    
    public static <B> ComposableTemplate<B> action(Consumer<InternalComposer<? extends B>> action) {
        return new ComposableTemplate<B>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends B> composer) {
                action.accept(composer);
            }
            @Override
            public String toString() {
                return "do " + nestedString(action);
            }
        };
    }
    
    /**
     * Wraps {@code template} to allow fluent composition.
     * @param <B>
     * @param template
     * @return template
     */
    public static <B> ComposableTemplate<B> compose(Template<? super B> template) {
        if (template instanceof ComposableTemplate) {
            return (ComposableTemplate<B>) template;
        }
        return new ComposableTemplate<B>() {
            @Override
            public void addTo(Object key, InternalComposer<? extends B> composer) {
                template.addTo(key, composer);
            }
            @Override
            public String toString() {
                return String.valueOf(template);
            }
        };
    }
    
    public static <B> ComposableTemplate<B> all(Template<? super B>... templates) {
        if (templates == null || templates.length == 0) return (ComposableTemplate) noOp();
        if (templates.length == 1) return compose(templates[0]);
        return new MultiTemplate<>(templates);
    }
    
    public static <B> ComposableTemplate<B> all(Collection<? extends Template<? super B>> templates) {
        if (templates == null || templates.isEmpty()) return (ComposableTemplate) noOp();
        if (templates.size() == 1) return compose(templates.iterator().next());
        return new MultiTemplate<>(templates);
    }
    
    public static <B> ComposableTemplate<B> all(Iterable<? extends Template<? super B>> templates) {
        return new MultiTemplate<>(templates);
    }
    
    /**
     * Creates a template that will run an {@code action} on the value of {@code key}.
     * @param <V>
     * @param key
     * @param action
     * @return template
     */
    public static <V> KeyTemplate<Object, V> setUp(Key<V> key, Consumer<? super V> action) {
        return new KeyTemplate<Object, V>() {
            @Override
            public Key<V> getKey() { return key; }
            @Override
            public void addTo(Object k, InternalComposer<? extends Object> query) {
                action.accept(query.node(key));
            }
            @Override
            public String toString() {
                return "setUp " + key;
            }
        };
    }

    /**
     * Returns a template that does nothing.
     * @return template
     */
    public static ComposableTemplate<Object> noOp() {
        return Special.NO_OP;
    }
    
    public static <N extends ComposableNode<N>> N allNodes(Iterable<N> nodes) {
        Iterator<N> it = nodes.iterator();
        if (!it.hasNext()) return null;
        N n = it.next();
        while (it.hasNext()) {
            n = (N) n.and(it.next());
        }
        return n;
    }
    
    /**
     * A template with helper methods for fluent composition.
     * @param <Builder> 
     */
    public static interface ComposableTemplate<Builder> extends Template<Builder> {
        
        default <B extends Builder, Value> KeyTemplate<B, Value> key(Key<Value> key) {
            return new KeyTemplate<B, Value>() {
                @Override
                public Key<Value> getKey() {
                    return key;
                }
                @Override
                public void addTo(Object key, InternalComposer<? extends B> query) {
                    ComposableTemplate.this.addTo(key, query);
                }
            };
        }
        
        /**
         * Creates a template that will transform the value that
         * this template added for {@code key}.
         * @param <B>
         * @param <Value>
         * @param key
         * @param action
         * @return template
         */
        default <B extends Builder, Value> KeyTemplate<B, Value> transform(Key<Value> key, BiFunction<KeyQueryComposer<? extends Builder, Value>, ? super Value, ? extends Value> action) {
            return hook(key, (q, v) -> {
               v = action.apply(q, v);
               if (v != null) q.addNode(key, v);
            });
        }
        
        /**
         * Creates a template that will transform the value that
         * this template added for {@code key}.
         * @param <B>
         * @param <Value>
         * @param key
         * @param action
         * @return template
         */
        default <B extends Builder, Value> KeyTemplate<B, Value> transform(Key<Value> key, Function<? super Value, ? extends Value> action) {
            return hook(key, (q, v) -> {
               v = action.apply(v);
               if (v != null) q.addNode(key, v);
            });
        }
        
        /**
         * Creates a template that will perform an {@code actien} when
         * this template tries to add a value for {@code key}.
         * @param <B>
         * @param <Value>
         * @param key
         * @param action
         * @return template
         */
        @SuppressWarnings("Convert2Lambda")
        default <B extends Builder, Value> KeyTemplate<B, Value> hook(Key<Value> key, BiConsumer<? super KeyQueryComposer<? extends B, Value>, ? super Value> action) {
            return wrap(key, new NodeWrapper<B>() {
                @Override
                public <V> void addNode(Key<V> k, V v, InternalComposer<? extends B> q) {
                   if (k == key) {
                       action.accept(new KeyQueryComposerImpl<>(key, q), (Value) v);
                   }  else {
                       q.addNode(k, v);
                   }
                }
            });
        }
        
        /**
         * Creates a template that will perform an {@code action} whenever
         * this template tries to add a value for any key.
         * @param <B>
         * @param action
         * @return template
         */
        default <B extends Builder> ComposableTemplate<B> wrap(NodeWrapper<B> action) {
            return new TemplateWrapper<>(this, action);
        }
        
        /**
         * Creates a template that will perform an {@code action} when this 
         * template tries to add a value for any key.
         * @param <Value>
         * @param <B>
         * @param key
         * @param action
         * @return template
         */
        default <Value, B extends Builder> KeyTemplate<B, Value> wrap(Key<Value> key, NodeWrapper<B> action) {
            return new KeyTemplateWrapper<>(key, this, action);
        }
        
        /**
         * Merges two templates. If both templates need to add nodes for the
         * same key, use {@link #andNode(org.cthul.miro.composer.Template)} instead.
         * @param <B>
         * @param template
         * @return template
         */
        default <B extends Builder> ComposableTemplate<B> and(Template<? super B> template) {
            return new MultiTemplate<>(this, template);
        }
        
        /**
         * When multiple {@link ComposableNode}s are added for the same key,
         * the are merged automatically.
         * @param <B>
         * @param template
         * @return template
         */
        default <B extends Builder> ComposableTemplate<B> andNode(Template<? super B> template) {
            return new MultiComposableNodeTemplate<>(this, template);
        }
        
        /**
         * When multiple {@link ComposableNode}s are added for the same key,
         * the are merged automatically.
         * @param <B>
         * @param <Value>
         * @param key
         * @param template
         * @return template
         */
        default <B extends Builder, Value> KeyTemplate<B, Value> andNode(Key<Value> key, Template<? super B> template) {
            return new MultiComposobaleNodeKeyTemplate<>(key, this, template);
        }
        
        /**
         * When multiple {@link ComposableNode}s are added for the same key,
         * the are merged automatically.
         * @param <B>
         * @param <Value>
         * @param template
         * @return template
         */
        default <B extends Builder, Value> KeyTemplate<B, Value> andNode(KeyTemplate<? super B, Value> template) {
            return andNode(template.getKey(), template);
        }
        
        default <Value, B extends Builder> ComposableTemplate<B> andNewNode(Key<Value> key, Supplier<? extends Value> factory) {
            return andNode(key, newNode(factory));
        }
        
        default <Value, B extends Builder> ComposableTemplate<B> andNewNode(Key<Value> key, Function<? super InternalComposer<? extends B>, Value> factory) {
            return andNode(key, newNode(factory));
        }
        
        default <B extends Builder> ComposableTemplate<B> andNodePart(StatementPart<? super B> part) {
            return andNode(constNodePart(part));
        }
        
        default <B extends Builder> ComposableTemplate<B> andNewNodePart(Supplier<? extends StatementPart<? super B>> factory) {
            return andNode(newNodePart(factory));
        }
        
        default <B extends Builder> ComposableTemplate<B> andNewNodePart(Function<? super InternalComposer<? extends B>, ? extends StatementPart<? super B>> factory) {
            return andNode(newNodePart(factory));
        }
        
        default <B extends Builder> ComposableTemplate<B> andNewPart(Supplier<? extends StatementPart<? super B>> factory) {
            return and(newPart(factory));
        }
        
        default <B extends Builder> ComposableTemplate<B> andNewPart(Function<? super InternalComposer<? extends B>, ? extends StatementPart<? super B>> factory) {
            return and(newPart(factory));
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
        
        default <B extends Builder> ComposableTemplate<B> andDo(Consumer<InternalComposer<? extends B>> action) {
            return andNode(action(action));
        }
    }
    
    public static interface KeyTemplate<Builder, Value> extends ComposableTemplate<Builder> {
        
        Key<Value> getKey();

        @Override
        default <B extends Builder> KeyTemplate<B, Value> andNode(Template<? super B> template) {
            return andNode(getKey(), template);
        }
        
        default <B extends Builder> ComposableTemplate<B> andNewNode(Supplier<? extends Value> factory) {
            return andNode(getKey(), newNode(factory));
        }
        
        default <B extends Builder> ComposableTemplate<B> andNewNode(Function<? super InternalComposer<? extends B>, Value> factory) {
            return andNode(getKey(), newNode(factory));
        }
        
        default <B extends Builder> KeyTemplate<B, Value> hook(BiConsumer<? super Templates.KeyQueryComposer<? extends B, Value>, ? super Value> action) {
            return this.hook(getKey(), action);
        }

        default <B extends Builder> KeyTemplate<B, Value> transform(Function<? super Value, ? extends Value> action) {
            return this.transform(getKey(), action);
        }

        default <B extends Builder> KeyTemplate<B, Value> transform(BiFunction<Templates.KeyQueryComposer<? extends Builder, Value>, ? super Value, ? extends Value> action) {
            return this.transform(getKey(), action);
        }
        
        default <B extends Builder> KeyTemplate<B, Value> andSetUp(Consumer<? super Value> action) {
            return this.andSetUp(getKey(), action);
        }
    }
    
    private static enum Special implements ComposableTemplate<Object> {
        
        NO_OP {
            @Override
            public void addTo(Object key, InternalComposer<? extends Object> query) { }
        };
    }
    
    public static final class MultiTemplate<Builder> implements ComposableTemplate<Builder> {
        private final List<Template<? super Builder>> templates = new ArrayList<>();

        public MultiTemplate(Template<? super Builder>... templates) {
            this(Arrays.asList(templates));
        }

        public MultiTemplate(Iterable<? extends Template<? super Builder>> templates) {
            for (Template<? super Builder> t: templates) {
                if (t instanceof MultiTemplate) {
                    this.templates.addAll(((MultiTemplate<Builder>) t).templates);
                } else {
                    this.templates.add(t);
                }
            }
        }

        @Override
        public void addTo(Object key, InternalComposer<? extends Builder> query) {
            templates.forEach(t -> t.addTo(key, query));
        }
        
        @Override
        public String toString() {
            return templates.stream().map(Object::toString).collect(Collectors.joining(", "));
        }
    }
    
    /**
     * When a template tries to add a node, executes custom action instead.
     * @param <Builder> 
     */
    public static class TemplateWrapper<Builder> implements ComposableTemplate<Builder> {
        
        private final Template<? super Builder> actualTemplate;
        private final NodeWrapper<? super Builder> wrapper;

        public TemplateWrapper(Template<? super Builder> actualTemplate, NodeWrapper<? super Builder> wrapper) {
            this.actualTemplate = actualTemplate;
            this.wrapper = wrapper;
        }

        @Override
        public void addTo(Object key, InternalComposer<? extends Builder> query) {
            InternalComposer<? extends Builder> q2 = new InternalQueryComposerDelegator<Builder>(query, null) {
                @Override
                public <V> void addNode(Key<V> key, V node) {
                    wrapper.addNode(key, node, query);
                }
            };
            actualTemplate.addTo(key, q2);
        }

        @Override
        public String toString() {
            return "<" + actualTemplate + ">";
        }
    }
    
    /**
     * When a template tries to add a node, executes custom action instead.
     * @param <Builder>
     * @param <Value> 
     */
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
    
    /**
     * Adds values to a composer.
     * @param <Builder> 
     */
    public static interface NodeWrapper<Builder> {
        
        <Value> void addNode(Key<Value> key, Value value, InternalComposer<? extends Builder> query);
    }
    
    public static interface KeyQueryComposer<Builder, Value> extends InternalComposer<Builder> {
        
        Key<Value> getKey();
        
        void addNode(Value node);
    }
    
    public static class InternalQueryComposerDelegator<Builder> implements InternalComposer<Builder>, AdaptedTemplate.ComposerWrapper {
        private final InternalComposer<?> delegatee;
        private final InternalComposer<? extends Builder> partDelegatee;

        public InternalQueryComposerDelegator(InternalComposer<? extends Builder> delegatee) {
            this(delegatee, delegatee);
        }

        public InternalQueryComposerDelegator(InternalComposer<?> delegatee, InternalComposer<? extends Builder> partDelegatee) {
            this.delegatee = delegatee;
            this.partDelegatee = partDelegatee;
        }

        /* this is to be compatible with adapted composers */
        @Override
        public InternalComposer<?> getActual() {
            InternalComposer actualParts = ((AdaptedTemplate.ComposerWrapper) getPartDelegatee()).getActual();
            return new InternalQueryComposerDelegator<>(this, actualParts);
        }
        
        public InternalComposer<?> getDelegatee() {
            return delegatee;
        }
        
        public InternalComposer<? extends Builder> getPartDelegatee() {
            if (partDelegatee == null) {
                throw new UnsupportedOperationException();
            }
            return partDelegatee;
        }
        @Override
        public void addPart(StatementPart<? super Builder> part) {
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
        public <V> V node(Key<V> key) {
            return getDelegatee().node(key);
        }
        @Override
        public String toString() {
            return "<" + getDelegatee() + ">";
        }
    }
    
    public static class KeyQueryComposerImpl<Builder, Value> 
                    extends InternalQueryComposerDelegator<Builder> 
                    implements KeyQueryComposer<Builder, Value> {

        private final Key<Value> key;

        public KeyQueryComposerImpl(Key<Value> key, InternalComposer<?> delegatee) {
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
    
    /**
     * Implementations can extend {@link AbstractComposableNode}.
     * @param <Other> 
     */
    public static interface ComposableNode<Other> {
        
        Object and(Other other);
    }
        
    public static class MultiNode<N> implements Copyable<Object> {
        
        private final List<N> nodes = new ArrayList<>();

        public MultiNode(N... nodes) {
            for (N n: nodes) {
                if (n.getClass() == getClass()) {
                    this.nodes.addAll(((MultiNode<N>) n).nodes);
                } else {
                    this.nodes.add(n);
                }
            }
        }
        
        protected void all(Consumer<N> action) {
            nodes.forEach(action);
        }
        
        protected <T> void all(BiConsumer<N, T> action, T arg) {
            all(n -> action.accept(n, arg));
        }

        @Override
        public Object copyFor(InternalComposer<Object> ic) {
            CopyManager cpy = ic.node(CopyManager.key);
            List<N> newNodes = cpy.copyAll(nodes);
            return allNodes((List) newNodes);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return nodes.stream().allMatch(isLatest);
        }

        @Override
        public String toString() {
            return nodes.stream().map(Object::toString).collect(Collectors.joining(","));
        }
    }
    
    public static abstract class AbstractComposableNode<Other> 
                    extends MultiNode<Other>
                    implements ComposableNode<Other> {

        public AbstractComposableNode(Other... nodes) {
            super(nodes);
        }
    }
    
    /**
     * When multiple templates add {@link ComposableNode}s for the same key,
     * they are merged.
     * @param <Other>
     * @param <Builder> 
     */
    public static class MultiComposableNodeTemplate<Other, Builder> implements ComposableTemplate<Builder> {

        private final List<Template<? super Builder>> templates = new ArrayList<>();
//        private final Set<Object> keyGuard = new LinkedHashSet<>();

        public MultiComposableNodeTemplate(Template<? super Builder>... templates) {
            for (Template<? super Builder> t: templates) {
                if (t instanceof MultiComposableNodeTemplate) {
                    this.templates.addAll(((MultiComposableNodeTemplate)t).templates);
                } else {
                    this.templates.add(t);
                }
            }
        }

        @Override
        public void addTo(Object key, InternalComposer<? extends Builder> query) {
            addAllExcept(key, query, null, null);
        }

        protected void addAllExcept(Object key, InternalComposer<? extends Builder> ic, Template<?> skip, ComposableNode<?> skippedValue) {
            CollectTemplates<Builder> collector = new CollectTemplates<>(key, templates, skip, skippedValue);
            InternalComposer<Builder> c2 = new InternalQueryComposerDelegator<Builder>(ic) {
                @Override
                public <V> void addNode(Key<V> key2, V node) {
                    if (key == key2 && (node instanceof ComposableNode)) {
                        collector.add((ComposableNode<?>) node);
                        collector.resume(this);
                    } else if (node instanceof ComposableNode) {
                        addAllExcept(key2, this, collector.getCurrent(), (ComposableNode) node);
                    } else {
                        super.addNode(key2, node);
                    }
                }
                @Override
                public String toString() {
                    return "Multi-" + key;
                }
            };
            collector.resume(c2);
            List<ComposableNode<?>> bag = collector.getBag();
            if (!bag.isEmpty()) {
                Object n = allNodes((List) bag);
                ic.addNode((Key) key, n);
            }
        }

        @Override
        public String toString() {
            return templates.stream().map(Object::toString).collect(Collectors.joining(", "));
        }
    }
    
    protected static class CollectTemplates<Builder> {
        final Object key; 
        final Template<?> skip;
        final ComposableNode<?> skippedValue;
        final List<ComposableNode<?>> bag;
        final Iterator<Template<? super Builder>> iterator;
        Template<? super Builder> current;

        public CollectTemplates(Object key, List<Template<? super Builder>> templates, Template<?> skip, ComposableNode<?> skippedValue) {
            this.key = key;
            this.skip = skip;
            this.skippedValue = skippedValue;
            this.iterator = templates.iterator();
            this.bag = new ArrayList<>(templates.size());
        }
        
        public void resume(InternalComposer<Builder> q2) {
            while (iterator.hasNext()) {
                current = iterator.next();
                if (current == skip) {
                    if (skippedValue != null) {
                        bag.add(skippedValue);
                    }
                } else {
                    current.addTo(key, q2);
                }
            }
        }

        private void add(ComposableNode<?> composableNode) {
            bag.add(composableNode);
        }

        public Template<? super Builder> getCurrent() {
            return current;
        }

        public List<ComposableNode<?>> getBag() {
            return bag;
        }
    }

    protected static class Container<T> {
        T value;
        public Container() { }
        public Container(T value) {
            this.value = value;
        }
    }
    
    /**
     * When multiple templates add {@link ComposableNode}s for the same key,
     * they are merged.
     * @param <Other>
     * @param <Builder>
     * @param <Value> 
     */
    public static class MultiComposobaleNodeKeyTemplate<Other, Builder, Value> extends MultiComposableNodeTemplate<Other, Builder> implements KeyTemplate<Builder, Value> {
        
        private final Key<Value> key;

        public MultiComposobaleNodeKeyTemplate(Key<Value> key, Template<? super Builder> t1, Template<? super Builder> t2) {
            super(t1, t2);
            this.key = key;
        }

        @Override
        public Key<Value> getKey() {
            return key;
        }
    }
    
    /**
     * Calls a template with another key.
     * @param <Builder>
     * @param <Value> 
     */
    public static class KeyRedirect<Builder, Value> implements KeyTemplate<Builder, Value> {

        private final Template<? super Builder> template;
        private final Key<Value> actualKey;

        public KeyRedirect(Template<? super Builder> parent, Key<Value> actualKey) {
            this.template = parent;
            this.actualKey = actualKey;
        }

        @Override
        public Key<Value> getKey() {
            return actualKey;
        }

        @Override
        public void addTo(Object key, InternalComposer<? extends Builder> query) {
            template.addTo(actualKey, query);
        }

        @Override
        public String toString() {
            return template + "(" + actualKey + ")";
        }
    }
    
    private static String nestedString(Object o) {
        String s = String.valueOf(o);
        if (s.length() > 20) {
            int dot = s.lastIndexOf('.');
            if (dot < 0) dot = s.length() - 20;
            s = s.substring(dot);
        }
        return s;
    }
    

}
