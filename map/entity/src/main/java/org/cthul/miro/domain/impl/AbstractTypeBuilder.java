package org.cthul.miro.domain.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.builder.SelectorBuilder;
import org.cthul.miro.entity.map.*;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class AbstractTypeBuilder<Entity, This extends AbstractTypeBuilder<Entity, This>> 
                extends AbstractEntityType<Entity> 
                implements TypeBuilder<Entity, This>, EntityPropertiesBuilder.Delegator<Entity, This> {

    private final Class<Entity> clazz;
    private final PropertiesConfiguration<Entity> properties;
    private final List<String> keys = new ArrayList<>();
    private boolean keysToConstructor;
    private ColumnReader.Factory keyReaderFactory = null;
    private Function<Object[], Entity> constructor = null;

    public AbstractTypeBuilder(Class<Entity> clazz) {
        super(clazz.getSimpleName());
        this.clazz = clazz;
        this.properties = new PropertiesConfiguration<>(clazz);
    }

    public AbstractTypeBuilder(Class<Entity> clazz, Object shortString) {
        super(shortString);
        this.clazz = clazz;
        this.properties = new PropertiesConfiguration<>(clazz);
    }

    public PropertiesConfiguration<Entity> getAttributes() {
        return properties;
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
    public EntityPropertiesBuilder<Entity, ?> internalPropertiesBuilder() {
        return properties;
    }
    
    @Override
    protected EntityConfiguration<Entity> createPropertiesReader(Repository repository, Collection<?> properties) {
        List<String> propNames = flattenStr(properties);
        return this.properties.read(repository, propNames);
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
    public void newEntityCreator(Repository repository, SelectorBuilder<Entity> builder) {
        initConstructor();
        if (keysToConstructor) {
            builder.setSelector(constructor::apply);
        } else {
            MappedProperty<Entity>[] keyAts = new MappedProperty[keys.size()];
            for (int i = 0; i < keys.size(); i++) {
                keyAts[i] = properties.getAttributeMap().get(keys.get(i));
            }
            builder.setSelector(key -> {
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
            MappedProperty<Entity> at = properties.getAttributeMap().get(keys.get(i));
            array[i] = at.get(e);
        }
        return array;
    }

    @Override
    protected ColumnReader newKeyReader(Repository repository, MiResultSet resultSet) throws MiException {
        if (keyReaderFactory == null) {
            ColumnMapping[] keyColumns = new ColumnMapping[keys.size()];
            for (int i = 0; i < keyColumns.length; i++) {
                keyColumns[i] = properties.getAttributeMap().get(keys.get(i)).getMapping();
                if (keyColumns[i] == null) throw new IllegalArgumentException(keys.get(i));
            }
            keyReaderFactory = ColumnReader.factory(keyColumns);
        }
        return keyReaderFactory.create(resultSet, repository);
    }

    @Override
    public ColumnMapping mapToColumns(String prefix, Object key) {
        return ((PropertiesConfiguration) this.properties.select(keys)).mapToColumns(this, key, prefix);
    }
    
    private final static Object[] NO_ARGS = {};
}
