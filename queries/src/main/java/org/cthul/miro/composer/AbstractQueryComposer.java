package org.cthul.miro.composer;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.composer.template.InternalQueryComposer;
import org.cthul.miro.composer.template.Template;

/**
 *
 * @param <Builder>
 */
public class AbstractQueryComposer<Builder> extends AbstractCache<Object, QueryPart<? super Builder>> implements QueryComposer {
    
    private final Template<? super Builder> template;
    private final List<QueryPart<? super Builder>> parts = new ArrayList<>();
    private final InternalQueryComposer<Builder> iqc = new InternalQueryComposer<Builder>() {
        @Override
        public void addPart(Object key, QueryPart<? super Builder> part) {
            internalPut(key, part);
        }
        @Override
        public void put2(Object key, Object key2, Object... args) {
            AbstractQueryComposer.this.put2(key, key2, args);
        }
    };

    public AbstractQueryComposer(Template<? super Builder> template) {
        this.template = template;
    }

    @Override
    protected synchronized QueryPart<? super Builder> putNew(Object key) {
        QueryPart<? super Builder> newPart = create(key);
        internalPut(key, newPart);
        return newPart;
    }

    private void internalPut(Object key, QueryPart<? super Builder> newPart) throws IllegalStateException {
        QueryPart<? super Builder> actual = tryPut(key, newPart);
        if (actual != newPart) {
            throw new IllegalStateException(
                    "part duplication: " + actual + ", " + newPart);
        }
        parts.add(newPart);
    }

    protected List<QueryPart<? super Builder>> getParts() {
        return parts;
    }

    @Override
    protected QueryPart<? super Builder> create(Object key) {
        template.addTo(key, iqc);
        return getValue(key);
    }

    @Override
    public void put2(Object key, Object key2, Object... args) {
        getValue(key).put(key2, args);
    }
    
    protected void buildStatement(Builder builder) {
        getParts().forEach(part -> {
            part.addTo(builder);
        });
    }
}
