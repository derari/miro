package org.cthul.miro.at.model;

import java.util.Map;
import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.graph.impl.GraphSchemaBuilder;
import org.cthul.miro.map.sql.MappedEntityType;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.map.sql.MappedSqlBuilder;

/**
 *
 */
public class EntitySchemaBuilder extends GraphSchemaBuilder {
    
    private final TypeAnnotationReader atReader = new TypeAnnotationReader();
    
    public void setDefaultSchema(String schema) {
        getSchemaMap().put("", schema);
    }
    
    public Map<String, String> getSchemaMap() {
        return atReader.getSchemaMap();
    }
    
    public EntitySchemaBuilder put(Class<?> entityClass) {
        MappedEntityType<?> et = new AnnotatedType<>(this, entityClass);
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
        MappedEntityType<N> t = (MappedEntityType<N>) nodeType(entityClass);
        return t.getMappingBuilder();
    }
    
    public <N> TemplateLayer<? super MappedStatement<N, ? extends SelectBuilder>> getSelectLayer(Class<N> entityClass) {
        MappedEntityType<N> t = (MappedEntityType<N>) nodeType(entityClass);
        return t.getSelectLayer();
    }
    
    public <N> TemplateLayer<? super MappedStatement<N, ? extends SelectBuilder>> getSelectLayer(Object entityClass) {
        MappedEntityType<N> t = (MappedEntityType<N>) nodeType(entityClass);
        return t.getSelectLayer();
    }
    
    private static class AnnotatedType<Entity> extends MappedEntityType<Entity> {
        private TypeAnnotationReader atReader;
        public AnnotatedType(GraphSchema schema, Class<Entity> clazz) {
            super(schema, clazz);
            atReader = ((EntitySchemaBuilder) schema).atReader;
        }

        @Override
        public void initialize() {
            super.initialize();
            atReader.read(getEntityClass(), getMappingBuilder());
            atReader = null;
        }
    }
}
