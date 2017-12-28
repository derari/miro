package org.cthul.miro.request.impl;

import org.cthul.miro.util.AbstractCache;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Builder>
 */
public abstract class AbstractTemplate<Builder> extends AbstractCache<Object, Template<? super Builder>> implements Template<Builder> {

    private final Template<? super Builder> parent;
    private final AbstractTemplate<? super Builder> parentLookUp;

//    public AbstractTemplate() {
//        this(NO_PARENT);
//    }
//
    public AbstractTemplate(Template<? super Builder> parent) {
        this.parent = parent;
        this.parentLookUp = getParentLookUp(parent);
    }
    
    protected static <Builder> AbstractTemplate<? super Builder> getParentLookUp(Template<? super Builder> parent) {
        if (parent instanceof AbstractTemplate) {
            try {
                Class<?> decl = parent.getClass()
                        .getMethod("addTo", Key.class, InternalComposer.class)
                        .getDeclaringClass();
                if (decl == AbstractTemplate.class) {
                    return (AbstractTemplate<Builder>) parent;
                }
            } catch (SecurityException e) {
                // unclear if addTo is overridden, return null to be safe
            } catch (NoSuchMethodException e) {
                throw new AssertionError("Method AbstractTemplate#addTo(Key, InternalComposer> should exist", e);
            }
        }        
        return null;
    }

    @Override
    public void addTo(Key<?> key, InternalComposer<? extends Builder> query) {
        getValue(key).addTo(key, query);
    }

    @Override
    protected Template<? super Builder> create(Object key) {
        Template<? super Builder> t = createPartTemplate(key);
        if (t != null) return t;
        if (parentLookUp != null) {
            return parentLookUp.getValue(key);
        }
        return parent;
    }
    
    protected void forceParentLookUp(Object key) {
        if (parentLookUp != null) {
            forcePut(key, parentLookUp.getValue(key));
        } else {
            forcePut(key, parent);
        }
    }
    
    protected abstract Template<? super Builder> createPartTemplate(Object key);
    
    protected static final Template<Object> NO_PARENT = new Template<Object>() {
        @Override
        public void addTo(Key<?> key, InternalComposer<? extends Object> composer) {
            throw new IllegalArgumentException("Unknown key: " + key);
        }
        @Override
        public String toString() {
            return "NO_PARENT";
        }
    };
    
    public static Template<Object> noTemplate() {
        return NO_PARENT;
    }
    
//    protected <V> KeyTemplate<? super Builder, V> parentPart(Key<V> key) {
//        return Templates.redirect(key, parent);
//    }
    
    protected Templates.ComposableTemplate<? super Builder> parentPart() {
        return Templates.compose(parent);
    }

    @Override
    public String toString() {
        if (parent != Templates.noOp()) {
            if (parent instanceof AdaptedTemplate.ParentAdapter) {
                return getShortString() + parent;
            } else {
                return getShortString() + ">" + parent;
            }
        } else {
            return getShortString();
        }
    }

    protected String getShortString() {
        return super.toString();
    }
}
