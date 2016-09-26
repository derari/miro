package org.cthul.miro.entity.map;

import java.util.*;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.InitializationBuilder;

/**
 * Allows to create {@linkplain EntityConfiguration configurations} 
 * that map columns from a result set to properties.
 * @param <Entity>
 * @param <Cnn>
 */
public class PropertiesConfiguration<Entity, Cnn> 
                implements EntityPropertiesBuilder<Entity, Cnn, PropertiesConfiguration<Entity, Cnn>>,
                           EntityProperties<Entity, Cnn> {
    
    public static <Entity, Cnn> PropertiesConfiguration<Entity, Cnn> build() {
        return new PropertiesConfiguration<>();
    }
    
    public static <Entity, Cnn> PropertiesConfiguration<Entity, Cnn> build(Class<Entity> entityClass) {
        return new PropertiesConfiguration<>(entityClass);
    }
    
    private final Map<String, EntityAttribute<Entity, Cnn>> attributeMap = new HashMap<>();
    private final List<EntityAttribute<Entity, Cnn>> attributes = new ArrayList<>();
    
    private final Class<Entity> entityClass;
    private final List<EntityAttribute<Entity, Cnn>> starAttributes;

    public PropertiesConfiguration() {
        this(null);
    }
    
    public PropertiesConfiguration(Class<Entity> entityClass) {
        this.entityClass = entityClass;
        this.starAttributes = Collections.emptyList();
    }

    public PropertiesConfiguration(Class<Entity> entityClass, List<EntityAttribute<Entity, Cnn>> starAttributes) {
        this.entityClass = entityClass;
        this.starAttributes = starAttributes;
    }

    @Override
    public Class<Entity> entityClass() {
        return entityClass;
    }

    public List<EntityAttribute<Entity, Cnn>> getAttributes() {
        return attributes;
    }

    public Map<String, EntityAttribute<Entity, Cnn>> getAttributeMap() {
        return attributeMap;
    }

    @Override
    public PropertiesConfiguration<Entity, Cnn> add(EntityAttribute<Entity, Cnn> attribute) {
        attributes.add(attribute);
        attributeMap.put(attribute.getKey(), attribute);
        return this;
    }

    @Override
    public EntityProperties<Entity, Cnn> select(Collection<String> fields) {
        Collection<String> names = fields.size() < 16 ? fields : new HashSet<>(fields);
        boolean star = names.contains("*");
        PropertiesConfiguration<Entity, Cnn> copy = new PropertiesConfiguration<>(
                entityClass, star ? new ArrayList<>() : Collections.emptyList());
        attributes.forEach(at -> {
            if (names.contains(at.getKey())) {
                copy.add(at);
            } else if (star) {
                copy.starAttributes.add(at);
            }
        });
        return copy;
    }

    @Override
    public void newInitializer(MiResultSet resultSet, Cnn cnn, InitializationBuilder<? extends Entity> builder) throws MiException {
        for (EntityAttribute<Entity, Cnn> a: attributes) {
            a.newInitializer(resultSet, cnn, builder);
        }
        for (EntityAttribute<Entity, Cnn> a: starAttributes) {
            if (a.accept(resultSet, cnn)) {
                a.newInitializer(resultSet, cnn, builder);
            }
        }
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}
