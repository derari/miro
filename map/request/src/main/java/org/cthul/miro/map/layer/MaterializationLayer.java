package org.cthul.miro.map.layer;

import org.cthul.miro.map.MappedType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.part.ListNode;
import static org.cthul.miro.request.template.Templates.*;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.util.XBiConsumer;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.request.ComposerKey;
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
        switch (ComposerKey.key(key)) {
//            case ALWAYS:
//                return parent.andSetUp(MappingKey.INCLUDE, inc -> 
//                        inc.addAll(getOwner().getKeys()));
        }
        switch (MappingKey.key(key)) {
            case FETCH:
                return link(MappingKey.INCLUDE, MappingKey.LOAD);
            case LOAD:
                return newNodePart(LoadField::new);
            case INCLUDE:
                return newNode(IncludeProperty::new);
            case SET:
                return newNodePart(SetField::new);
        }
        return null;
    }

    protected class LoadField 
                    implements ListNode<String>, Copyable,
                                StatementPart<Mapping<Entity>> {

        private final LinkedHashSet<String> attributes = new LinkedHashSet<>();
        private final Composer cmp;

        public LoadField(Composer cmp) {
            this.cmp = cmp;
        }
        
        @Override
        public void addTo(Mapping<Entity> builder) {
            GraphApi graph = (GraphApi) cmp.get(MappingKey.TYPE).getGraph();
            builder.configureWith(getOwner().getAttributes().newConfiguration(graph, attributes));
        }

        @Override
        public void add(String attribute) {
            attributes.add(attribute);
        }

        @Override
        public Object copyFor(CopyComposer cc) {
            LoadField copy = new LoadField(cc);
            copy.attributes.addAll(attributes);
            return copy;
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return isLatest.test(cmp.get(MappingKey.TYPE));
        }

        @Override
        public String toString() {
            return "LOAD " + attributes;
        }
    }
    
    protected class IncludeProperty implements ListNode<String>, Copyable {

        final ListNode<String> resultColumns;
        
        public IncludeProperty(Composer c) {
            Key<ListNode<String>> key = getOwner().getResultColumnsKey();
            resultColumns = c.node(key);
        }

        @Override
        public void add(String entry) {
            EntityAttribute<?,GraphApi> at = getOwner().getAttributes().getAttributeMap().get(entry);
            if (at == null) throw new IllegalArgumentException(entry);
            resultColumns.addAll(at.getColumns());
        }

        @Override
        public Object copyFor(CopyComposer cc) {
            return new IncludeProperty(cc);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
    
    protected class SetField implements MappingKey.SetProperty, 
                        EntityInitializer<Entity>, Copyable,
                        StatementPart<Mapping<Entity>> {

        final List<XConsumer<Entity, MiException>> setUps = new ArrayList<>();
        
        @Override
        public void set(String key, Supplier<?> value) {
            EntityAttribute<Entity, GraphApi> at = getOwner().getAttributes().getAttributeMap().get(key);
            if (at == null) throw new IllegalArgumentException(key);
            XBiConsumer<Entity, Object, MiException> setter = at::set;
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
        public Object copyFor(CopyComposer cc) {
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
