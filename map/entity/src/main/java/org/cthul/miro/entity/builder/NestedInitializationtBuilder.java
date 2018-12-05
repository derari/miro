package org.cthul.miro.entity.builder;

import org.cthul.miro.entity.InitializationBuilder;
import org.cthul.miro.util.*;

/**
 *
 */
public class NestedInitializationtBuilder<Entity> extends NestedInitializationtBuilderBase<Entity, InitializationBuilder<Entity>> {
    
    public NestedInitializationtBuilder(CompletableBuilder completableBuilder) {
        super(completableBuilder);
    }
}
