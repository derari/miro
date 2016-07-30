package org.cthul.miro.entity.map;

import java.util.*;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityTypes;

/**
 * A {@linkplain EntityConfiguration configuration} that maps columns 
 * from a result set to attributes.
 * @param <Entity>
 */
public class AttributesConfiguration<Entity, Cnn> 
                implements EntityAttributesBuilder<Entity, Cnn, AttributesConfiguration<Entity, Cnn>>,
                           EntityAttributes<Entity, Cnn> {
    
    public static <Entity, Cnn> AttributesConfiguration<Entity, Cnn> build() {
        return new AttributesConfiguration<>();
    }
    
    public static <Entity, Cnn> AttributesConfiguration<Entity, Cnn> build(Class<Entity> entityClass) {
        return new AttributesConfiguration<>(entityClass);
    }
    
    private final Map<String, EntityAttribute<Entity, Cnn>> attributeMap = new HashMap<>();
    private final List<EntityAttribute<Entity, Cnn>> attributes = new ArrayList<>();
    
    private final Class<Entity> entityClass;

    public AttributesConfiguration() {
        this(null);
    }
    
    public AttributesConfiguration(Class<Entity> entityClass) {
        this.entityClass = entityClass;
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
    public AttributesConfiguration<Entity, Cnn> add(EntityAttribute<Entity, Cnn> attribute) {
        attributes.add(attribute);
        attributeMap.put(attribute.getKey(), attribute);
        return this;
    }

    @Override
    public EntityInitializer<Entity> newInitializer(MiResultSet rs, Cnn cnn) throws MiException {
        List<EntityInitializer<? super Entity>> inits = new ArrayList<>();
        for (EntityAttribute<Entity, Cnn> a: attributes) {
            inits.add(a.newInitializer(rs, cnn));
        }
        return EntityTypes.multiInitializer(inits);
    }

    @Override
    public EntityInitializer<Entity> newInitializer(MiResultSet rs, Cnn cnn, List<String> fields) throws MiException {
        return newConfiguration(cnn, fields).newInitializer(rs);
    }

    @Override
    public EntityConfiguration<Entity> newConfiguration(Cnn cnn, Collection<String> fields) {
        Collection<String> names = fields.size() > 15 ? fields : new HashSet<>(fields);
        if (names.contains("*")) {
            return newConfiguration(cnn);
        }
        AttributesConfiguration copy = new AttributesConfiguration();
        attributes.stream()
                .filter(f -> names.contains(f.getKey()))
                .forEach(copy::add);
        return rs -> copy.newInitializer(rs, cnn);
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}
