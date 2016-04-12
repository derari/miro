package org.cthul.miro.map.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.cthul.miro.composer.ComposerKey;
import org.cthul.miro.composer.Copyable;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.composer.ListNode;
import static org.cthul.miro.composer.template.Templates.*;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.Mapping;

/**
 * @param <Entity>
 */
public class MaterializationLayer<Entity> extends AbstractMappingLayer<Entity> {

    public MaterializationLayer(MappedTemplates<Entity> owner) {
        super(owner);
    }

    @Override
    protected String getShortString() {
        return "Materialize";
    }

    @Override
    protected Template<? super Mapping<Entity>> createPartTemplate(Parent<Mapping<Entity>> parent, Object key) {
        switch (ComposerKey.key(key)) {
            case RESULT:
                return parent.andLink(MappingKey.LOAD_FIELD);
        }
        switch (MappingKey.key(key)) {
            case LOAD_ALL:
                return setUp(MappingKey.LOAD_FIELD, lf -> ((LoadField) lf).loadAll());
            case LOAD_FIELD:
                return newNodePart(LoadField::new);
            case SET_FIELD:
                return newNodePart(SetField::new);
        }
        return super.createPartTemplate(parent, key);
    }

    protected class LoadField 
                    implements ListNode<String>, Copyable<Object>,
                                StatementPart<Mapping<Entity>> {

        private final LinkedHashSet<String> attributes = new LinkedHashSet<>();
        private boolean all = false;
        
        public void loadAll() {
            all = true;
        }
        
        @Override
        public void addTo(Mapping<Entity> builder) {
            if (all) {
                builder.configureWith(rs -> {
                    List<String> list = new ArrayList<>();
                    for (int i = 0; i < rs.getColumnCount(); i++) {
                        String col = rs.getColumnLabel(i+1);
                        if (getOwner().getSetters().containsKey(col)) {
                            list.add(col);
                        }
                    }
                    return getOwner().attributeConfiguration(list).newInitializer(rs);
                });
            } else {
                builder.configureWith(getOwner().attributeConfiguration(attributes));
            }
        }

        @Override
        public void add(String attribute) {
            attributes.add(attribute);
        }

        @Override
        public Object copyFor(InternalComposer<Object> iqc) {
            LoadField copy = new LoadField();
            copy.attributes.addAll(attributes);
            copy.all = all;
            return copy;
        }

        @Override
        public String toString() {
            return "LOAD " + attributes;
        }
    }
    
    protected class SetField implements MappingKey.SetAttribute, 
                        EntityInitializer<Entity>, Copyable<Object>,
                        StatementPart<Mapping<Entity>> {

        final List<Consumer<Entity>> setUps = new ArrayList<>();
        
        @Override
        public void set(String key, Supplier<?> value) {
            BiConsumer<Entity, Object> setter = getOwner().getSetters().get(key);
            if (setter == null) throw new IllegalArgumentException("Unknown setter: " + key);
            setUps.add(new Consumer<Entity>() {
                @Override
                public void accept(Entity t) {
                    setter.accept(t, value.get());
                }
                @Override
                public String toString() {
                    String vStr = String.valueOf(value);
                    int dot = vStr.lastIndexOf('.');
                    if (dot > 0) vStr = vStr.substring(dot);
                    return key + " := " + vStr;
                }
            });
        }

        @Override
        public void addTo(Mapping<Entity> builder) {
            builder.initializeWith(this);
        }

        @Override
        public void apply(Entity entity) throws MiException {
            setUps.forEach(s -> s.accept(entity));
        }

        @Override
        public void complete() throws MiException { }

        @Override
        public Object copyFor(InternalComposer<Object> iqc) {
            SetField copy = new SetField();
            copy.setUps.addAll(setUps);
            return copy;
        }

        @Override
        public String toString() {
            return "SET " + setUps;
        }
    }
}
