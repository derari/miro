package org.cthul.miro.composer.impl;

import org.cthul.miro.util.AbstractCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.cthul.miro.composer.Composer;
import org.cthul.miro.composer.ComposerKey;
import org.cthul.miro.composer.Copyable;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.util.Key;

/**
 * Implements look-up of parts in a template.
 * @param <Builder>
 */
public abstract class AbstractComposer<Builder> implements Composer {
    
    private final InternalComposer<Builder> internal = new Internal();
    private final Template<? super Builder> template;
    private PartList<Builder> frozenParts;
    private PartList<Builder> parts = null;
    
    public AbstractComposer(Template<? super Builder> template) {
        if (template == null) throw new NullPointerException("template");
        this.template = template;
        this.frozenParts = NO_PARENT;
    }

    /** Copy constructor
     * @param source */
    @SuppressWarnings("LeakingThisInConstructor")
    protected AbstractComposer(AbstractComposer<Builder> source) {
        this.template = source.template;
        this.frozenParts = source.partListForCopy();
    }
    
    /** @return immutable snapshot of current state */
    private synchronized PartList<Builder> partListForCopy() {
        if (parts != null && parts.isModified()) {
            frozenParts = parts.freeze();
            parts = null;
        }
        return frozenParts;
    }
    
    protected PartList<Builder> parts() {
        if (parts == null) {
            parts = frozenParts.copy(this);
        }
        return parts;
    }

    protected void build(Builder builder) {
        optional(ComposerKey.PHASE, p -> p.enter(ComposerKey.Phase.BUILD));
        parts().forEachPart(p -> p.addTo(builder));
    }
    
    protected void create(Object key) {
        template.addTo(key, internal);
    }

    @Override
    public void require(Object key) {
        parts().getValue(key);
    }

    @Override
    public <V> V node(Key<V> key) {
        Object v = parts().getValue(key);
        return key.cast(v);
    }

    protected void initialize() {
        optional(ComposerKey.PHASE, p -> p.enter(ComposerKey.Phase.COMPOSE));
        require(ComposerKey.ALWAYS);
    }
    
    private class Internal implements InternalComposer<Builder> {
        @Override
        public void addPart(StatementPart<? super Builder> part) {
            parts().addPart(part);
        }
        @Override
        public <V> void addNode(Key<V> key, V node) {
            parts().addNode(key, node);
        }
        @Override
        public void require(Object key) {
            AbstractComposer.this.require(key);
        }
        @Override
        public <V> V node(Key<V> key) {
            return AbstractComposer.this.node(key);
        }
    }
    
    /**
     * Contains the parts of a composer.
     * Can have a chain of parent lists whose entries are copied on demand.
     * Once a part list becomes a parent, it should be immutable.
     * @param <Builder> 
     */
    protected static class PartList<Builder> extends AbstractCache<Object, Object> {
        private AbstractComposer<Builder> owner;
        private final PartList<Builder> parent;
        private boolean collectedParentParts = false;
        private List<StatementPart<? super Builder>> myParts = null;
        private Map<Object, Object> copyMap = null;
        private boolean initialized;

        protected PartList() {
            this.owner = null;
            this.parent = null;
            this.initialized = false;
        }

        protected PartList(AbstractComposer<Builder> owner, PartList<Builder> parent) {
            this.owner = owner;
            this.parent = parent;
            this.initialized = parent.initialized;
        }
        
        public boolean isModified() {
            return !emptyCache() || myParts != null || copyMap != null;
        }
        
        public PartList<Builder> freeze() {
            owner = null;
            return this;
        }
        
        public PartList<Builder> copy(AbstractComposer<Builder> newOwner) {
            if (owner != null) {
                throw new IllegalStateException("part list not frozen");
            }
            return new PartList<>(newOwner, this);
        }
        
        private void checkActive() {
            if (owner == null) {
                throw new IllegalStateException("frozen");
            }
        }
        
        public void addPart(StatementPart<? super Builder> part) {
            checkActive();
            if (myParts == null) {
                myParts = new ArrayList<>();
            }
            myParts.add(part);
        }
        
        public <V> void addNode(Key<V> key, V node) {
            tryPut(key, node);
        }
        
        /**
         * Returns all statement parts.
         * Fetches parts from all parents.
         * @return all parts
         */
        private Iterable<StatementPart<? super Builder>> allParts() {
            if (collectedParentParts) {
                return myParts;
            }
            Set<StatementPart<? super Builder>> bag = new LinkedHashSet<>();
            copyParentParts(bag);
            if (myParts != null) bag.addAll(myParts);
            return myParts = new ArrayList<>(bag);
        }
        
        protected void copyParentParts(Collection<StatementPart<? super Builder>> bag) {
            parent.collectParts(part -> {
                StatementPart<? super Builder> copy = copyValue(part);
                if (copy != null) bag.add(copy);
            });
            collectedParentParts = true;
        }
        
        /**
         * Collects all known parts.
         * @param bag 
         */
        protected void collectParts(Consumer<StatementPart<? super Builder>> bag) {
            if (!collectedParentParts) {
//                if (copyMap == null) {
                    parent.collectParts(bag);
//                } else {
//                    parent.collectParts(p -> {
//                        Object copy = copyMap.getOrDefault(p, p);
//                        bag.accept((StatementPart<Builder>) copy);
//                    });
//                }
            }
            if (myParts != null) {
                // `myParts` only contains copies from parents 
                // if `collectedParentParts` is true
                myParts.forEach(bag);
            }
        }
        
        public void forEachPart(Consumer<StatementPart<? super Builder>> action) {
            allParts().forEach(action);
        }

        @Override
        @SuppressWarnings("Convert2Lambda")
        protected Object create(Object key) {
            if (key == CopyManager.key) {
                return new CopyManager() {
                    @Override
                    public <V> V tryCopy(V value) {
                        return copyValue(value);
                    }
                };
            }
            Object v = parent.hierarchyPeek(key);
            if (v != null) return copyValue(v);
            owner.create(key);
            return peekValue(key);
        }
        
        protected Object hierarchyPeek(Object key) {
            Object v = peekValue(key);
            if (v != null) return v;
            return parent.hierarchyPeek(key);
        }
        
        protected <V> V hierarchyCopyPeek(V value) {
            if (copyMap == null) {
                return parent.hierarchyCopyPeek(value);
            }
            V copy = (V) copyMap.get(value);
            if (copy != null) return copy;
            copy = parent.hierarchyCopyPeek(value);
            if (value == copy) return value;
            return (V) copyMap.getOrDefault(copy, copy);
        }

        protected <V> V copyValue(V value) {
            if (copyMap == null) {
                copyMap = new HashMap<>();
            }
            value = parent.hierarchyCopyPeek(value);
            V copy = (V) copyMap.get(value);
            if (copy != null) {
                return copy;
            }
            copy = Copyable.tryCopy(value, owner.internal);
            if (copy != null) {
                copyMap.putIfAbsent(value, copy);
            }
            return (V) copyMap.get(value);
        }
        
        @Override
        protected Object tryPut(Object key, Object value) {
            checkActive();
            Object actual = super.tryPut(key, value);
            if (actual != null && actual != value) {
                throw new IllegalStateException(
                        "node duplication: " + actual + ", " + value);
            }
            return value;
        }
        
        @Override
        protected Object getValue(Object key) {
            checkActive();
            if (!initialized) {
                initialized = true;
                owner.initialize();
            }
            return super.getValue(key);
        }
    }
    
    private static final PartList NO_PARENT = new PartList() {
        @Override
        protected Object hierarchyPeek(Object key) { return null; }
        @Override
        protected Object hierarchyCopyPeek(Object value) { return value; }
        @Override
        protected void collectParts(Consumer bag) { }
        @Override
        protected Object tryPut(Object key, Object value) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean isModified() {
            throw new UnsupportedOperationException();
        }
    };
}
