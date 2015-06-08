package org.cthul.miro.entity.base;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;

/**
 * Maps columns from a result set to attributes.
 * @param <Entity>
 */
public class AttributeMapping<Entity> extends AttributeMappingBase<Entity, RuntimeException, AttributeMapping<Entity>> implements EntityConfiguration<Entity> {
    
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
    
    public static int[] findAllColumns(MiResultSet rs, String... columns) throws MiException {
        return findColumns(ColumnRule.REQUIRED, ColumnRule.REQUIRED, rs, columns);
    }
    
    /**
     * 
     * @param allColumns
     * @param eachColumn
     * @param rs
     * @param columns
     * @return array of column indices
     * @throws MiException 
     */
    public static int[] findColumns(ColumnRule allColumns, ColumnRule eachColumn, MiResultSet rs, String... columns) throws MiException {
        boolean allFound = true;
        final int[] indices = findColumns(rs, columns);
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
    
    private final List<MappingEntry<Entity>> entries = new ArrayList<>();

    public AttributeMapping() {
    }

    @Override
    protected AttributeMapping<Entity> add(MappingEntry<Entity> entry) throws RuntimeException {
        entries.add(entry);
        return this;
    }
    
    public AttributeReader<Entity> newReader(MiResultSet resultSet) throws MiException {
        List<ReaderEntry<Entity>> readers = new ArrayList<>(entries.size());
        for (MappingEntry<Entity> me: entries) {
            ReaderEntry<Entity> re = me.newReader(resultSet);
            if (re != null) readers.add(re);
        }
        return new AttributeReader<>(resultSet, readers);
    }

    @Override
    public AttributeReader<Entity> newInitializer(MiResultSet rs) throws MiException {
        return newReader(rs);
    }

    @Override
    public String toString() {
        return entries.toString();
    }
    
    public static interface Setter<Entity> {
        void set(Entity e, MiResultSet rs, int index) throws MiException;
    }
    
    public static interface GroupSetter<Entity> {
        void set(Entity e, MiResultSet rs, int[] indices) throws MiException;
    }

    public static enum ColumnRule {
        
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
        OPTIONAL {
            @Override
            public boolean skipIfMissing(Object column) throws MiException {
                return true;
            }
        },
        DEFAULT {
            @Override
            public boolean skipIfMissing(Object column) throws MiException {
                return false;
            }
        };
        
        public abstract boolean skipIfMissing(Object column) throws MiException;
        
    }
}
