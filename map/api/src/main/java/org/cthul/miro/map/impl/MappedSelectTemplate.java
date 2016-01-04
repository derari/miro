package org.cthul.miro.map.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.cthul.miro.composer.ComposerKey;
import static org.cthul.miro.composer.ComposerParts.*;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.map.MappingKey;

/**
 *
 */
public class MappedSelectTemplate<Entity, RS> extends AbstractMappedTemplate<Entity, RS> {

    public MappedSelectTemplate(MappingTemplates<Entity> owner, Template<? super MappedStatementBuilder<Entity, RS>> parent) {
        super(owner, parent);
    }

    @Override
    protected String getShortString() {
        return "Map-Select";
    }
    
    @Override
    protected Template<? super MappedStatementBuilder<Entity, RS>> createPartType(Object key) {
        switch (MappingKey.key(key)) {
            case LOAD_FIELD:
                return link(ComposerKey.RESULT);
            case SET_FIELD:
                return newNodePart(SetAttribute::new);
        }
        switch (ComposerKey.key(key)) {
            case RESULT:
                return parentPartType(ComposerKey.RESULT)
                        .andNewNodePart(ResultAttributes::new);
        }
        return super.createPartType(key);
    }

    protected class ResultAttributes implements ComposerKey.ResultAttributes,
                                    StatementPart<MappedStatementBuilder<Entity, RS>> {

        private final LinkedHashSet<String> attributes = new LinkedHashSet<>();
        
        @Override
        public void addTo(MappedStatementBuilder<Entity, RS> builder) {
            builder.configureWith(getOwner().attributeConfiguration(attributes));
        }

        @Override
        public void add(String attribute) {
            attributes.add(attribute);
        }
    }
    
    protected class SetAttribute implements MappingKey.SetAttribute, 
                        EntityInitializer<Entity>,
                        StatementPart<MappedStatementBuilder<Entity, ?>> {

        final List<Consumer<Entity>> setUps = new ArrayList<>();
        
        @Override
        public void set(String key, Supplier<?> value) {
            BiConsumer<Entity, Object> setter = getOwner().getSetters().get(key);
            setUps.add(e -> setter.accept(e, value.get()));
        }

        @Override
        public void addTo(MappedStatementBuilder<Entity, ?> builder) {
            builder.initializeWith(this);
        }

        @Override
        public void apply(Entity entity) throws MiException {
            setUps.forEach(s -> s.accept(entity));
        }

        @Override
        public void complete() throws MiException { }
    }
}
