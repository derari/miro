package org.cthul.miro.domain.impl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.map.ResultColumns;
import org.cthul.miro.entity.map.ColumnMapping;

/**
 * Reads column values from a result set.
 */
public interface ColumnReader {

    /**
     * Returns the current row's values.
     * @param array array of appropriate size or {@code null}.
     * @return keys
     * @throws MiException
     */
    Object[] get(Object[] array) throws MiException;
    
    interface Factory {
        
        ColumnReader create(MiResultSet rs, Repository repository) throws MiException;
    }
    
    @SuppressWarnings({"ConfusingArrayVararg", "PrimitiveArrayArgumentToVariableArgMethod"})
    static ColumnReader create(MiResultSet resultSet, String... columns) throws MiException {
        int[] indices = ResultColumns.findAllColumns(resultSet, columns);
        return create(resultSet, indices);
    }
    
    static ColumnReader create(MiResultSet resultSet, List<String> columns) throws MiException {
        int[] indices = ResultColumns.findAllColumns(resultSet, columns);
        return create(resultSet, indices);
    }
    
    static ColumnReader create(MiResultSet resultSet, int... indices) throws MiException {
        return create(resultSet, indices, (Function[]) null);
    }
    
//    @SuppressWarnings({"ConfusingArrayVararg", "PrimitiveArrayArgumentToVariableArgMethod"})
//    static ColumnReader create(MiResultSet resultSet, String[] columns, Function<Object, Object>[] postProcess) throws MiException {
//        int[] indices = ResultColumns.findAllColumns(resultSet, columns);
//        return create(resultSet, indices, postProcess);
//    }
//    
//    static ColumnReader create(MiResultSet resultSet, List<String> columns, Function<Object, Object>[] postProcess) throws MiException {
//        int[] indices = ResultColumns.findAllColumns(resultSet, columns);
//        return create(resultSet, indices, postProcess);
//    }
    
    static ColumnReader create(MiResultSet resultSet, int[] indices, Function<Object, Object>[] postProcess) throws MiException {
        class IndexReader implements ColumnReader {
            @Override
            public Object[] get(Object[] array) throws MiException {
                if (array == null) array = new Object[indices.length];
                for (int i = 0; i < indices.length; i++) {
                    int index = indices[i];
                    Object o = index < 0 ? null : resultSet.get(index);
                    if (postProcess != null && postProcess.length > i && postProcess[i] != null) {
                        o = postProcess[i].apply(o);
                    }
                    array[i] = o;
                }
                return array;
            }
            @Override
            public String toString() {
                return "Read" + Arrays.toString(indices);
            }
        }
        return new IndexReader();
    }
    
    static ColumnReader create(MiResultSet resultSet, Repository repository, ColumnMapping[] columns) throws MiException {
        EntityFactory<?>[] readers = new EntityFactory[columns.length];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = columns[i].newValueReader(repository, resultSet);
        }
        class ValueReader implements ColumnReader {
            @Override
            public Object[] get(Object[] array) throws MiException {
                if (array == null) array = new Object[readers.length];
                for (int i = 0; i < readers.length; i++) {
                    array[i] = readers[i].newEntity();
                }
                return array;
            }
            @Override
            public String toString() {
                return Arrays.toString(columns);
            }
        }
        return new ValueReader();
    }
    
//    static <Cnn> ColumnReader.Factory<Cnn> factory(String... columns) {
//        return (rs, cnn) -> create(rs, columns);
//    }
//    
//    static <Cnn> ColumnReader.Factory<Cnn> factory(List<String> columns) {
//        return rs -> create(rs, columns);
//    }
//    
//    static <Cnn> ColumnReader.Factory<Cnn> factory(int... indices) {
//        return rs -> create(rs, indices);
//    }
//    
    static ColumnReader.Factory factory(ColumnMapping... columns) {
        return (rep, rs) -> create(rep, rs, columns);
    }
    
//    static ColumnReader.Factory factory(String column, Function<Object, Object> postProcess) {
//        return factory(new String[]{column}, new Function[]{postProcess});
//    }
//    
//    static ColumnReader.Factory factory(String[] columns, Function<Object, Object>[] postProcess) {
//        return rs -> create(rs, columns, postProcess);
//    }
//    
//    static ColumnReader.Factory factory(List<String> columns, Function<Object, Object>[] postProcess) {
//        return rs -> create(rs, columns, postProcess);
//    }
//    
//    static ColumnReader.Factory factory(int[] indices, Function<Object, Object>[] postProcess) {
//        return rs -> create(rs, indices, postProcess);
//    }
}
