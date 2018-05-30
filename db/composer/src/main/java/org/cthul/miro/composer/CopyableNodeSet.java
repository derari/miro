package org.cthul.miro.composer;

import org.cthul.miro.composer.node.StatementPart;
import org.cthul.miro.composer.node.Copyable;
import org.cthul.miro.composer.node.Initializable;
import java.util.*;
import java.util.function.Consumer;

/**
 *
 * @param <Key>
 * @param <Hint>
 * @param <Value>
 */
public abstract class CopyableNodeSet<Key, Hint, Value> {
    
    private Nodes<Key, Hint, Value> nodes;

    public CopyableNodeSet() {
        this.nodes = new Nodes<>(this);
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    public CopyableNodeSet(CopyableNodeSet<Key, Hint, Value> parent) {
        this.nodes = parent.copyParts(this);
    }
    
    protected Value peekValue(Key key) {
        Value v = nodes.nodes.get(key);
        if (v == NULL_NODE) return null;
        return v;
    }
    
    protected Value getValue(Key key, Hint hint) {
        Value v = nodes.get(key, hint);
        if (v == NULL_NODE) return null;
        return v;
    }
    
    protected void putValue(Key key, Value value) {
        nodes.putValue(key, value);
    }
    
    protected void putNode(Key key, Value value) {
        nodes.putNode(key, value);
    }
    
    protected void putNullNode(Key key) {
        nodes.putNode(key, (Value) NULL_NODE);
    }
    
    protected void putNullValue(Key key) {
        nodes.putValue(key, (Value) NULL_NODE);
    }
    
    protected void addPartsTo(Object builder) {
        nodes.addTo(builder);
    }

    protected abstract void newEntry(Key key, Hint hint);
    
    protected abstract Object getInitializationArg();
    

    private Nodes<Key, Hint, Value> copyParts(CopyableNodeSet<Key, Hint, Value> newOwner) {
        nodes = nodes.freezeChanges();
        return nodes.copy(newOwner);
    }
    
    private static class Nodes<Key, Hint, Value> {
        
        private CopyableNodeSet<Key, Hint, Value> owner;
        private final Nodes<Key, Hint, Value> parent;
        private final Map<Key, Value> values = new HashMap<>();
        private final Map<Key, Value> nodes = new HashMap<>();
        private List<Key> partKeys = null;
        private boolean modified = false;

        public Nodes(CopyableNodeSet<Key, Hint, Value> owner) {
            this(owner, null);
        }

        public Nodes(CopyableNodeSet<Key, Hint, Value> owner, Nodes parent) {
            this.owner = owner;
            this.parent = parent;
        }
        
        public Nodes freezeChanges() {
            if (!modified) return this;
            Nodes next = new Nodes(owner, this);
            owner = null;
            return next;
        }
        
        public boolean isFrozen() {
            return owner == null;
        }
        
        public Nodes copy(CopyableNodeSet<Key, Hint, Value> newOwner) {
            if (modified) {
                throw new IllegalStateException("Freeze changes first");
            }
            return new Nodes(newOwner, parent);
        }
        
        public void putValue(Key key, Value value) {
            if (isFrozen()) {
                throw new IllegalStateException("Frozen");
            }
            Value old = values.putIfAbsent(key, value);
            if (old != null && old != value) {
                throw new IllegalArgumentException("Duplicate key: " + key);
            }
            modified = true;
        }
        
        public void putNode(Key key, Value value) {
            putValue(key, value);
            nodes.put(key, value);
            if (value instanceof Initializable) {
                ((Initializable) value).initialize(owner.getInitializationArg());
            }
            if (value instanceof StatementPart) {
                if (partKeys == null) {
                    partKeys = new ArrayList<>();
                }
                partKeys.add(key);
            }
        }
        
        public Value get(Key key, Hint hint) {
            Value value = getOrInherit(key);
            if (value != null) return value;
            owner.newEntry(key, hint);
            value = values.get(key);
            if (value != null) return value;
            throw new NoSuchElementException(String.valueOf(key));
        }
        
        public Value getOrInherit(Key key) {
            return getOrInherit(key, false);
        }
        
        public Value getOrInherit(Key key, boolean readOnly) {
            if (isFrozen()) {
                throw new IllegalStateException("Frozen");
            }
            Value value = values.get(key);
            if (value != null) return value;
            for (Nodes<Key, Hint, Value> current = parent; current != null; current = current.parent) {
                value = current.nodes.get(key);
                if (value != null) {
                    if (value instanceof Copyable) {
                        Copyable<Object> cpy = (Copyable) value;
                        if (readOnly && cpy.allowReadOriginal()) return value;
                        value = (Value) cpy.copy(owner.getInitializationArg());
                    }
                    nodes.put(key, value);
                    values.put(key, value);
                    modified = true;
                    return value;
                }
            }
            return null;
        }

        public void addTo(Object builder) {
            Set<Key> guard = new HashSet<>();
            Consumer<Key> addToBuilder = key -> {
                if (guard.add(key)) {
                    ((StatementPart) getOrInherit(key, true)).addTo(builder);
                }
            };
            collectPartKeys(addToBuilder); 
       }
        
        private void collectPartKeys(Consumer<Key> bag) {
            if (parent != null) parent.collectPartKeys(bag);
            if (partKeys != null) partKeys.forEach(bag);
        }
    }
    
    private static final Object NULL_NODE = new Copyable<Object>() {
        @Override
        public Object copy(Object composer) {
            return this;
        }
        @Override
        public boolean allowReadOriginal() {
            return true;
        }
    };
}
