package org.cthul.miro.map;

import org.cthul.miro.map.node.MappedQueryNodeFactory;
import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.composer.node.ListNode;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.impl.AbstractTypeBuilder;
import org.cthul.miro.composer.ComposerState;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class MappedType<Entity, This extends MappedType<Entity, This>> extends AbstractTypeBuilder<Entity, This> {
    
    private MappedQueryComposer queryComposerPrototype = null;

    public MappedType(Class<Entity> clazz) {
        super(clazz);
    }

    public MappedType(Class<Entity> clazz, Object shortString) {
        super(clazz, shortString);
    }
    
    public MappedQueryComposer newMappedQueryComposer() {
        if (queryComposerPrototype == null) {
            queryComposerPrototype = new MappedQueryNodeFactory(this, entityClass(), asPlainEntityType()).newComposer();
        }
        return ComposerState.copy(queryComposerPrototype);
    }
}
