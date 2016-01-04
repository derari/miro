package org.cthul.miro.composer.impl;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.composer.Composer;
import org.cthul.miro.composer.ComposerKey;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.Template;
import org.cthul.miro.util.Key;

/**
 * Implements look-up of parts in a template.
 * @param <Builder>
 */
public class AbstractComposer<Builder> extends AbstractCache<Object, Object> implements Composer {
    
    private final Template<? super Builder> template;
    private final PartList<Builder> parts = new PartList<>();
    private final InternalComposer<Builder> iqc = new InternalComposer<Builder>() {
        @Override
        public void addPart(StatementPart<? super Builder> part) {
            internalAddPart(part);
        }
        @Override
        public <V> void addNode(Key<V> key, V node) {
            internalAddNode(key, node);
        }
        @Override
        public void require(Object key) {
            AbstractComposer.this.require(key);
        }
        @Override
        public <V> V node(Key<V> key) {
            return AbstractComposer.this.node(key);
        }
    };
    
    private boolean first = true;

    public AbstractComposer(Template<? super Builder> template) {
        this.template = template;
    }
    
    protected void internalAddPart(StatementPart<? super Builder> part) {
        parts.add(part);
    }

    protected <V> void internalAddNode(Key<V> key, V node) {
        tryPut(key, node);
    }

    protected void buildStatement(Builder builder) {
        optional(ComposerKey.PHASE, p -> p.enter(ComposerKey.Phase.BUILD));
        parts.buildStatement(builder);
    }

    @Override
    protected Object tryPut(Object key, Object value) {
        Object actual = super.tryPut(key, value);
        if (actual != value) {
            throw new IllegalStateException(
                    "node duplication: " + actual + ", " + value);
        }
        return actual;
    }
    
    @Override
    protected Object create(Object key) {
        template.addTo(key, iqc);
        return peekValue(key);
    }

    @Override
    public void require(Object key) {
        getValue(key);
    }

    @Override
    public <V> V node(Key<V> key) {
        Object v = getValue(key);
        return key.cast(v);
    }

    @Override
    protected Object getValue(Object key) {
        if (first) {
            first = false;
            initialize();
        }
        return super.getValue(key);
    }
    
    protected void initialize() {
        optional(ComposerKey.PHASE, p -> p.enter(ComposerKey.Phase.COMPOSE));
    }
    
    private static class PartList<Builder> {
        private final List<StatementPart<? super Builder>> parts = new ArrayList<>();

        public void add(StatementPart<? super Builder> part) {
            parts.add(part);
        }
        
        public void buildStatement(Builder builder) {
            parts.forEach(p -> p.addTo(builder));
        }
    }
}
