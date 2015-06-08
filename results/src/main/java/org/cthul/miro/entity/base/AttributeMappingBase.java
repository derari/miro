package org.cthul.miro.entity.base;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.base.AttributeMapping.ColumnRule;
import org.cthul.miro.entity.base.AttributeMapping.GroupSetter;
import org.cthul.miro.entity.base.AttributeMapping.Setter;
import static org.cthul.miro.entity.base.AttributeMapping.findColumns;

/**
 *
 * @param <Entity>
 * @param <E>
 * @param <This>
 */
public abstract class AttributeMappingBase<Entity, E extends Exception, This> {
    
    protected abstract This add(MappingEntry<Entity> entry) throws E;
    
    /**
     * Adds required attribute.
     * @param column
     * @param setter
     * @return this
     * @throws E
     */
    public This required(String column, Setter<Entity> setter) throws E {
        return add(new MapSingle<>(AttributeMapping.ColumnRule.REQUIRED, column, setter));
    }
    
    /**
     * Adds optional attribute.
     * @param column
     * @param setter
     * @return this
     * @throws E
     */
    public This optional(String column, Setter<Entity> setter) throws E {
        return add(new MapSingle<>(AttributeMapping.ColumnRule.OPTIONAL, column, setter));
    }
    
    /**
     * Adds attribute with no default behavior.
     * Setter will be called with index -1 if column is not present.
     * @param column
     * @param setter
     * @return this
     * @throws E
     */
    public This attribute(String column, Setter<Entity> setter) throws E {
        return add(new MapSingle<>(AttributeMapping.ColumnRule.DEFAULT, column, setter));
    }
    
    /**
     * Adds required attributes.
     * @param columns
     * @param setter
     * @return this
     * @throws E
     */
    public This required(String[] columns, GroupSetter<Entity> setter) throws E {
        return add(new MapGroup<>(AttributeMapping.ColumnRule.REQUIRED, AttributeMapping.ColumnRule.REQUIRED, columns, setter));
    }
    
    /**
     * Adds optional attributes.
     * If only some attributes are missing, their indices are set to -1.
     * @param columns
     * @param setter
     * @return 
     * @throws E
     */
    public This optional(String[] columns, GroupSetter<Entity> setter) throws E {
        return add(new MapGroup<>(AttributeMapping.ColumnRule.OPTIONAL, AttributeMapping.ColumnRule.DEFAULT, columns, setter));
    }
    
    /**
     * Adds optional attributes.
     * If any attribute is missing, setter is not called.
     * @param columns
     * @param setter
     * @return this
     * @throws E
     */
    public This allOrNone(String[] columns, GroupSetter<Entity> setter) throws E {
        return add(new MapGroup<>(AttributeMapping.ColumnRule.OPTIONAL, AttributeMapping.ColumnRule.OPTIONAL, columns, setter));
    }
    
    /**
     * Adds attributes.
     * Setter will always be called, missing attributes will have index -1.
     * @param columns
     * @param setter
     * @return 
     * @throws E
     */
    public This attributes(String[] columns, GroupSetter<Entity> setter) throws E {
        return add(new MapGroup<>(AttributeMapping.ColumnRule.DEFAULT, AttributeMapping.ColumnRule.DEFAULT, columns, setter));
    }
    
    /**
     * Adds required attributes.
     * @param columns
     * @return this
     * @throws E
     */
    public ConfigureGroup required(String... columns) throws E {
        return group(AttributeMapping.ColumnRule.REQUIRED, AttributeMapping.ColumnRule.REQUIRED, columns);
    }
    
    /**
     * Adds optional attributes.
     * If only some attributes are missing, their indices are set to -1.
     * @param columns
     * @return 
     * @throws E
     */
    public ConfigureGroup optional(String... columns) throws E {
        return group(AttributeMapping.ColumnRule.OPTIONAL, AttributeMapping.ColumnRule.DEFAULT, columns);
    }
    
    /**
     * Adds optional attributes.
     * If any attribute is missing is not called.
     * @param columns
     * @return this
     * @throws E
     */
    public ConfigureGroup allOrNone(String... columns) throws E {
        return group(AttributeMapping.ColumnRule.OPTIONAL, AttributeMapping.ColumnRule.OPTIONAL, columns);
    }
    
    /**
     * Adds attributes.
     * Setter will always be called, missing attributes will have index -1.
     * @param columns
     * @return 
     * @throws E
     */
    public ConfigureGroup attributes(String... columns) throws E {
        return group(AttributeMapping.ColumnRule.DEFAULT, AttributeMapping.ColumnRule.DEFAULT, columns);
    }
    
    protected ConfigureGroup group(ColumnRule ruleAny, ColumnRule ruleAll, String[] columns) {
        return new ConfigureGroup(ruleAny, ruleAll, columns);
    }
    
    protected abstract static class MappingEntry<Entity> {

        public abstract ReaderEntry<Entity> newReader(MiResultSet resultSet) throws MiException;
    }
    
    protected static class MapSingle<Entity> extends MappingEntry<Entity> {
        
        private final AttributeMapping.ColumnRule rule;
        private final String column;
        private final Setter<Entity> setter;

        public MapSingle(AttributeMapping.ColumnRule rule, String column, Setter<Entity> setter) {
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
        
        private final AttributeMapping.ColumnRule allColumns;
        private final AttributeMapping.ColumnRule eachColumn;
        private final String[] columns;
        private final GroupSetter<Entity> setter;

        public MapGroup(ColumnRule allColumns, ColumnRule eachColumn, String[] columns, GroupSetter<Entity> setter) {
            this.allColumns = allColumns;
            this.eachColumn = eachColumn;
            this.columns = columns;
            this.setter = setter;
        }

        @Override
        public ReaderEntry<Entity> newReader(MiResultSet resultSet) throws MiException {
            int[] indices;
            if (allColumns == AttributeMapping.ColumnRule.DEFAULT && eachColumn == AttributeMapping.ColumnRule.DEFAULT) {
                indices = findColumns(resultSet, columns);
            } else {
                indices = findColumns(allColumns, eachColumn, resultSet, columns);
                if (indices == null) return null;
            }
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
        
        private final AttributeMapping.ColumnRule ruleAny;
        private final AttributeMapping.ColumnRule ruleAll;
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
