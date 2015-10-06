package org.cthul.miro.composer.template;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.composer.AbstractCache;
import org.cthul.miro.composer.QueryPart;
import org.cthul.miro.composer.QueryParts;

/**
 *
 * @param <Builder>
 */
public abstract class AbstractTemplate<Builder> extends AbstractCache<Object, Template<? super Builder>> implements Template<Builder> {

    private final Template<? super Builder> parent;

    public AbstractTemplate(Template<? super Builder> parent) {
        this.parent = parent;
    }

    public AbstractTemplate() {
        this(NO_PARENT);
    }

    @Override
    public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
        getValue(key).addTo(key, query);
    }

    @Override
    protected Template<? super Builder> create(Object key) {
//        if (key instanceof DirectTemplate) {
//            DirectTemplate<? super Builder> dt = (DirectTemplate) key;
//            return dt;
//        }
        Template<? super Builder> t = createPartType(key);
        return t != null ? t : parent;
    }
    
    protected abstract Template<? super Builder> createPartType(Object key);
    
    protected static final Template<Object> NO_PARENT = (k, q) -> {
        throw new IllegalArgumentException(
                "Unknown key: " + k);
    };
    
    public static Template<Object> noTemplate() {
        return NO_PARENT;
    }
    
    protected QueryParts.ComposableTemplate<? super Builder> superPartType(Object key) {
        return new ProxyParentKey<>(parent, key);
    }
    
//    protected InternalBuilder forKey(Object... id) {
//        return new InternalBuilder(id);
//    }
//    
//    protected static interface DirectTemplate<Builder> extends Template<Builder> {
//    }
//    
    protected static class ProxyParentKey<Builder> implements QueryParts.ComposableTemplate<Builder> {

        private final Template<? super Builder> parent;
        private final Object actualKey;

        public ProxyParentKey(Template<? super Builder> parent, Object actualKey) {
            this.parent = parent;
            this.actualKey = actualKey;
        }
                
        @Override
        public void addTo(Object key, InternalQueryComposer<? extends Builder> query) {
            List<Object> altKeys = new ArrayList<>();
            installProxy(key, query, altKeys);
            invokeParent(query, altKeys);
        }

        private void invokeParent(InternalQueryComposer<? extends Builder> query, List<Object> altKeys) {
            InternalQueryComposer<? extends Builder> q2 = new InternalQueryComposer<Builder>() {
                @Override
                public void addPart(Object key, QueryPart<? super Builder> part) {
                    if (key == actualKey) {
                        key = new Object();
                        altKeys.add(key);
                    }
                    query.addPart(key, part);
                }
                @Override
                public void put2(Object key, Object key2, Object... args) {
                    query.put2(key, key2, args);
                }
            };
            parent.addTo(actualKey, q2);
        }

        private QueryPart<Builder> installProxy(Object key, InternalQueryComposer<? extends Builder> query, List<Object> altKeys) {
            QueryPart<Builder> proxy = new QueryPart<Builder>() {
                @Override
                public void put(Object key, Object... args) {
                    altKeys.forEach(k -> query.put2(k, key, args));
                }
                @Override
                public void setUp(Object... args) {
                    altKeys.forEach(k -> query.put(k, args));
                }
                @Override
                public void addTo(Builder builder) { }
            };
            query.addPart(key, proxy);
            return proxy;
        }
    }
//    
//    protected abstract static class InternalKey<Builder> implements DirectTemplate<Builder> {
//
//        private final Object[] id;
//
//        public InternalKey(Object... id) {
//            this.id = id;
//        }
//
//        @Override
//        public int hashCode() {
//            return Arrays.deepHashCode(this.id);
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null || getClass() != obj.getClass()) {
//                return false;
//            }
//            final InternalKey<?> other = (InternalKey<?>) obj;
//            return Arrays.deepEquals(this.id, other.id);
//        }
//        
//        public <B extends Builder> InternalKey<B> andAdd(QueryPart<? super B> qp) {
//            return new InternalKey<B>(id) {
//                @Override
//                public QueryPart<? super B> addTo(Object key, InternalQueryComposer<? extends B> query) {
//                    InternalKey.this.addTo(key, query);
//                    return query.addPart(key, qp);
//                }
//            };
//        }
//        
//        public <B extends Builder> InternalKey<B> andAdd(Supplier<? extends QueryPart<? super B>> supplier) {
//            return new InternalKey<B>(id) {
//                @Override
//                public QueryPart<? super B> addTo(Object key, InternalQueryComposer<? extends B> query) {
//                    InternalKey.this.addTo(key, query);
//                    return query.addPart(key, supplier.get());
//                }
//            };
//        }
//        
//        public <B extends Builder> InternalKey<B> andDo(BiFunction<Object, InternalQueryComposer<? extends B>, QueryPart> bif) {
//            return new InternalKey<B>(id) {
//                @Override
//                public QueryPart<? super B> addTo(Object key, InternalQueryComposer<? extends B> query) {
//                    InternalKey.this.addTo(key, query);
//                    return bif.apply(key, query);
//                }
//            };
//        }
//    }
//    
//    private static class NOP extends InternalKey<Object> {
//        public NOP(Object... id) {
//            super(id);
//        }
//        @Override
//        public QueryPart<? super Object> addTo(Object key, InternalQueryComposer<? extends Object> query) {
//            return null;
//        }
//    }
//    
//    protected static class InternalBuilder {
//        
//        private final Object[] id;
//
//        public InternalBuilder(Object[] id) {
//            this.id = id;
//        }
//        
//        public <Builder> InternalKey<Builder> add(QueryPart<? super Builder> qp) {
//            return new NOP(id).andAdd(qp);
//        }
//        
//        public <Builder> InternalKey<Builder> add(Supplier<? extends QueryPart<? super Builder>> supplier) {
//            return new NOP(id).andAdd(supplier);
//        }
//        
//        public <Builder> InternalKey<Builder> compose(BiFunction<Object, InternalQueryComposer<? extends Builder>, QueryPart> bif) {
//            return new NOP(id).andDo(bif);
//        }
//    }
}
