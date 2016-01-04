package org.cthul.miro.composer.impl;

import org.cthul.miro.composer.ComposerParts;
import org.cthul.miro.composer.ComposerParts.KeyTemplate;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.Template;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Builder>
 */
public abstract class AbstractTemplate<Builder> extends AbstractCache<Object, Template<? super Builder>> implements Template<Builder> {

    private final Template<? super Builder> parent;
    private final AbstractTemplate<? super Builder> parentLookUp;

    public AbstractTemplate(Template<? super Builder> parent) {
        this.parent = parent;
        this.parentLookUp = getParentLookUp();
    }
    
    private AbstractTemplate<? super Builder> getParentLookUp() {
        if (parent instanceof AbstractTemplate) {
            try {
                Class<?> decl = parent.getClass()
                        .getMethod("addTo", Object.class, InternalComposer.class)
                        .getDeclaringClass();
                if (decl == AbstractTemplate.class) {
                    return (AbstractTemplate<Builder>) parent;
                }
            } catch (NoSuchMethodException e) {
                throw new AssertionError(null, e);
            }
        }        
        return null;
    }

    public AbstractTemplate() {
        this(NO_PARENT);
    }

    @Override
    public void addTo(Object key, InternalComposer<? extends Builder> query) {
        getValue(key).addTo(key, query);
    }

    @Override
    protected Template<? super Builder> create(Object key) {
//        if (key instanceof DirectTemplate) {
//            DirectTemplate<? super Builder> dt = (DirectTemplate) key;
//            return dt;
//        }
        Template<? super Builder> t = createPartType(key);
        if (t != null) return t;
        if (parentLookUp != null) {
            return parentLookUp.getValue(key);
        }
        return parent;
    }
    
    protected abstract Template<? super Builder> createPartType(Object key);
    
    protected static final Template<Object> NO_PARENT = (k, q) -> {
        throw new IllegalArgumentException(
                "Unknown key: " + k);
    };
    
    public static Template<Object> noTemplate() {
        return NO_PARENT;
    }
    
    protected <V> KeyTemplate<? super Builder, V> parentPartType(Key<V> key) {
        return ComposerParts.redirect(key, parent);
    }
    
    protected ComposerParts.ComposableTemplate<? super Builder> parentPartType() {
        return ComposerParts.compose(parent);
    }

    @Override
    public String toString() {
        if (parent != ComposerParts.noOp()) {
            return getShortString() + ">" + parent;
        } else {
            return getShortString();
        }
    }

    protected String getShortString() {
        return super.toString();
    }
}
