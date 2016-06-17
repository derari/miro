package org.cthul.miro.lib.msql;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.at.model.TypeAnnotationReader;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.map.EntityAttributesBuilder;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.graph.impl.AbstractEntityNodeType;
import org.cthul.miro.graph.impl.AbstractTypeBuilder;
import org.cthul.miro.graph.impl.GraphSchemaBuilder;
import org.cthul.strings.JavaNames;

/**
 *
 */
public class SimpleSchema extends GraphSchemaBuilder {
    
    public SimpleSchema put(Class<?> entityClass) {
        SimpleType<?> et = new SimpleType<>(entityClass);
        put(entityClass, et);
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
    
    public <T> EntityAttributesBuilder<T, ?> setUp(Class<T> clazz) {
        return (EntityAttributesBuilder) nodeType(clazz);
    }
    
    private static class SimpleType<E> extends AbstractTypeBuilder<E, SimpleType<E>> {

        public SimpleType(Class<E> clazz) {
            super(clazz);
            init();
        }
        
        private void init() {
            new TypeAnnotationReader() {
                Map<String, Field> fields = new HashMap<>();
                @Override
                protected void table(String schema, String table, String tableAlias) { }
                @Override
                protected void property(boolean key, Field field) {
                    fields.put(field.getName(), field);
                }
                @Override
                protected void column(String field, String tableAlias, String columnName) {
                    Field f = fields.remove(field);
                    if (columnName == null) columnName = JavaNames.under_score(field);
                    SimpleType.this.optional(columnName).field(f);
                }
            }.read(entityClass());
        }

        @Override
        protected AbstractEntityNodeType.BatchLoader<E> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            throw new UnsupportedOperationException();
        }

        @Override
        public EntityFactory<E> newFactory(MiResultSet rs) throws MiException {
            return super.newFactory(rs)
                .with(getAttributes().newInitializer(rs));
        }
    }
}
