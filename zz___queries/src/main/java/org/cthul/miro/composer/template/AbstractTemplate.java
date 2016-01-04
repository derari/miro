package org.cthul.miro.composer.template;

import org.cthul.miro.composer.AbstractCache;
import org.cthul.miro.composer.QueryParts;
import org.cthul.miro.composer.QueryParts.KeyTemplate;
import org.cthul.miro.util.Key;

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
    
    protected <V> KeyTemplate<? super Builder, V> superPartType(Key<V> key) {
        return QueryParts.redirect(key, parent);
    }
}
