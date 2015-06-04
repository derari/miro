package org.cthul.miro.graph.base;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.base.AttributeMapping;

public interface EntityKeyAdapter<Entity> {
    
    Entity newEntity(Object[] key);
    
    Object[] getKey(Entity e, Object[] array);
    
    KeyReader newKeyReader(MiResultSet resultSet) throws MiException;
    
    interface KeyReader {
    
        Object[] getKey(Object[] array) throws MiException;
    }
    
    @SuppressWarnings({"ConfusingArrayVararg", "PrimitiveArrayArgumentToVariableArgMethod"})
    public static KeyReader newKeyReader(MiResultSet resultSet, String... columns) throws MiException {
        int[] indices = AttributeMapping.findAllColumns(resultSet, columns);
        return newKeyReader(resultSet, indices);
    }
    
    public static KeyReader newKeyReader(MiResultSet resultSet, int... indices) throws MiException {
        return array -> {
            if (array == null) array = new Object[indices.length];
            for (int i = 0; i < indices.length; i++) {
                int index = indices[i];
                array[i] = index < 0 ? null : resultSet.get(index);
            }
            return array;
        };
    }
}
