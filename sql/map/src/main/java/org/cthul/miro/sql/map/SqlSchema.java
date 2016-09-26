package org.cthul.miro.sql.map;

import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.NodeType;
import org.cthul.miro.graph.impl.GraphSchemaBuilder;
import org.cthul.miro.entity.map.EntityPropertiesBuilder;

/**
 *
 */
public class SqlSchema extends GraphSchemaBuilder {
    
    public SqlSchema put(Class<?> entityClass) {
        SqlTableType<?> et = new SqlTableType<>(entityClass);
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
    
    public <T> EntityPropertiesBuilder<T,Graph,?> setUp(Class<T> clazz) {
        return (EntityPropertiesBuilder) nodeType(clazz);
    }
}
