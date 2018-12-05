package org.cthul.miro.entity.builder;

import org.cthul.miro.util.CompleteAndClose;

/**
 *
 */
public class BasicFactoryBuilder<Entity> extends NestedFactoryBuilder<Entity> {
    
    private final CompleteAndClose.Builder<?> ccBuilder;

    public BasicFactoryBuilder() {
        this(new CompleteAndClose.Builder<>());
    }

    private BasicFactoryBuilder(CompleteAndClose.Builder<?> ccBuilder) {
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
