package org.cthul.miro.entity.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.util.Closeables;
import org.cthul.miro.util.CompleteAndClose;
import org.cthul.miro.util.XConsumer;
import static org.cthul.miro.util.CompleteAndClose.blank;

/**
 *
 */
final class CompositeInitializer<Entity> implements EntityInitializer<Entity> {
    
    final List<XConsumer<? super Entity, ?>> initializers;
    final CompleteAndClose completeAndClose;

    public CompositeInitializer(List<XConsumer<? super Entity, ?>> consumers, CompleteAndClose completeAndClose) {
        this.initializers = blank(consumers) ? Collections.emptyList() : new ArrayList<>(consumers);
        this.completeAndClose = completeAndClose;
    }

    @Override
    public void apply(Entity entity) throws MiException {
        try {
            for (XConsumer<? super Entity, ?> c : initializers) {
                c.accept(entity);
            }
        } catch (Throwable e) {
            throw Closeables.exceptionAs(e, MiException.class);
        }
    }

    @Override
    public void complete() throws MiException {
        try {
            completeAndClose.complete();
        } catch (Exception e) {
            throw Closeables.exceptionAs(e, MiException.class);
        }
    }

    @Override
    public void close() throws MiException {
        try {
            completeAndClose.close();
        } catch (Exception e) {
            throw Closeables.exceptionAs(e, MiException.class);
        }
    }

    @Override
    public String toString() {
        return completeAndClose.toString();
    }
    
}
