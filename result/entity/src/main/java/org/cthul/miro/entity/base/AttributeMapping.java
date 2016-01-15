package org.cthul.miro.entity.base;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.base.ResultColumns.ColumnRule;
import static org.cthul.miro.entity.base.ResultColumns.findColumns;

/**
 * Base class for configuring a mapping from result columns to entity attributes.
 * @param <Entity>
 * @param <Ex>
 * @param <This>
 */
public interface AttributeMapping<Entity, Ex extends Exception, This> {
    
    /**
     * Adds an entry to the mapping.
     * @param entry
     * @return this
     * @throws Ex exception 
     */
    This add(MappingEntry<Entity> entry) throws Ex;
    
    /**
     * Adds required attribute.
     * @param column
     * @param setter
     * @return this
     * @throws Ex exception
     */
    default This required(String column, Setter<Entity> setter) throws Ex {
        return add(new MapSingle<>(ColumnRule.REQUIRED, column, setter));
    }
    
    /**
     * Adds optional attribute.
     * @param column
     * @param setter
     * @return this
     * @throws Ex exception
     */
    default This optional(String column, Setter<Entity> setter) throws Ex {
        return add(new MapSingle<>(ColumnRule.OPTIONAL, column, setter));
    }
    
    /**
     * Adds attribute with no default behavior.
     * Setter will be called with index -1 if column is not present.
     * @param column
     * @param setter
     * @return this
     * @throws Ex exception
     */
    default This attribute(String column, Setter<Entity> setter) throws Ex {
        return add(new MapSingle<>(ColumnRule.DEFAULT, column, setter));
    }
    
    /**
     * Adds required attributes.
     * @param columns
     * @param setter
     * @return this
     * @throws Ex exception
     */
    default This required(String[] columns, GroupSetter<Entity> setter) throws Ex {
        return add(new MapGroup<>(ColumnRule.REQUIRED, ColumnRule.REQUIRED, columns, setter));
    }
    
    /**
     * Adds optional attributes.
     * If only some attributes are missing, their indices are set to -1.
     * @param columns
     * @param setter
     * @return 
     * @throws Ex exception
     */
    default This optional(String[] columns, GroupSetter<Entity> setter) throws Ex {
        return add(new MapGroup<>(ColumnRule.OPTIONAL, ColumnRule.DEFAULT, columns, setter));
    }
    
    /**
     * Adds optional attributes.
     * If any attribute is missing, setter is not called.
     * @param columns
     * @param setter
     * @return this
     * @throws Ex exception
     */
    default This allOrNone(String[] columns, GroupSetter<Entity> setter) throws Ex {
        return add(new MapGroup<>(ColumnRule.OPTIONAL, ColumnRule.OPTIONAL, columns, setter));
    }
    
    /**
     * Requires that at least one column is present.
     * @param columns
     * @param setter
     * @return this
     * @throws Ex exception 
     */
    default This any(String[] columns, GroupSetter<Entity> setter) throws Ex {
        return add(new MapGroup<>(ColumnRule.REQUIRED, ColumnRule.DEFAULT, columns, setter));
    }
    
    /**
     * Adds attributes.
     * Setter will always be called, missing attributes will have index -1.
     * @param columns
     * @param setter
     * @return this
     * @throws Ex exception
     */
    default This attributes(String[] columns, GroupSetter<Entity> setter) throws Ex {
        return add(new MapGroup<>(ColumnRule.DEFAULT, ColumnRule.DEFAULT, columns, setter));
    }
    
    /**
     * Adds required attributes.
     * @param columns
     * @return group configuration
     * @throws Ex exception
     */
    default ConfigureGroup<Entity, Ex, This> required(String... columns) throws Ex {
        return group(ColumnRule.REQUIRED, ColumnRule.REQUIRED, columns);
    }
    
    /**
     * Adds optional attributes.
     * If only some attributes are missing, their indices are set to -1.
     * @param columns
     * @return group configuration
     * @throws Ex exception
     */
    default ConfigureGroup<Entity, Ex, This> optional(String... columns) throws Ex {
        return group(ColumnRule.OPTIONAL, ColumnRule.DEFAULT, columns);
    }
    
    /**
     * Adds optional attributes.
     * If any attribute is missing is not called.
     * @param columns
     * @return group configuration
     * @throws Ex exception
     */
    default ConfigureGroup<Entity, Ex, This> allOrNone(String... columns) throws Ex {
        return group(ColumnRule.OPTIONAL, ColumnRule.OPTIONAL, columns);
    }
    
    /**
     * Requires that at least one column is present.
     * @param columns
     * @return group configuration
     * @throws Ex exception 
     */
    default ConfigureGroup<Entity, Ex, This> any(String... columns) throws Ex {
        return group(ColumnRule.REQUIRED, ColumnRule.DEFAULT, columns);
    }
    
    /**
     * Adds attributes.
     * Setter will always be called, missing attributes will have index -1.
     * @param columns
     * @return group configuration
     * @throws Ex exception
     */
    default ConfigureGroup<Entity, Ex, This> attributes(String... columns) throws Ex {
        return group(ColumnRule.DEFAULT, ColumnRule.DEFAULT, columns);
    }
    
    default ConfigureGroup<Entity, Ex, This> group(ColumnRule ruleAny, ColumnRule ruleAll, String... columns) {
        return new ConfigureGroup<>(this, ruleAny, ruleAll, columns);
    }

    interface MappingEntry<Entity> {
        
        boolean setsAttribute(String attribute);

        ReaderEntry<Entity> newReader(MiResultSet resultSet) throws MiException;
    }
    
    class MapSingle<Entity> implements MappingEntry<Entity> {
        
        private final ColumnRule rule;
        private final String column;
        private final Setter<Entity> setter;

        public MapSingle(ColumnRule rule, String column, Setter<Entity> setter) {
            this.rule = rule;
            this.column = column;
            this.setter = setter;
        }

        @Override
        public boolean setsAttribute(String attribute) {
            return column.equals(attribute);
        }

        @Override
        public ReaderEntry<Entity> newReader(MiResultSet resultSet) throws MiException {
            int index = resultSet.findColumn(column);
            if (index < 0 && rule.skipIfMissing(column)) {
                return null;
            }
            return new ReadSingle<>(resultSet, index, setter);
        }

        @Override
        public String toString() {
            return column;
        }
    }
    
    class MapGroup<Entity> implements MappingEntry<Entity> {
        
        private final String name;
        private final ColumnRule allColumns;
        private final ColumnRule eachColumn;
        private final String[] columns;
        private final GroupSetter<Entity> setter;

        /**
         * @param allColumns rule is applied if all columns are missing
         * @param eachColumn rule is applied for each missing column
         * @param columns
         * @param setter 
         */
        public MapGroup(ColumnRule allColumns, ColumnRule eachColumn, String[] columns, GroupSetter<Entity> setter) {
            this(null, allColumns, eachColumn, columns, setter);
        }
        
        /**
         * @param name
         * @param allColumns rule is applied if all columns are missing
         * @param eachColumn rule is applied for each missing column
         * @param columns
         * @param setter 
         */
        public MapGroup(String name, ColumnRule allColumns, ColumnRule eachColumn, String[] columns, GroupSetter<Entity> setter) {
            this.name = name;
            this.allColumns = allColumns;
            this.eachColumn = eachColumn;
            this.columns = columns;
            this.setter = setter;
        }

        @Override
        public boolean setsAttribute(String attribute) {
            if (name != null) {
                return name.equals(attribute);
            }
            for (String c: columns) {
                if (c.equals(attribute)) return true;
            }
            return false;
        }

        @Override
        public ReaderEntry<Entity> newReader(MiResultSet resultSet) throws MiException {
            int[] indices = findColumns(allColumns, eachColumn, resultSet, columns);
            if (indices == null) return null;
            return new ReadGroup<>(resultSet, indices, setter);
        }

        @Override
        public String toString() {
            return "[" + String.join(",", columns) + "]";
        }
    }
    
    interface ReaderEntry<Entity> {
        
        void apply(Entity e) throws MiException;
    }
    
    static class ReadSingle<Entity> implements ReaderEntry<Entity> {
        private final MiResultSet rs;
        private final int index;
        private final Setter<Entity> setter;

        public ReadSingle(MiResultSet rs, int index, Setter<Entity> setter) {
            this.rs = rs;
            this.index = index;
            this.setter = setter;
        }

        @Override
        public void apply(Entity e) throws MiException {
            setter.set(e, rs, index);
        }

        @Override
        public String toString() {
            try {
                return rs.getColumnLabel(index);
            } catch (MiException e) {
                return "#" + index;
            }
        }
    }
    
    class ReadGroup<Entity> implements ReaderEntry<Entity> {
        private final MiResultSet rs;
        private final int[] indices;
        private final GroupSetter<Entity> setter;

        public ReadGroup(MiResultSet rs, int[] indices, GroupSetter<Entity> setter) {
            this.rs = rs;
            this.indices = indices;
            this.setter = setter;
        }

        @Override
        public void apply(Entity e) throws MiException {
            setter.set(e, rs, indices);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i: indices) {
                try {
                    sb.append(rs.getColumnLabel(i));
                } catch (MiException e) {
                    sb.append("#").append(i);
                }
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
            return sb.append("]").toString();
        }
    }
    
    public class ConfigureGroup<Entity, E extends Exception, Mapping> {
        
        private final AttributeMapping<Entity, E, Mapping> mapping;
        private final ColumnRule ruleAny;
        private final ColumnRule ruleAll;
        private final String[] columns;
        private String name = null;

        protected ConfigureGroup(AttributeMapping<Entity, E, Mapping> mapping, ColumnRule ruleAny, ColumnRule ruleAll, String[] columns) {
            this.mapping = mapping;
            this.ruleAny = ruleAny;
            this.ruleAll = ruleAll;
            this.columns = columns;
        }
        
        public ConfigureGroup<Entity, E, Mapping> as(String name) {
            this.name = name;
            return this;
        }
        
        public Mapping set(GroupSetter<Entity> setter) throws E {
            return mapping.add(new MapGroup<>(name, ruleAny, ruleAll, columns, setter));
        }
    }
    
    /**
     * {@code (e, rs, i) -> { e.attribute = rs.get(i) }}
     * @param <Entity> 
     */
    interface Setter<Entity> {
        void set(Entity e, MiResultSet rs, int index) throws MiException;
    }
    
    /**
     * {@code (e, rs, i) -> { e.attribute(rs.get(i[0]), rs.get(i[1])) }}
     * @param <Entity> 
     */
    interface GroupSetter<Entity> {
        void set(Entity e, MiResultSet rs, int[] indices) throws MiException;
    }
}
