package org.cthul.miro.entity.builder;

import org.cthul.miro.entity.EntitySelector;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.util.*;

/**
 *
 * @param <Entity>
 */
public class NestedSelectorBuilder<Entity> extends NestedInitializationtBuilderBase<Entity, SelectorBuilder<Entity>> implements SelectorBuilder<Entity> {
    
    private XFunction<Object[], ? extends Entity, ?> selector = null;
    private Object selectorName = null;
    
    private boolean singleSelectorMode = true;
    private EntitySelector<? extends Entity> singleSelector = null;

    public NestedSelectorBuilder(CompletableBuilder completableBuilder) {
        super(completableBuilder);
    }
    
    private void disableSingleSelectorMode() {
        if (!singleSelectorMode) return;
        singleSelectorMode = false;
        if (singleSelector == null) return;
        setSelector(singleSelector::get);
        singleSelector = null;
    }

    @Override
    public <E extends Entity> SelectorBuilder<E> set(EntitySelector<? extends E> selector) {
        if (singleSelectorMode) {
            if (singleSelector == null) {
                singleSelector = selector;
                super.addCompletable(selector);
                super.addCloseable(selector);
                return (SelectorBuilder) this;
            } else {
                disableSingleSelectorMode();
            }
        }
        if (selector instanceof CompositeSelector) {
            CompositeSelector<E> cf = (CompositeSelector<E>) selector;
            if (cf.selectorName != null) {
                addName(selectorName);
            }
            return setSelector(cf.selector).add(cf.setup);
        } else {
            addName(selector);
            addCompleteAndClose(selector);
            return setSelector(selector::get);
        }
    }

    @Override
    public <E extends Entity> SelectorBuilder<E> setSelector(XFunction<Object[], ? extends E, ?> selector) {
        disableSingleSelectorMode();
        if (this.selector != null) {
            throw new IllegalStateException("Selector already set");
        }
        this.selector = selector;
        return (SelectorBuilder) this;
    }

    @Override
    public SelectorBuilder<Entity> addName(Object name) {
        disableSingleSelectorMode();
        if (selectorName == null) {
            selectorName = name;
            return this;
        } else {
            return super.addName(name);
        }
    }

    @Override
    public SelectorBuilder<Entity> add(EntityInitializer<? super Entity> initializer) {
        disableSingleSelectorMode();
        return super.add(initializer);
    }

    @Override
    public SelectorBuilder<Entity> addInitializer(XConsumer<? super Entity, ?> initializer) {
        disableSingleSelectorMode();
        if (selectorName != null && selector == null) {
            super.addName(selectorName);
            selectorName = null;
        }
        return super.addInitializer(initializer);
    }

    @Override
    public SelectorBuilder<Entity> addCompletable(Completable completable) {
        disableSingleSelectorMode();
        return super.addCompletable(completable);
    }

    @Override
    public SelectorBuilder<Entity> addCloseable(AutoCloseable closeable) {
        disableSingleSelectorMode();
        return super.addCloseable(closeable);
    }
    
    public EntitySelector<Entity> buildSelector() {
        if (singleSelectorMode) {
            return (EntitySelector) singleSelector;
        }
        if (selector == null) {
            throw new IllegalStateException("selector required");
        }
        return new CompositeSelector<>(selector, selectorName, buildInitializer());
    }

    @Override
    public String toString() {
        return (selectorName != null ? selectorName : "?") + " with " + super.toString();
    }
    
}
