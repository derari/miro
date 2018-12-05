package org.cthul.miro.entity.builder;

import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntitySelector;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.util.Closeables;
import org.cthul.miro.util.XFunction;

/**
 *
 * @param <Entity>
 */
public class CompositeSelector<Entity> implements EntitySelector<Entity> {
    
    protected final XFunction<Object[], ? extends Entity, ?> selector;
    protected final Object selectorName;
    protected final EntityInitializer<? super Entity> setup;

    public CompositeSelector(XFunction<Object[], ? extends Entity, ?> selector, Object factoryName, EntityInitializer<? super Entity> setup) {
        this.selector = selector;
        this.selectorName = factoryName;
        this.setup = setup;
    }
    
    protected CompositeSelector(EntitySelector<Entity> source) {
        if (source instanceof CompositeSelector) {
            CompositeSelector<Entity> cs = (CompositeSelector) source;
            this.selector = cs.selector;
            this.selectorName = cs.selectorName;
            this.setup = cs.setup;
        } else {
            this.selector = source::get;
            this.selectorName = source;
            this.setup = Entities.buildInitializer(b -> b.addCompleteAndClose(source));
        }
    }

    @Override
    public final Entity get(Object... key) throws MiException {
        Entity n;
        try {
            n = selector.apply(key);
        } catch (Throwable t) {
            throw Closeables.exceptionAs(t, MiException.class);
        }
        setup.apply(n);
        return n;
    }

    @Override
    public final void complete() throws MiException {
        setup.complete();
    }

    @Override
    public final void close() throws MiException {
        setup.close();
    }

    @Override
    public String toString() {
        if (setup == Entities.noInitialization()) {
            return String.valueOf(selectorName);
        }
        return String.valueOf(selectorName) + " with " + setup.toString();
    }
}
