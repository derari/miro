package org.cthul.miro.entity.builder;

import org.cthul.miro.util.CompleteAndClose;

/**
 *
 * @param <Entity>
 */
public class BasicSelectorBuilder<Entity> extends NestedSelectorBuilder<Entity> {
    
    private final CompleteAndClose.Builder<?> ccBuilder;

    public BasicSelectorBuilder() {
        this(new CompleteAndClose.Builder<>());
    }

    private BasicSelectorBuilder(CompleteAndClose.Builder<?> ccBuilder) {
        super(ccBuilder);
        this.ccBuilder = ccBuilder;
    }

    @Override
    protected void addNestedName(Object name) {
        ccBuilder.addName(name);
    }

    @Override
    public CompleteAndClose buildCompleteAndClose() {
        return ccBuilder.buildCompleteAndClose();
    }
}
