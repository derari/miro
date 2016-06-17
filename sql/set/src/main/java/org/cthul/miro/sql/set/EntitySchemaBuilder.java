package org.cthul.miro.sql.set;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.at.model.TypeAnnotationReader;
import org.cthul.miro.sql.template.SqlAttribute;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.graph.impl.GraphSchemaBuilder;
import org.cthul.miro.sql.map.MappedSqlBuilder;
import org.cthul.miro.map.layer.MappedQuery;
import org.cthul.miro.sql.map.MappedSqlType;

/**
 *
 */
public class EntitySchemaBuilder extends GraphSchemaBuilder {
    
    final Map<String, String> schemaMap = new HashMap<>();
        
    public void setDefaultSchema(String schema) {
        getSchemaMap().put("", schema);
    }
    
    public Map<String, String> getSchemaMap() {
        return schemaMap;
    }
    
    public EntitySchemaBuilder put(Class<?> entityClass) {
        AnnotatedType<?> et = new AnnotatedType<>(schemaMap, entityClass);
        put(entityClass, et);
        et.initialize();
        return this;
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
        AnnotatedType<N> t = (AnnotatedType<N>) nodeType(entityClass);
        return t;
    }
    
    public <N> TemplateLayer<MappedQuery<N, SelectQuery>> getSelectLayer(Class<N> entityClass) {
        AnnotatedType<N> t = (AnnotatedType<N>) nodeType(entityClass);
        return t.getSelectLayer();
    }
    
    public <N> TemplateLayer<MappedQuery<N, SelectQuery>> getSelectLayer(Object entityClass) {
        AnnotatedType<N> t = (AnnotatedType<N>) nodeType(entityClass);
        return t.getSelectLayer();
    }
    
    private static class AnnotatedType<Entity> extends MappedSqlType<Entity> {
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
    
    private static class SqlAnnotationReader extends TypeAnnotationReader {

        final AnnotatedType<?> type;

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
            type.column(name).field(field);
            if (key) type.key(name);
        }

        @Override
        protected void column(String property, String tableAlias, String columnName) {
            if (columnName == null) {
                columnName = property;
            }
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
    }
}
