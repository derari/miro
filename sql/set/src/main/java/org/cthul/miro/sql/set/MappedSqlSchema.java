package org.cthul.miro.sql.set;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.at.model.TypeAnnotationReader;
import org.cthul.miro.sql.composer.model.SqlAttribute;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.entity.base.ResultColumns;
import org.cthul.miro.entity.base.ResultColumns.ColumnRule;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.graph.impl.GraphSchemaBuilder;

/**
 *
 */
public class MappedSqlSchema extends GraphSchemaBuilder {
    
    final Map<String, String> schemaMap = new HashMap<>();
        
    public void setDefaultSchema(String schema) {
        getSchemaMap().put("", schema);
    }
    
    public Map<String, String> getSchemaMap() {
        return schemaMap;
    }
    
    public MappedSqlSchema put(Class<?> entityClass) {
        return put(entityClass, entityClass);
    }
    
    public MappedSqlSchema put(Object key, Class<?> entityClass) {
        AnnotatedType<?> et = new AnnotatedType<>(schemaMap, entityClass);
        put(key, et);
        et.initialize();
        return this;
    }
    
    public <N> MappedSqlBuilder<N, ?> newMapping(Object key, Class<N> entityClass) {
        return (MappedSqlBuilder) put(key, entityClass).nodeType(key);
    }

    @Override
    public <N> NodeType<N> nodeType(Object key) {
        NodeType<N> n = super.nodeType(key);
        if (n == null && (key instanceof Class)) {
            return put((Class<?>) key).nodeType(key);
        }
        return n;
    }
    
    public <N> MappedSqlBuilder<N, ?> getMappingBuilder(Class<N> entityClass) {
        MappedSqlBuilder<N,?> t = (MappedSqlBuilder<N,?>) nodeType(entityClass);
        return t;
    }
    
    public <N> MappedSelectRequest<N> newMappedSelectRequest(Class<N> entityClass) {
        return newMappedSelectRequest((Object) entityClass);
    }
    
    public <N> MappedSelectRequest<N> newMappedSelectRequest(Object key) {
        MappedSqlType<N> type = (MappedSqlType<N>) nodeType(key);
        return type.newMappedSelectComposer();
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
                columnName = property;
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
            if (fieldType.isPrimitive() || PRIMITIVES.contains(fieldType)) {
                defineAttribute(columnName, tableAlias, property);
                type.require(property).field(f);
            } else if (fieldType.isAssignableFrom(List.class)) {
                
//                Type gt = f.getGenericType();
//                if (gt instanceof ParameterizedType) {
//                    ParameterizedType pt = (ParameterizedType) gt;
//                    Type argType = pt.getActualTypeArguments()[0];
//                    if (PRIMITIVES.contains(argType)) {
//                        return; // TODO something
//                    }
//                    String prefix = columnName + "_";
//                    type
//                            .as(columnName)
//                            .column(ResultColumns.matchPrefixIgnoreCase(ColumnRule.OPTIONAL, prefix))
//                            .readWith((rs, g) -> {
//                                // TODO use key attributes instead of "id"
//                                int idIndex = rs.findColumn("id");
//                                rs = rs.subResult(prefix);
//                                return ColumnMappingBuilder.listReader(
//                                        idIndex, rs, g.getEntityType(argType, "*"));
//                            })
//                            .field(f);
//                }
            } else {
                String prefix = columnName+".";
                MappedSqlType<?> other = (MappedSqlType) nodeType(fieldType);
                type.join(tableAlias, prefix, other);
                type
                        .as(property)
                        .column(ResultColumns.matchPrefixIgnoreCase(ColumnRule.OPTIONAL, prefix))
                        .readWith((rs, g) -> {
                            rs = rs.subResult(prefix);
                            return g.getEntityType(fieldType, "*").newFactory(rs);
                        })
                        .field(f);
            }
        }
    }
    
    private static final List<Type> PRIMITIVES = Arrays.asList(Long.class, Integer.class, String.class);
}