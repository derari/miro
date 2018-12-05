package org.cthul.miro.sql.map;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.at.model.TypeAnnotationReader;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.domain.EntityType;
import org.cthul.miro.domain.KeyMap.KeyArray;
import org.cthul.miro.domain.impl.ColumnReader;
import org.cthul.miro.domain.impl.DomainBuilder;
import org.cthul.miro.entity.EntityTemplate;
import org.cthul.miro.entity.map.ResultColumns;
import org.cthul.miro.entity.map.ResultColumns.ColumnRule;
import org.cthul.miro.sql.composer.model.SqlAttribute;
import org.cthul.miro.entity.map.ColumnMappingBuilder;

/**
 *
 */
public class MappedSqlDomain extends DomainBuilder {
    
    final Map<String, String> schemaMap = new HashMap<>();
        
    public void setDefaultSchema(String schema) {
        getSchemaMap().put("", schema);
    }
    
    public Map<String, String> getSchemaMap() {
        return schemaMap;
    }
    
    public MappedSqlDomain put(Class<?> entityClass) {
        return put(entityClass, entityClass);
    }
    
    public MappedSqlDomain put(Object key, Class<?> entityClass) {
        AnnotatedType<?> et = new AnnotatedType<>(schemaMap, entityClass);
        put(key, et);
        et.initialize();
        return this;
    }
    
    public <N> MappedSqlBuilder<N, ?> newMapping(Object key, Class<N> entityClass) {
        return (MappedSqlBuilder) put(key, entityClass).getEntityType(key);
    }

//    @Override
//    public <N> NodeType<N> nodeType(Object key) {
//        NodeType<N> n = super.nodeType(key);
//        if (n == null && (key instanceof Class)) {
//            return put((Class<?>) key).nodeType(key);
//        }
//        return n;
//    }
    
    @Override
    protected  EntityType<?> addMissingType(Object key) {
        put(key, (Class) key);
        return getEntityType(key);
    }

    public <N> MappedSqlBuilder<N, ?> getMappingBuilder(Class<N> entityClass) {
        MappedSqlBuilder<N,?> t = (MappedSqlBuilder<N,?>) getEntityType(entityClass);
        return t;
    }
    
    public <N> MappedSelectRequest<N> newMappedSelectRequest(Class<N> entityClass) {
        return newMappedSelectRequest((Object) entityClass);
    }
    
    public <N> MappedSelectRequest<N> newMappedSelectRequest(Object key) {
        MappedSqlType<N> type = (MappedSqlType<N>) getEntityType(key);
        return type.newMappedSelectComposer();
    }
    
    protected String defaultColumnName(String propertyName) {
        return propertyName;
    }
    
    protected String defaultAliasName(String columnName, String propertyName) {
        return propertyName;
    }
    
    protected String nestedPropertyPrefix(String columnName) {
        return columnName + ".";
    }
    
    private class AnnotatedType<Entity> extends MappedSqlType<Entity> {
        private Map<String, String> schemaMap;

        public AnnotatedType(Map<String, String> schemaMap, Class<Entity> clazz) {
            super(clazz);
            this.schemaMap = schemaMap;
        }

        public void initialize() {
            new SqlAnnotationReader(this).read(entityClass());
            schemaMap = null;
        }
    }
    
    private class SqlAnnotationReader extends TypeAnnotationReader {

        final AnnotatedType<?> type;
        final Map<String, Field> fields = new HashMap<>();
        private EntityTemplate<?> keyLookUp = null;

        public SqlAnnotationReader(AnnotatedType<?> type) {
            super(type.schemaMap);
            this.type = type;
        }

        @Override
        protected void table(String schema, String table, String tableAlias) {
            type.from(schema, table, tableAlias);
        }

        @Override
        protected void property(boolean key, Field field) {
            String name = field.getName();
            fields.put(name, field);
            if (key) type.key(name);
        }

        @Override
        protected void column(String property, String tableAlias, String columnName) {
            if (columnName == null) {
                columnName = defaultColumnName(property);
            }
            defineProperty(property, tableAlias, columnName);
        }

        private void defineAttribute(String columnName, String tableAlias, String property) {
            QlCode expression = QlCode.id(columnName);
            if (tableAlias != null) {
                expression = QlCode.ql(tableAlias).ql(".").ql(expression);
            }
            SqlAttribute at = new SqlAttribute(property, expression, QlCode.ql(property));
            if (tableAlias != null) {
                at.getDependencies().add(tableAlias);
            }
            type.attribute(at);
        }
        
        private void defineProperty(String property, String tableAlias, String columnName) {
            Field f = fields.remove(property);
            Class fieldType = f.getType();
            String alias = defaultAliasName(columnName, property);
            if (fieldType.isPrimitive() || PRIMITIVES.contains(fieldType)) {
                defineAttribute(columnName, tableAlias, alias);
                type.property(property)
                    .requiredColumn(alias).field(f);
            } else if (fieldType.isAssignableFrom(List.class)) {
                
                Type gt = f.getGenericType();
                if (gt instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) gt;
                    Type argType = pt.getActualTypeArguments()[0];
                    if (PRIMITIVES.contains(argType)) {
                        return; // TODO something
                    }
                    String prefix = nestedPropertyPrefix(columnName);
                    type.property(columnName)
                        .column(ResultColumns.matchPrefixIgnoreCase(ColumnRule.OPTIONAL, prefix))
                        .readWith((rep, rs, builder) -> {
                            EntityTemplate<?> parentLookUp = keyLookUp();//rep.getEntitySet(type.entityClass()).getLookUp();
                            EntityTemplate<?> nestedLookUp = rep.getEntitySet(argType).getLookUp().andRead("*");
                            ColumnMappingBuilder.listReader(parentLookUp, nestedLookUp, prefix, rs, builder);
                        })
                        .field(f);
                }
            } else {
                String prefix = nestedPropertyPrefix(columnName);
                MappedSqlType<?> other = (MappedSqlType) getEntityType(fieldType);
                type.join(tableAlias, prefix, other);
                type.property(property)
                    .column(ResultColumns.matchPrefixIgnoreCase(ColumnRule.OPTIONAL, prefix))
                    .readWith((rep, rs, builder) -> {
                        rs = rs.subResult(prefix);
                        rep.getEntitySet(fieldType).getLookUp().andRead("*").newFactory(rs, builder);
                    })
                    .field(f);
            }
        }
        
        protected EntityTemplate<?> keyLookUp() {
            if (keyLookUp != null) return keyLookUp;
            List<String> keys = type.getKeys();
            if (keys.size() == 1) {
                String key = keys.get(0);
                return keyLookUp = (rs, builder) -> {
                    int i = rs.findColumn(key);
                    builder.setFactory(() -> rs.get(i));
                };
            } else {
                return keyLookUp = (rs, builder) -> {
                    ColumnReader reader = ColumnReader.create(rs, keys);
                    builder.setFactory(() -> new KeyArray(reader.get(null), false));
                };
            }
        }
    }
    
    private static final List<Type> PRIMITIVES = Arrays.asList(Long.class, Integer.class, String.class);
}