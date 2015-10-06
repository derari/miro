package org.cthul.miro.entity.base;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.base.ResultColumns.ColumnRule;
import org.cthul.miro.entity.base.AttributeMapping.GroupSetter;
import org.cthul.miro.entity.base.AttributeMapping.Setter;
import static org.cthul.miro.entity.base.ResultColumns.findColumns;

/**
 * Base class for configuring a mapping from result columns to entity attributes.
 * @param <Entity>
 * @param <E>
 * @param <This>
 */
public abstract class AttributeMappingBase<Entity, E extends Exception, This> {
    
    /**
     * Adds an entry to the mapping.
     * @param entry
     * @return this
     * @throws E exception 
     */
    protected abstract This add(MappingEntry<Entity> entry) throws E;
    
    /**
     * Adds required attribute.
     * @param column
     * @param setter
     * @return this
     * @throws E exception
     */
    public This required(String column, Setter<Entity> setter) throws E {
        return add(new MapSingle<>(ColumnRule.REQUIRED, column, setter));
    }
    
    /**
     * Adds optional attribute.
     * @param column
     * @param setter
     * @return this
     * @throws E exception
     */
    public This optional(String column, Setter<Entity> setter) throws E {
        return add(new MapSingle<>(ColumnRule.OPTIONAL, column, setter));
    }
    
    /**
     * Adds attribute with no default behavior.
     * Setter will be called with index -1 if column is not present.
     * @param column
     * @param setter
     * @return this
     * @throws E exception
     */
    public This attribute(String column, Setter<Entity> setter) throws E {
        return add(new MapSingle<>(ColumnRule.DEFAULT, column, setter));
    }
    
    /**
     * Adds required attributes.
     * @param columns
     * @param setter
     * @return this
     * @throws E exception
     */
    public This required(String[] columns, GroupSetter<Entity> setter) throws E {
        return add(new MapGroup<>(ColumnRule.REQUIRED, ColumnRule.REQUIRED, columns, setter));
    }
    
    /**
     * Adds optional attributes.
     * If only some attributes are missing, their indices are set to -1.
     * @param columns
     * @param setter
     * @return 
     * @throws E exception
     */
    public This optional(String[] columns, GroupSetter<Entity> setter) throws E {
        return add(new MapGroup<>(ColumnRule.OPTIONAL, ColumnRule.DEFAULT, columns, setter));
    }
    
    /**
     * Adds optional attributes.
     * If any attribute is missing, setter is not called.
     * @param columns
     * @param setter
     * @return this
     * @throws E exception
     */
    public This allOrNone(String[] columns, GroupSetter<Entity> setter) throws E {
        return add(new MapGroup<>(ColumnRule.OPTIONAL, ColumnRule.OPTIONAL, columns, setter));
    }
    
    /**
     * Requires that at least one column is present.
     * @param columns
     * @param setter
     * @return this
     * @throws E exception 
     */
    public This any(String[] columns, GroupSetter<Entity> setter) throws E {
        return add(new MapGroup<>(ColumnRule.REQUIRED, ColumnRule.DEFAULT, columns, setter));
    }
    
    /**
     * Adds attributes.
     * Setter will always be called, missing attributes will have index -1.
     * @param columns
     * @param setter
     * @return this
     * @throws E exception
     */
    public This attributes(String[] columns, GroupSetter<Entity> setter) throws E {
        return add(new MapGroup<>(ColumnRule.DEFAULT, ColumnRule.DEFAULT, columns, setter));
    }
    
    /**
     * Adds required attributes.
     * @param columns
     * @return group configuration
     * @throws E exception
     */
    public ConfigureGroup required(String... columns) throws E {
        return group(ColumnRule.REQUIRED, ColumnRule.REQUIRED, columns);
    }
    
    /**
     * Adds optional attributes.
     * If only some attributes are missing, their indices are set to -1.
     * @param columns
     * @return group configuration
     * @throws E exception
     */
    public ConfigureGroup optional(String... columns) throws E {
        return group(ColumnRule.OPTIONAL, ColumnRule.DEFAULT, columns);
    }
    
    /**
     * Adds optional attributes.
     * If any attribute is missing is not called.
     * @param columns
     * @return group configuration
     * @throws E exception
     */
    public ConfigureGroup allOrNone(String... columns) throws E {
        return group(ColumnRule.OPTIONAL, ColumnRule.OPTIONAL, columns);
    }
    
    /**
     * Requires that at least one column is present.
     * @param columns
     * @return group configuration
     * @throws E exception 
     */
    public ConfigureGroup any(String... columns) throws E {
        return group(ColumnRule.REQUIRED, ColumnRule.DEFAULT, columns);
    }
    
    /**
     * Adds attributes.
     * Setter will always be called, missing attributes will have index -1.
     * @param columns
     * @return group configuration
     * @throws E exception
     */
    public ConfigureGroup attributes(String... columns) throws E {
        return group(ColumnRule.DEFAULT, ColumnRule.DEFAULT, columns);
    }
    
    protected ConfigureGroup group(ColumnRule ruleAny, ColumnRule ruleAll, String[] columns) {
        return new ConfigureGroup(ruleAny, ruleAll, columns);
    }
    
    protected abstract static class MappingEntry<Entity> {

        public abstract ReaderEntry<Entity> newReader(MiResultSet resultSet) throws MiException;
    }
    
    protected static class MapSingle<Entity> extends MappingEntry<Entity> {
        
        private final ColumnRule rule;
        private final String column;
        private final Setter<Entity> setter;

        public MapSingle(ColumnRule rule, String column, Setter<Entity> setter) {
            this.rule = rule;
            this.column = column;
            this.setter = setter;
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
    
    protected static class MapGroup<Entity> extends MappingEntry<Entity> {
        
        private final ColumnRule allColumns;
        private final ColumnRule eachColumn;
        private final String[] columns;
        private final GroupSetter<Entity> setter;

        /**
         * 
         * @param allColumns rule is applied if all columns are missing
         * @param eachColumn rule is applied for each missing column
         * @param columns
         * @param setter 
         */
        public MapGroup(ColumnRule allColumns, ColumnRule eachColumn, String[] columns, GroupSetter<Entity> setter) {
            this.allColumns = allColumns;
            this.eachColumn = eachColumn;
            this.columns = columns;
            this.setter = setter;
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
    
    protected abstract static class ReaderEntry<Entity> {
        
        public abstract void apply(Entity e) throws MiException;
    }
    
    protected static class ReadSingle<Entity> extends ReaderEntry<Entity> {
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
    
    protected static class ReadGroup<Entity> extends ReaderEntry<Entity> {
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
    
    public class ConfigureGroup {
        
        private final ColumnRule ruleAny;
        private final ColumnRule ruleAll;
        private final String[] columns;

        protected ConfigureGroup(ColumnRule ruleAny, ColumnRule ruleAll, String[] columns) {
            this.ruleAny = ruleAny;
            this.ruleAll = ruleAll;
            this.columns = columns;
        }
        
        public This set(GroupSetter<Entity> setter) throws E {
            return add(new MapGroup<>(ruleAny, ruleAll, columns, setter));
        }
    }
}
