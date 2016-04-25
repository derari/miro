package org.cthul.miro.map.layer;

import org.cthul.miro.map.MappedType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.part.ListNode;
import static org.cthul.miro.request.template.Templates.*;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.util.XBiConsumer;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.util.Key;
import org.cthul.miro.util.XConsumer;

/**
 * @param <Entity>
 */
public class MaterializationLayer<Entity> extends AbstractMappingLayer<Entity, Mapping<Entity>> {

    public MaterializationLayer(MappedType<Entity, ?> owner) {
        super(owner);
    }

    @Override
    protected String getShortString() {
        return "Materialize";
    }

    @Override
    protected Template<? super Mapping<Entity>> createPartTemplate(Parent<Mapping<Entity>> parent, Object key) {
        switch (MappingKey.key(key)) {
            case LOAD_ALL:
                return setUp(MappingKey.LOAD, lf -> ((LoadField) lf).loadAll());
            case LOAD:
                return newNodePart(LoadField::new);
            case INCLUDE:
                return newNode(IncludeProperty::new);
            case SET:
                return newNodePart(SetField::new);
        }
        return super.createPartTemplate(parent, key);
    }

    protected class LoadField 
                    implements ListNode<String>, Copyable<Object>,
                                StatementPart<Mapping<Entity>> {

        private final Composer cmp;
        private final LinkedHashSet<String> attributes = new LinkedHashSet<>();
        private boolean all = false;

        public LoadField(Composer cmp) {
            this.cmp = cmp;
        }
        
        public void loadAll() {
            all = true;
        }
        
        @Override
        public void addTo(Mapping<Entity> builder) {
            if (all) {
                builder.configureWith(rs -> {
                    return getOwner().getAttributes().newInitializer(rs);
//                    List<String> list = new ArrayList<>();
//                    for (int i = 0; i < rs.getColumnCount(); i++) {
//                        String col = rs.getColumnLabel(i+1);
//                        getOwner().getAttributes().getAttributeMap().
//                        if (getOwner().getSetters().containsKey(col)) {
//                            list.add(col);
//                        }
//                    }
//                    return getOwner().attributeConfiguration(list).newInitializer(rs);
                });
            } else {
                builder.configureWith(getOwner().getAttributes().newConfiguration(attributes));
            }
        }

        @Override
        public void add(String attribute) {
            if (attributes.add(attribute)) {
                cmp.node(MappingKey.INCLUDE).add(attribute);
            }
        }

        @Override
        public Object copyFor(InternalComposer<Object> ic) {
            LoadField copy = new LoadField(ic);
            copy.attributes.addAll(attributes);
            copy.all = all;
            return copy;
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }

        @Override
        public String toString() {
            return "LOAD " + attributes;
        }
    }
    
    protected class IncludeProperty implements ListNode<String>, Copyable<Object> {

        final ListNode<String> resultColumns;
        
        public IncludeProperty(Composer c) {
            Key<ListNode<String>> key = getOwner().getResultColumnsKey();
            resultColumns = c.node(key);
        }

        @Override
        public void add(String entry) {
            EntityAttribute<?> at = getOwner().getAttributes().getAttributeMap().get(entry);
            resultColumns.addAll(at.getColumns());
        }

        @Override
        public Object copyFor(InternalComposer<Object> ic) {
            return new IncludeProperty(ic);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
    
    protected class SetField implements MappingKey.SetProperty, 
                        EntityInitializer<Entity>, Copyable<Object>,
                        StatementPart<Mapping<Entity>> {

        final List<XConsumer<Entity, MiException>> setUps = new ArrayList<>();
        
        @Override
        public void set(String key, Supplier<?> value) {
            EntityAttribute<Entity> at = getOwner().getAttributes().getAttributeMap().get(key);
            XBiConsumer<Entity, Object, MiException> setter = at::set;
            if (setter == null) throw new IllegalArgumentException("Unknown setter: " + key);
            setUps.add(new XConsumer<Entity, MiException>() {
                @Override
                public void accept(Entity t) throws MiException {
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
            for (XConsumer<Entity, MiException> c: setUps) {
                c.accept(entity);
            }
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
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }

        @Override
        public String toString() {
            return "SET " + setUps;
        }
    }
}
