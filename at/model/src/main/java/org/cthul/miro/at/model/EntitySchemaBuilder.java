package org.cthul.miro.at.model;

import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.graph.impl.GraphSchemaBuilder;
import org.cthul.miro.map.sql.MappedEntityType;

/**
 *
 */
public class EntitySchemaBuilder extends GraphSchemaBuilder {
    
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
    
    public static class AnnotatedType<Entity> extends MappedEntityType<Entity> {

        public AnnotatedType(GraphSchema schema, Class<Entity> clazz) {
            super(schema, clazz);
        }

        @Override
        public void initialize() {
            super.initialize();
            TypeAnnotationReader.read(getEntityClass(), getMappingBuilder());
        }
    }
}
