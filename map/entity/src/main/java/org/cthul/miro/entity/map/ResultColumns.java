package org.cthul.miro.entity.map;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.FactoryBuilder;

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
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] < 0) {
                if (i == 0 && allMissing(indices)) {
                    // apply rule for all columns missing
                    if (allColumns.skipIfMissing(columns)) {
                        return null;
                    }
                } else if (eachColumn.skipIfMissing(columns[i])) {
                    // apply rule for single missing column
                    return null;
                }
                // take as-is
                break;
            }
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
                throw missingColumn(column);
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
        
        public boolean errorOnMissing() {
            return this == REQUIRED;
        }
        
        public boolean defaultOnMissing() {
            return this == DEFAULT;
        }
    }
    
    private static MiException missingColumn(Object column) {
        if (column instanceof String[]) {
            column = String.join("','", (String[]) column);
            return new MiException("No columns '" + column + "'");
        }
        return new MiException("No column '" + column + "'");
    }
    
    public static interface ColumnMatcher {
        
        Integer test(MiResultSet resultSet) throws MiException;
        
        Integer find(MiResultSet resultSet) throws MiException;
    }
    
    public static interface ColumnsMatcher {
        
        int[] test(MiResultSet resultSet) throws MiException;
        
        int[] find(MiResultSet resultSet) throws MiException;
    }
    
    public static ColumnMatcher match(ColumnRule rule, String name) {
        return new ColumnMatcher() {
            @Override
            public Integer test(MiResultSet resultSet) throws MiException {
                int i = resultSet.findColumn(name);
                if (i < 0 && !rule.defaultOnMissing()) return null;
                return i;
            }
            @Override
            public Integer find(MiResultSet resultSet) throws MiException {
                int i = resultSet.findColumn(name);
                if (i < 0 && rule.skipIfMissing(name)) return null;
                return i;
            }
            @Override
            public String toString() {
                return rule + " " + name;
            }
        };
    }
    
    public static ColumnMatcher matchPattern(ColumnRule rule, String name) {
        return match(rule, Pattern.compile(name));
    }
    
    public static ColumnMatcher match(ColumnRule rule, Pattern name) {
        return match(rule, name.asPredicate());
    }
    
    public static ColumnMatcher matchPrefix(ColumnRule rule, String name) {
        Predicate<String> prefix = new Predicate<String>() {
            @Override
            public boolean test(String t) {
                return t.startsWith(name);
            }
            @Override
            public String toString() {
                return name + "*";
            }
        };
        return match(rule, prefix);
    }
    
    public static ColumnMatcher matchPrefixIgnoreCase(ColumnRule rule, String name) {
        String ucase = name.toUpperCase();
        Predicate<String> prefix = new Predicate<String>() {
            @Override
            public boolean test(String t) {
                return t.toUpperCase().startsWith(ucase);
            }
            @Override
            public String toString() {
                return name + "*";
            }
        };
        return match(rule, prefix);
    }
    
    public static ColumnMatcher match(ColumnRule rule, Predicate<String> name) {
        return new ColumnMatcher() {
            @Override
            public Integer test(MiResultSet resultSet) throws MiException {
                int c = resultSet.getColumnCount() + 1;
                for (int i = 1; i < c; i++) {
                    String l = resultSet.getColumnLabel(i);
                    if (name.test(l)) return i;
                }
                if (rule.defaultOnMissing()) return -1;
                return null;
            }
            @Override
            public Integer find(MiResultSet resultSet) throws MiException {
                Integer i = test(resultSet);
                if (i == null && rule.skipIfMissing(name)) return null;
                return i;
            }
            @Override
            public String toString() {
                return rule + " " + name;
            }
        };
    }
    
    public static ColumnsMatcher match(ColumnRule allRule, ColumnRule eachRule, String[] columns) {
        return new ColumnsMatcher() {
            @Override
            public int[] test(MiResultSet resultSet) throws MiException {
                try {
                    return find(resultSet);
                } catch (MiException e) {
                    return null;
                }
            }

            @Override
            public int[] find(MiResultSet resultSet) throws MiException {
                return ResultColumns.findColumns(allRule, eachRule, resultSet, columns);
            }
        };
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
    public static EntityFactory<?> readColumn(MiResultSet rs, ColumnMatcher column, ColumnMappingBuilder.MultiValue multi) throws MiException {
        Integer index = column.find(rs);
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
            @Override
            public String toString() {
                return column + "(#" + index + ")";
            }
        }
        return new SingleColumnValue();
    }

    public static void readColumn(MiResultSet rs, ColumnMatcher column, FactoryBuilder<Object> factoryBuilder) throws MiException {
        Integer index = column.find(rs);
        if (index == null) return;
        factoryBuilder
                .setFactory(() -> rs.get(index))
                .addName(column + "(#" + index + ")");
    }

    public static Object[] readColumns(MiResultSet rs, int[] indices, Object[] result) throws MiException {
        if (result == null) result = new Object[indices.length];
        for (int i = 0; i < indices.length; i++) {
            result[i] = rs.get(indices[i]);
        }
        return result;
    }
}
