package org.cthul.miro.entity.map;

import org.cthul.miro.domain.Repository;
import java.util.*;
import java.util.stream.Collectors;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.domain.EntityType;
import org.cthul.miro.entity.*;
import org.cthul.miro.entity.map.ResultColumns.ColumnRule;
import org.cthul.miro.entity.map.ResultColumns.ColumnsMatcher;

/**
 * Allows to create {@linkplain EntityConfiguration configurations} 
 * that map columns from a result set to properties.
 * @param <Entity>
 */
public class PropertiesConfiguration<Entity> 
                implements EntityPropertiesBuilder<Entity, PropertiesConfiguration<Entity>>,
                           EntityProperties<Entity> {
    
    public static <Entity> PropertiesConfiguration<Entity> build() {
        return new PropertiesConfiguration<>();
    }
    
    public static <Entity> PropertiesConfiguration<Entity> build(Class<Entity> entityClass) {
        return new PropertiesConfiguration<>(entityClass);
    }
    
    private final Map<String, MappedProperty<Entity>> attributeMap = new HashMap<>();
    private final List<MappedProperty<Entity>> attributes = new ArrayList<>();
    
    private final Class<Entity> entityClass;
    private final List<MappedProperty<Entity>> starAttributes;

    public PropertiesConfiguration() {
        this(null);
    }
    
    public PropertiesConfiguration(Class<Entity> entityClass) {
        this.entityClass = entityClass;
        this.starAttributes = Collections.emptyList();
    }

    public PropertiesConfiguration(Class<Entity> entityClass, List<MappedProperty<Entity>> starAttributes) {
        this.entityClass = entityClass;
        this.starAttributes = starAttributes;
    }

    @Override
    public Class<Entity> entityClass() {
        return entityClass;
    }

//    public List<EntityAttribute<Entity>> getAttributes() {
//        return attributes;
//    }

    public Map<String, MappedProperty<Entity>> getAttributeMap() {
        return attributeMap;
    }

    @Override
    public PropertiesConfiguration<Entity> add(MappedProperty<Entity> attribute) {
        attributes.add(attribute);
        attributeMap.put(attribute.getKey(), attribute);
        return this;
    }

    public PropertiesConfiguration<Entity> addStar(MappedProperty<Entity> attribute) {
        starAttributes.add(attribute);
        attributeMap.put(attribute.getKey(), attribute);
        return this;
    }

    @Override
    public PropertiesConfiguration<Entity> select(Collection<String> fields) {
//        Collection<String> names = new HashSet<>(fields);
//        boolean star = names.remove("*");
//        PropertiesConfiguration<Entity> copy = new PropertiesConfiguration<>(entityClass, starAttributes);
//        if (star) {
//            starAttributes.forEach(at -> {
//                names.remove(at.getKey());
//                copy.add(at);
//            });
//        }
//        if (names.isEmpty()) return copy;
//        for (MappedProperty<Entity> at: attributes) {
//            if (names.remove(at.getKey())) {
//                copy.add(at);
//            }
//            if (names.isEmpty()) break;
//        }
//        return copy;
        Collection<String> names = fields.size() < 16 ? fields : new HashSet<>(fields);
        boolean star = names.contains("*");
        PropertiesConfiguration<Entity> copy = new PropertiesConfiguration<>(
                entityClass, star ? new ArrayList<>() : Collections.emptyList());
        attributes.forEach(at -> {
            if (names.contains(at.getKey())) {
                copy.add(at);
            } else if (star) {
                copy.addStar(at.nested(STAR));
            } else {
                String prefix = at.getKey() + ".";
                List<String> nested = names.stream()
                        .filter(s -> s.startsWith(prefix))
                        .map(s -> s.substring(prefix.length()))
                        .collect(Collectors.toList());
                if (!nested.isEmpty()) {
                    copy.add(at.nested(nested));
                }
            }
            });
        return copy;
    }

    @Override
    public void newReader(Repository repository, MiResultSet resultSet, InitializationBuilder<? extends Entity> initBuilder) throws MiException {
        for (MappedProperty<Entity> a: attributes) {
            a.newInitializer(repository, resultSet, initBuilder);
        }
        for (MappedProperty<Entity> a: starAttributes) {
            if (a.getMapping().accept(resultSet)) {
                a.newInitializer(repository, resultSet, initBuilder);
            }
        }
    }

    public ColumnMapping mapToColumns(EntityType<Entity> owner, Object key, String prefix) {
        List<String> columns = new ArrayList<>();
        attributes.stream()
                .flatMap(a -> a.getMapping().getColumns().stream())
                .map(c -> prefix + c)
                .forEach(columns::add);
        starAttributes.stream()
                .flatMap(a -> a.getMapping().getColumns().stream())
                .map(c -> prefix + c)
                .forEach(columns::add);

        ColumnMappingBuilder.SimpleBuilder<Entity> sb = new ColumnMappingBuilder.SimpleBuilder<>();
        sb.columns(matcher(prefix), columns)
                .readWith((rep, rs, fb) -> {
                    rs = rs.subResult(prefix);
                    EntitySelector<Entity> sel = rep.<Entity>getEntitySet(key).getSelector();
                    fb.set(owner.newEntityLookUp(rep, sel), rs)
                      .add((rs2, ib) -> newReader(rep, rs2, ib), rs);
                })
                .nested(a -> null); // select(a)
        return sb.getColumnMapping();
    }

    @Override
    public String toString() {
        return attributes.toString();
    }

    private ColumnsMatcher matcher(String prefix) {
        return new ColumnsMatcher() {
            @Override
            public int[] test(MiResultSet resultSet) throws MiException {
                resultSet = resultSet.subResult(prefix);
                for (MappedProperty<Entity> a: attributes) {
                    if (!a.getMapping().accept(resultSet)) return null;
                }
                return MATCH;
            }
            @Override
            public int[] find(MiResultSet resultSet) throws MiException {
                return test(resultSet);
            }
        };
    }
    
    private static final List<String> STAR = Arrays.asList("*");
    private static final int[] MATCH = {-1};
}
