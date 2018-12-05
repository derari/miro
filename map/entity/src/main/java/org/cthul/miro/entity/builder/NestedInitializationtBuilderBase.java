package org.cthul.miro.entity.builder;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.InitializationBuilder;
import org.cthul.miro.util.*;
import static org.cthul.miro.entity.Entities.noInitialization;
import static org.cthul.miro.util.CompleteAndClose.blank;

/**
 *
 */
public abstract class NestedInitializationtBuilderBase<Entity, This extends InitializationBuilder<Entity>> extends CompleteAndClose.NestedCompletableBuilder<InitializationBuilder<Entity>> implements InitializationBuilder<Entity> {
    
    private List<XConsumer<? super Entity, ?>> initializers = null;
    
    private boolean singleInitMode = true;
    private EntityInitializer<? super Entity> singleInit = null;

    public NestedInitializationtBuilderBase(CompletableBuilder completableBuilder) {
        super(completableBuilder);
    }

    private void disableSingleInitMode() {
        if (!singleInitMode) return;
        singleInitMode = false;
        if (singleInit == null) return;
        addInitializer(singleInit::apply);
        singleInit = null;
    }
    
    @Override
    public This add(EntityInitializer<? super Entity> initializer) {
        if (initializer == Entities.noInitialization()) return (This) this;
        if (singleInitMode) {
            if (singleInit == null) {
                singleInit = initializer;
                super.addCompletable(initializer);
                super.addCloseable(initializer);
                return (This) this;
            } else {
                disableSingleInitMode();
            }
        }
        if (initializer instanceof CompositeInitializer) {
            CompositeInitializer<Entity> ci = (CompositeInitializer) initializer;
            if (initializers == null) {
                initializers = new ArrayList<>();
            }
            initializers.addAll(ci.initializers);
            addName(ci.completeAndClose);
            addCompleteAndClose(ci.completeAndClose);
        } else {
            addInitializer(initializer::apply);
            addName(initializer);
            addCompleteAndClose(initializer);
        }
        return (This) this;
    }

    @Override
    public This addInitializer(XConsumer<? super Entity, ?> initializer) {
        disableSingleInitMode();
        if (initializers == null) {
            initializers = new ArrayList<>();
        }
        initializers.add(initializer);
        return (This) this;
    }

    @Override
    public This addCompletable(Completable completable) {
        disableSingleInitMode();
        if (completable instanceof CompositeInitializer) {
            completable = ((CompositeInitializer) completable).completeAndClose;
        }
        return (This) super.addCompletable(completable);
    }

    @Override
    public This addCloseable(AutoCloseable closeable) {
        disableSingleInitMode();
        if (closeable instanceof CompositeInitializer) {
            closeable = ((CompositeInitializer) closeable).completeAndClose;
        }
        return (This) super.addCloseable(closeable);
    }

    @Override
    public This addName(Object name) {
        disableSingleInitMode();
        if (name instanceof CompositeInitializer) {
            name = ((CompositeInitializer) name).completeAndClose;
        }
        return (This) super.addName(name);
    }
    
    protected boolean hasInitializers() {
        return singleInit != null || !blank(initializers);
    }

    public EntityInitializer<Entity> buildInitializer() {
        if (singleInitMode && singleInit != null) {
            return (EntityInitializer) singleInit;
        }
        CompleteAndClose cc = buildCompleteAndClose();
        if (cc == CompleteAndClose.NO_OP && blank(initializers)) {
            return noInitialization();
        }
        return new CompositeInitializer<>(initializers, cc);
    }
}
