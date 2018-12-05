package org.cthul.miro.entity.builder;

import org.cthul.miro.entity.InitializationBuilder;
import org.cthul.miro.util.CompleteAndClose;

/**
 *
 */
public class BasicInitializationBuilder<Entity> extends NestedInitializationtBuilder<Entity> {
    
    private final CompleteAndClose.Builder<?> ccBuilder;

    public BasicInitializationBuilder() {
        this(new CompleteAndClose.Builder<>());
    }

    private BasicInitializationBuilder(CompleteAndClose.Builder<?> ccBuilder) {
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
