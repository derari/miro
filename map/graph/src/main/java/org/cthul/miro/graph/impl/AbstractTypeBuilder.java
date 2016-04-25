package org.cthul.miro.graph.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.map.ColumnValue;
import org.cthul.miro.entity.map.AttributesConfiguration;
import org.cthul.miro.entity.map.EntityAttribute;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.entity.map.EntityAttributesBuilder;
import org.cthul.miro.graph.TypeBuilder;

/**
 *
 * @param <Entity>
 * @param <This>
 */
public abstract class AbstractTypeBuilder<Entity, This extends AbstractTypeBuilder<Entity, This>> 
                extends AbstractEntityNodeType<Entity> 
                implements TypeBuilder<Entity, This>, 
                           EntityAttributesBuilder.Delegator<Entity, This> {

    private final Class<Entity> clazz;
    private final AttributesConfiguration<Entity> attributes;
    private final List<String> keys = new ArrayList<>();
    private boolean keysToConstructor;
    private ColumnReader.Factory keyReaderFactory = null;
    private Function<Object[], Entity> constructor = null;

    public AbstractTypeBuilder(Class<Entity> clazz) {
        super(clazz.getSimpleName());
        this.clazz = clazz;
        this.attributes = new AttributesConfiguration<>(clazz);
    }

    public AbstractTypeBuilder(Class<Entity> clazz, Object shortString) {
        super(shortString);
        this.clazz = clazz;
        this.attributes = new AttributesConfiguration<>(clazz);
    }

    public AttributesConfiguration<Entity> getAttributes() {
        return attributes;
    }

    public List<String> getKeys() {
        return keys;
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
    public EntityAttributesBuilder<Entity, ?> internalEntityFieldsBuilder() {
        return attributes;
    }
    
    @Override
    public Entity[] newArray(int length) {
        return (Entity[]) Array.newInstance(clazz, length);
    }

    @Override
    protected EntityConfiguration<Entity> createAttributeReader(GraphApi graph, List<?> attributes) {
        return this.attributes.newConfiguration(flattenStr(attributes));
    }

    @Override
    protected Entity newEntity(Object[] key) throws MiException {
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
        if (keysToConstructor) {
            return constructor.apply(key);
        }
        Entity e = constructor.apply(NO_ARGS);
        if (key == null) {
            return e;
        }
        for (int i = 0; i < key.length; i++) {
            EntityAttribute<Entity> at = attributes.getAttributeMap().get(keys.get(i));
            at.set(e, key[i]);
        }
        return e;
    }

    @Override
    protected Object[] getKey(Entity e, Object[] array) throws MiException {
        if (array == null) array = new Object[keys.size()];
        for (int i = 0; i < array.length; i++) {
            EntityAttribute<Entity> at = attributes.getAttributeMap().get(keys.get(i));
            array[i] = at.get(e);
        }
        return array;
    }

    @Override
    protected ColumnReader getConstructorArguments(MiResultSet rs) throws MiException {
        if (keysToConstructor) {
            return newKeyReader(rs);
        }
        return super.getConstructorArguments(rs);
    }

    @Override
    protected ColumnReader newKeyReader(MiResultSet resultSet) throws MiException {
        if (keyReaderFactory == null) {
            ColumnValue[] keyColumns = new ColumnValue[keys.size()];
            for (int i = 0; i < keyColumns.length; i++) {
                keyColumns[i] = attributes.getAttributeMap().get(keys.get(i));
            }
            keyReaderFactory = ColumnReader.factory(keyColumns);
        }
        return keyReaderFactory.create(resultSet);
    }
    
    private final static Object[] NO_ARGS = {};
}
