package org.cthul.miro.graph.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.FactoryBuilder;
import org.cthul.miro.entity.map.PropertiesConfiguration;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.TypeBuilder;
import org.cthul.miro.entity.map.ColumnMapping;
import org.cthul.miro.graph.SelectorBuilder;
import org.cthul.miro.entity.map.EntityPropertiesBuilder;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class AbstractTypeBuilder<Entity, This extends AbstractTypeBuilder<Entity, This>> 
                extends AbstractNodeType<Entity> 
                implements TypeBuilder<Entity, GraphApi, This>, 
                           EntityPropertiesBuilder.Delegator<Entity, GraphApi, This> {

    private final Class<Entity> clazz;
    private final PropertiesConfiguration<Entity, GraphApi> attributes;
    private final List<String> keys = new ArrayList<>();
    private boolean keysToConstructor;
    private ColumnReader.Factory<GraphApi> keyReaderFactory = null;
    private Function<Object[], Entity> constructor = null;

    public AbstractTypeBuilder(Class<Entity> clazz) {
        super(clazz.getSimpleName());
        this.clazz = clazz;
        this.attributes = new PropertiesConfiguration<>(clazz);
    }

    public AbstractTypeBuilder(Class<Entity> clazz, Object shortString) {
        super(shortString);
        this.clazz = clazz;
        this.attributes = new PropertiesConfiguration<>(clazz);
    }

    public PropertiesConfiguration<Entity, GraphApi> getAttributes() {
        return attributes;
    }

    public List<String> getKeys() {
        return keys;
    }
    
    @Override
    public Class<Entity> entityClass() {
        return clazz;
    }

    @Override
    public This key(String attribute) {
        keys.add(attribute);
        return (This) this;
    }

    @Override
    public This constructor(Supplier<Entity> constructor) {
        this.constructor = no_args -> constructor.get();
        return (This) this;
    }

    @Override
    public This constructor(Function<Object[], Entity> constructor) {
        this.keysToConstructor = true;
        this.constructor = constructor;
        return (This) this;
    }

    @Override
    public EntityPropertiesBuilder<Entity, GraphApi, ?> internalEntityFieldsBuilder() {
        return attributes;
    }
    
    @Override
    protected EntityConfiguration<Entity> createAttributeReader(GraphApi graph, List<?> attributes) {
        return this.attributes.newConfiguration(graph, flattenStr(attributes));
    }
    
    private void initConstructor() {
        if (constructor == null) {
            if (keysToConstructor) throw new IllegalStateException("Constructor required");
            try {
                Constructor<Entity> c = clazz.getConstructor();
                c.setAccessible(true);
                constructor = no_args -> {
                    try {
                        return c.newInstance();
                    } catch (ReflectiveOperationException ex) {
                        throw new RuntimeException(ex);
                    }
                };
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("Default constructor expected");
            }
        }
    }

    @Override
    public void newNodeFactory(GraphApi graph, SelectorBuilder<? super Entity> builder) throws MiException {
        initConstructor();
        if (keysToConstructor) {
            builder.setFactory(constructor::apply);
        } else {
            EntityAttribute<Entity, GraphApi>[] keyAts = new EntityAttribute[keys.size()];
            for (int i = 0; i < keys.size(); i++) {
                keyAts[i] = attributes.getAttributeMap().get(keys.get(i));
            }
            builder.setFactory(key -> {
                Entity e = constructor.apply(NO_ARGS);
                if (key == null) return e;
                for (int i = 0; i < key.length; i++) {
                    keyAts[i].set(e, key[i]);
                }
                return e;
            });
        }
        builder.addName("new " + clazz.getSimpleName());
    }

//    @Override
//    public void newEntityFactory(GraphApi graph, FactoryBuilder<? super Entity> builder) throws MiException {
//        initConstructor();
//        if (keysToConstructor) {
//            super.newEntityFactory(graph, builder);
//        } else {
//            builder.setFactory(() -> constructor.apply(NO_ARGS));
//            builder.addName("new " + clazz.getSimpleName());
//        }
//    }

    @Override
    protected Object[] getKey(Entity e, Object[] array) throws MiException {
        if (array == null) array = new Object[keys.size()];
        for (int i = 0; i < array.length; i++) {
            EntityAttribute<Entity, GraphApi> at = attributes.getAttributeMap().get(keys.get(i));
            array[i] = at.get(e);
        }
        return array;
    }

    @Override
    protected ColumnReader newKeyReader(MiResultSet resultSet, GraphApi graph) throws MiException {
        if (keyReaderFactory == null) {
            ColumnMapping<GraphApi>[] keyColumns = new ColumnMapping[keys.size()];
            for (int i = 0; i < keyColumns.length; i++) {
                keyColumns[i] = attributes.getAttributeMap().get(keys.get(i));
                if (keyColumns[i] == null) throw new IllegalArgumentException(keys.get(i));
            }
            keyReaderFactory = ColumnReader.factory(keyColumns);
        }
        return keyReaderFactory.create(resultSet, graph);
    }
    
    private final static Object[] NO_ARGS = {};
}
