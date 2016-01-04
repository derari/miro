package org.cthul.miro.composer;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.composer.template.InternalQueryComposer;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Builder>
 */
public class AbstractQueryComposer<Builder> extends AbstractCache<Object, Object> implements QueryComposer {
    
    private final Template<? super Builder> template;
    private final PartList<Builder> parts = new PartList<>();
    private final InternalQueryComposer<Builder> iqc = new InternalQueryComposer<Builder>() {
        @Override
        public void addPart(QueryPart<? super Builder> part) {
            internalAddPart(part);
        }
        @Override
        public <V> void addNode(Key<V> key, V node) {
            internalAddNode(key, node);
        }
        @Override
        public void require(Object key) {
            AbstractQueryComposer.this.require(key);
        }
        @Override
        public <V> V part(Key<V> key) {
            return AbstractQueryComposer.this.part(key);
        }
    };

    public AbstractQueryComposer(Template<? super Builder> template) {
        this.template = template;
    }
    
    protected void internalAddPart(QueryPart<? super Builder> part) {
        parts.add(part);
    }

    protected <V> void internalAddNode(Key<V> key, V node) {
        tryPut(key, node);
    }

    protected void buildStatement(Builder builder) {
        parts.buildStatement(builder);
    }

    @Override
    protected Object tryPut(Object key, Object value) {
        Object actual = super.tryPut(key, value);
        if (actual != value) {
            throw new IllegalStateException(
                    "part duplication: " + actual + ", " + value);
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
    public <V> V part(Key<V> key) {
        Object v = getValue(key);
        return key.cast(v);
    }
    
    private static class PartList<Builder> {
        private final List<QueryPart<? super Builder>> parts = new ArrayList<>();

        public void add(QueryPart<? super Builder> part) {
            parts.add(part);
        }
        
        public void buildStatement(Builder builder) {
            parts.forEach(p -> p.addTo(builder));
        }
    }
}
