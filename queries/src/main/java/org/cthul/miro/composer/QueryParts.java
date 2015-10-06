package org.cthul.miro.composer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.composer.template.InternalQueryComposer;
import org.cthul.miro.composer.template.QueryPartType;

/**
 *
 */
public class QueryParts {
    
    public static <B> ComposableTemplate<B> constPart(QueryPart<? super B> part) {
        return (key, query) -> query.addPart(key, part);
    }
    
    public static <B> ComposableTemplate<B> createPart(Supplier<? extends QueryPart<? super B>> factory) {
        return (key, query) -> query.addPart(key, factory.get());
    }
    
    public static <B> ComposableTemplate<B> createPart(Function<? super InternalQueryComposer<? extends B>, ? extends QueryPart<? super B>> factory) {
        return (key, query) -> query.addPart(key, factory.apply(query));
    }
    
    public static ComposableTemplate<Object> require(Object key) {
        return (k, query) -> { 
            query.require(key); 
            noOpIgnoreArgs().addTo(k, query);
        };
    }
    
    public static ComposableTemplate<Object> require(Object... keys) {
        return (k, query) -> { 
            query.requireAll(keys); 
            noOpIgnoreArgs().addTo(k, query);
        };
    }
    
    public static ComposableTemplate<Object> require(Iterable<?> keys) {
        return (k, query) -> { 
            query.requireAll(keys); 
            noOpIgnoreArgs().addTo(k, query);
        };
    }
    
    public static ComposableTemplate<Object> setUp(Consumer<? super Object[]> action) {
        return new CfgSetUp(action);
    }
    
    public static ComposableTemplate<Object> setUp(BiConsumer<Object, ? super Object[]> action) {
        return new CfgPut(action);
    }
    
    public static <Builder> ComposableTemplate<Builder> init(Consumer<? super InternalQueryComposer<? extends Builder>> action) {
        return init((key, query) -> action.accept(query));
    }
    
    public static <Builder> ComposableTemplate<Builder> init(BiConsumer<Object, ? super InternalQueryComposer<? extends Builder>> action) {
        return new CfgDo<>(action);
    }
    
    public static ComposableTemplate<Object> put(Object key, Object... args) {
        return init((k, q) -> q.put(key, args));
    }
    
    public static NoOp noOp() {
        return NO_OP;
    }
    
    public static NoOp noOpIgnoreArgs() {
        return NO_OP;
    }
    
    public static ProxyTemplate proxy(Object... keys) {
        return new ProxyTemplate(keys);
    }
    
    public static ProxyTemplate proxy(Iterable<Object> keys) {
        List<Object> list = new ArrayList<>();
        keys.forEach(list::add);
        return new ProxyTemplate(list.toArray());
    }
    
    public static interface ComposableTemplate<Builder> extends QueryPartType<Builder> {
        
        default <B extends Builder> ComposableTemplate<B> and(QueryPartType<? super B> template) {
            return new MultiTemplate<>(this, template);
        }
        
        default <B extends Builder> ComposableTemplate<B> andAdd(QueryPart<? super B> part) {
            return and(constPart(part));
        }
        
        default <B extends Builder> ComposableTemplate<B> andCreate(Supplier<? extends QueryPart<? super B>> factory) {
            return and(createPart(factory));
        }
        
        default <B extends Builder> ComposableTemplate<B> andCreate(Function<? super InternalQueryComposer<? extends B>, ? extends QueryPart<? super B>> factory) {
            return and(createPart(factory));
        }
        
        default <B extends Builder> ComposableTemplate<B> andRequire(Object... keys) {
            return and(require(keys));
        }
        
        default <B extends Builder> ComposableTemplate<B> andRequire(Iterable<Object> keys) {
            return and(require(keys));
        }
        
        default <B extends Builder> ComposableTemplate<B> andProxy(Object... keys) {
            return and(proxy(keys));
        }
        
        default <B extends Builder> ComposableTemplate<B> andProxy(Iterable<Object> keys) {
            return and(proxy(keys));
        }
        
        default <B extends Builder> ComposableTemplate<B> andSetUp(Consumer<? super Object[]> action) {
            return and(setUp(action));
        }
        
        default <B extends Builder> ComposableTemplate<B> andSetUp(BiConsumer<Object, ? super Object[]> action) {
            return and(setUp(action));
        }
    }
    
    public static final NoOp NO_OP = new NoOp();
    public static final NoOpIgnoreArgs NO_OP_IGNORE_ARGS = new NoOpIgnoreArgs();
    
    public static class NoOp implements ComposableTemplate<Object>, QueryPart<Object> {
        @Override
        public void addTo(Object key, InternalQueryComposer<? extends Object> query) {
            query.addPart(key, this);
        }
        @Override
        public void addTo(Object builder) {
        }
    }
    
    public static class NoOpIgnoreArgs extends NoOp {
        @Override
        public void put(Object key, Object... args) {
        }
        @Override
        public void setUp(Object... args) {
        }
    }
    
    public static class CfgSetUp extends NoOp {
        private final Consumer<? super Object[]> action;
        public CfgSetUp(Consumer<? super Object[]> action) {
            this.action = action;
        }
        @Override
        public void setUp(Object... args) {
            action.accept(args);
        }
    }
    
    public static class CfgPut extends NoOp {
        private final BiConsumer<Object, ? super Object[]> action;
        public CfgPut(BiConsumer<Object, ? super Object[]> action) {
            this.action = action;
        }
        @Override
        public void put(Object key, Object... args) {
            action.accept(key, args);
        }
        @Override
        public void setUp(Object... args) {
            action.accept(null, args);
        }
    }
    
    public static class CfgDo<Builder> implements ComposableTemplate<Builder> {
        private final BiConsumer<Object, ? super InternalQueryComposer<? extends Builder>> action;
        public CfgDo(BiConsumer<Object, ? super InternalQueryComposer<? extends Builder>> action) {
            this.action = action;
        }
        @Override
        public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
            noOp().addTo(key, query);
            action.accept(key, query);
        }
    }
    
    public static class MultiTemplate<Builder> implements ComposableTemplate<Builder> {

        private final QueryPartType<? super Builder>[] templates;

        public MultiTemplate(QueryPartType<? super Builder>... templates) {
            this.templates = templates;
        }
        
        @Override
        public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
            List<Object> keys = new ArrayList<>(templates.length);
            for (QueryPartType<? super Builder> t: templates) {
                Object k = new Object();
                keys.add(k);
                t.addTo(k, query);
            }
            query.addPart(key, new ProxyPart(keys.toArray(), query));
        }

        @Override
        public <B extends Builder> ComposableTemplate<B> and(QueryPartType<? super B> template) {
            QueryPartType<? super B>[] more = Arrays.copyOf(templates, templates.length+1);
            more[templates.length] = template;
            return new MultiTemplate<>(more);
        }
    }
    
    public static class ProxyTemplate implements ComposableTemplate<Object> {
        
        private final Object[] keys;

        public ProxyTemplate(Object... keys) {
            this.keys = keys;
        }

        @Override
        public void addTo(Object key, InternalQueryComposer<? extends Object> query) {
            query.requireAll(keys);
            query.addPart(key, new ProxyPart(keys, query));
        }
    }
    
    public static class ProxyPart implements QueryPart<Object> {
        
        private final Object[] keys;
        private final InternalQueryComposer<? extends Object> query;

        public ProxyPart(Object[] keys, InternalQueryComposer<? extends Object> query) {
            this.keys = keys;
            this.query = query;
        }

        @Override
        public void put(Object key, Object... args) {
            for (Object k : keys) {
                query.put2(k, key, args);
            }
        }

        @Override
        public void setUp(Object... args) {
            for (Object k : keys) {
                query.put(k, args);
            }
        }
        
        @Override
        public void addTo(Object builder) {
        }
    }
}
