package org.cthul.miro.entity.base;

import java.util.List;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.map.ColumnMappingBuilder;

/**
 * Utility methods for finding columns in a result set.
 */
public class ResultColumns {

    protected ResultColumns() {
    }
 
    public static int[] findColumns(MiResultSet rs, String... fields) throws MiException {
        final int[] indices = new int[fields.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = rs.findColumn(fields[i]);
        }
        return indices;
    }

    public static int[] findColumns(MiResultSet rs, List<String> fields) throws MiException {
        final int[] indices = new int[fields.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = rs.findColumn(fields.get(i));
        }
        return indices;
    }
    
    protected static int findColumn(MiResultSet rs, String field) throws MiException {
        return rs.findColumn(field);
    }
    
    public static Integer findColumn(ColumnRule rule, MiResultSet rs, String field) throws MiException {
        int i = findColumn(rs, field);
        if (i < 0 && rule.skipIfMissing(field)) {
            return null;
        }
        return i;
    }
    
    public static int[] findAllColumns(MiResultSet rs, String... columns) throws MiException {
        return findColumns(ColumnRule.REQUIRED, ColumnRule.REQUIRED, rs, columns);
    }
    
    public static int[] findAllColumns(MiResultSet rs, List<String> columns) throws MiException {
        return findColumns(ColumnRule.REQUIRED, ColumnRule.REQUIRED, rs, columns.toArray(new String[columns.size()]));
    }
    
    /**
     * 
     * @param allColumns rule is applied when all columns are missing
     * @param eachColumn rule is applied for each column that is missing
     * @param rs
     * @param columns
     * @return array of column indices
     * @throws MiException 
     */
    public static int[] findColumns(ColumnRule allColumns, ColumnRule eachColumn, MiResultSet rs, String... columns) throws MiException {
        if (allColumns == ColumnRule.DEFAULT && eachColumn == ColumnRule.DEFAULT) {
            return findColumns(rs, columns);
        }
        final int[] indices = findColumns(rs, columns);
        boolean allFound = true;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] < 0) {
                allFound = false;
                if (i == 0 && allMissing(indices)) {
                    // all are missing -> don't apply single column rule
                    break;
                }
                // apply rule for single missing column
                if (eachColumn.skipIfMissing(columns[i])) {
                    return null;
                }
                // goto all columns rule
                break;
            }
        }
        // not all columns found -> apply rule
        if (!allFound && allColumns.skipIfMissing(columns)) {
            return null;
        }
        return indices;
    }
    
    private static boolean allMissing(int[] indices) {
        // assume first element is missing
        for (int i = 1; i < indices.length; i++) {
            if (indices[i] >= 0) return false;
        }
        return true;
    }
    
    public static enum ColumnRule {
        
        /** Throw an exception for missing columns */
        REQUIRED {
            @Override
            public boolean skipIfMissing(Object column) throws MiException {
                if (column instanceof String[]) {
                    column = String.join("','", (String[]) column);
                    throw new MiException("No columns '" + column + "'");
                }
                throw new MiException("No column '" + column + "'");
            }
        },
        /** Skip the setter for missing columns */
        OPTIONAL {
            @Override
            public boolean skipIfMissing(Object column) throws MiException {
                return true;
            }
        },
        /** Use index -1 for missing columns */
        DEFAULT {
            @Override
            public boolean skipIfMissing(Object column) throws MiException {
                return false;
            }
        };
        
        public abstract boolean skipIfMissing(Object column) throws MiException;
    }
    
//    public static ColumnValue readColumn(ColumnRule rule, String column) {
//        return readColumn(rule, null, column);
//    }
//    
//    public static ColumnValue readColumn(ColumnRule rule, Function<Object, Object> postProcess, String column) {
//        return rs -> {
//            int index = findColumns(rs, column)[0];
//            if (index < 0 && rule.skipIfMissing(index)) {
//                return null;
//            }
//            return readColumn(rs, index, postProcess);
//        };
//    }
//    
    public static EntityFactory<?> readColumn(MiResultSet rs, ColumnRule rule, String column, ColumnMappingBuilder.MultiValue multi) throws MiException {
        Integer index = findColumn(rule, rs, column);
        if (index == null) return null;
        class SingleColumnValue implements EntityFactory<Object> {
            Object[] v = multi != null ? new Object[1] : null;
            @Override
            public Object newEntity() throws MiException {
                Object o = index < 0 ? null : rs.get(index);
                if (multi == null) {
                    return o;
                }
                v[0] = o;
                return multi.join(v);
            }
        }
        return new SingleColumnValue();
    }

    public static Object[] readColumns(MiResultSet rs, int[] indices, Object[] result) throws MiException {
        if (result == null) result = new Object[indices.length];
        for (int i = 0; i < indices.length; i++) {
            result[i] = rs.get(indices[i]);
        }
        return result;
    }
}
