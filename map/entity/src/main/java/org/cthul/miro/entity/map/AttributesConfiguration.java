package org.cthul.miro.entity.map;

import org.cthul.miro.entity.base.MultiInitializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityInitializer;

/**
 * A {@linkplain EntityConfiguration configuration} that maps columns 
 * from a result set to attributes.
 * @param <Entity>
 */
public class AttributesConfiguration<Entity> 
                implements EntityAttributesBuilder<Entity, AttributesConfiguration<Entity>>,
                           EntityConfiguration<Entity>, EntityAttributes<Entity> {
    
    public static <Entity> AttributesConfiguration<Entity> build() {
        return new AttributesConfiguration<>();
    }
    
    public static <Entity> AttributesConfiguration<Entity> build(Class<Entity> entityClass) {
        return new AttributesConfiguration<>(entityClass);
    }
    
    private final Map<String, EntityAttribute<Entity>> attributeMap = new HashMap<>();
    private final List<EntityAttribute<Entity>> attributes = new ArrayList<>();
    
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

    public List<EntityAttribute<Entity>> getAttributes() {
        return attributes;
    }

    public Map<String, EntityAttribute<Entity>> getAttributeMap() {
        return attributeMap;
    }

    @Override
    public AttributesConfiguration<Entity> add(EntityAttribute<Entity> attribute) {
        attributes.add(attribute);
        attributeMap.put(attribute.getKey(), attribute);
        return this;
    }

    @Override
    public MultiInitializer<Entity> newInitializer(MiResultSet rs) throws MiException {
        return new MultiInitializer<Entity>(rs).addAll(attributes);
    }
    
    @Override
    public EntityInitializer<Entity> newInitializer(MiResultSet rs, List<String> fields) throws MiException {
        return newConfiguration(fields).newInitializer(rs);
    }

    @Override
    public EntityConfiguration<Entity> newConfiguration(Collection<String> fields) {
        Collection<String> names = fields.size() > 15 ? fields : new HashSet<>(fields);
        AttributesConfiguration copy = new AttributesConfiguration();
        attributes.stream()
                .filter(f -> names.contains(f.getKey()))
                .forEach(copy::add);
        return copy;
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}
