package org.cthul.miro.request.impl;

import org.cthul.miro.request.part.CopyManager;
import org.cthul.miro.util.AbstractCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.ComposerKey;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.template.Template;
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
        this.frozenParts = source.partsForCopy();
    }
    
    /** @return immutable snapshot of current state */
    private synchronized PartList<Builder> partsForCopy() {
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
    public boolean include(Object key) {
        return parts().getValue(key) != null;
    }

    @Override
    public <V> V get(Key<V> key) {
        Object v = parts().getValue(key);
        if (v == NO_NODE) return null;
        return key.cast(v);
    }

    protected void initialize() {
        optional(ComposerKey.PHASE, p -> p.enter(ComposerKey.Phase.COMPOSE));
        parts().getValue(ComposerKey.ALWAYS);
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
        public boolean include(Object key) {
            return AbstractComposer.this.include(key);
        }
        @Override
        public <V> V get(Key<V> key) {
            return AbstractComposer.this.get(key);
        }
    }
    
    /**
     * Contains the parts of a composer.
     * Can have a chain of parent lists whose entries are copied on demand.
     * Once a part list becomes a parent, it should be immutable.
     * @param <Builder> 
     */
    protected static class PartList<Builder> extends AbstractCache<Object, Object> implements CopyManager {
        private final PartList<Builder> parent;
        private AbstractComposer<Builder> owner;
        private List<StatementPart<? super Builder>> myParts = null;
        private Collection<StatementPart<? super Builder>> readOnlyParts = null;
        private Map<Object, Object> copyMap = null;
        private long accessCount = 0;
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
            readOnlyParts = null;
            myParts.add(part);
            accessCount++;
        }
        
        public <V> void addNode(Key<V> key, V node) {
            checkActive();
            readOnlyParts = null;
            tryPut(key, node);
            accessCount++;
        }
        
        /**
         * Returns all statement parts.
         * Fetches parts from all parents.
         * @return all parts
         */
        private Iterable<StatementPart<? super Builder>> allParts() {
            if (readOnlyParts != null) {
                return readOnlyParts;
            }
            Set<StatementPart<? super Builder>> bag = new LinkedHashSet<>();
            readParentParts(bag);
            if (myParts != null) bag.addAll(myParts);
            return readOnlyParts = bag;
        }
        
        protected void readParentParts(Collection<StatementPart<? super Builder>> bag) {
            parent.collectParts(part -> {
                StatementPart<? super Builder> copy = tryCopyReadOnly(part);
                if (copy != null) bag.add(copy);
            });
        }
        
        /**
         * Collects all known parts.
         * @param bag 
         */
        protected void collectParts(Consumer<StatementPart<? super Builder>> bag) {
            if (readOnlyParts != null) {
                readOnlyParts.forEach(bag);
            } else {
                parent.collectParts(bag);
                if (myParts != null) {
                    myParts.forEach(bag);
                }
            }
        }
        
        public void forEachPart(Consumer<StatementPart<? super Builder>> action) {
            allParts().forEach(action);
        }

        @Override
        @SuppressWarnings("Convert2Lambda")
        protected Object create(Object key) {
            checkActive();
            if (key == CopyManager.key) return this;
            Object v = parent.hierarchyPeek(key);
            if (v != null) return tryCopy(v);
            long before = accessCount;
            owner.create(key);
            v = peekValue(key);
            if (v == null && before < accessCount) {
                v = NO_NODE;
            }
            return v;
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

        @Override
        public <V> V tryCopy(V value) {
            return copyValue(value, false);
        }
        
        protected <V> V tryCopyReadOnly(V value) {
            return copyValue(value, true);
        }

        protected <V> V copyValue(V value, boolean readOnly) {
            if (copyMap == null) {
                copyMap = new HashMap<>();
            }
            value = parent.hierarchyCopyPeek(value);
            V copy = (V) copyMap.get(value);
            if (copy != null) {
                return copy;
            }
            if (readOnly && Copyable.allowReadOnly(value, this::isLatest)) {
                return value;
            }
            copy = Copyable.tryCopy(value, owner.internal);
            if (copy != null) copyMap.putIfAbsent(value, copy);
            return (V) copyMap.get(value);
        }
        
        protected boolean isLatest(Object o) {
            return o == tryCopyReadOnly(o);
        }
        
        @Override
        protected Object tryPut(Object key, Object value) {
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
            accessCount++;
            return super.getValue(key);
        }
    }
    
    private static final Object NO_NODE = new Object();
    
    private static final PartList NO_PARENT = new PartList() {
        @Override
        protected Object hierarchyPeek(Object key) { return null; }
        @Override
        protected Object hierarchyCopyPeek(Object value) { return value; }
        @Override
        public void forEachPart(Consumer action) { }
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
