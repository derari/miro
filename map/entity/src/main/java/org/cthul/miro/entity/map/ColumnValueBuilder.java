package org.cthul.miro.entity.map;

import org.cthul.miro.util.XBiFunction;
import org.cthul.miro.util.XFunction;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.base.ResultColumns;
import org.cthul.miro.entity.base.ResultColumns.ColumnRule;
import org.cthul.miro.util.Closables.FunctionalHelper;

/**
 *
 * @param <Entity>
 * @param <S>
 * @param <G>
 */
public interface ColumnValueBuilder<Entity, S extends ColumnValueBuilder.Single<Entity, S>, G extends ColumnValueBuilder.Group<Entity, G>> {
    
    S column(ColumnRule rule, String column);
    
    G columns(ColumnRule allRule, ColumnRule eachRule, String... columns);
    
    /**
     * Adds attribute with required column.
     * @param column
     * @return single
     */
    default S require(String column) {
        return ColumnValueBuilder.this.column(ColumnRule.REQUIRED, column);
    }

    /**
     * Adds attribute with optional column.
     * @param column
     * @return single
     */
    default S optional(String column) {
        return ColumnValueBuilder.this.column(ColumnRule.OPTIONAL, column);
    }

    /**
     * Adds attribute with no default behavior.
     * Setter will be called with index -1 if column is not present.
     * @param column
     * @return single
     */
    default S column(String column) {
        return ColumnValueBuilder.this.column(ColumnRule.DEFAULT, column);
    }

    /**
     * Adds attribute with required columns.
     * @param columns
     * @return group
     */
    default G required(String... columns) {
        return ColumnValueBuilder.this.columns(ColumnRule.REQUIRED, ColumnRule.REQUIRED, columns);
    }

    /**
     * Adds attribute with optional columns.
     * If only some columns are missing, their indices are set to -1.
     * @param columns
     * @return group
     */
    default G optional(String... columns) {
        return ColumnValueBuilder.this.columns(ColumnRule.OPTIONAL, ColumnRule.DEFAULT, columns);
    }

    /**
     * Adds attribute with optional columns.
     * If any column is missing, setter is not called.
     * @param columns
     * @return group
     */
    default G allOrNone(String... columns) {
        return ColumnValueBuilder.this.columns(ColumnRule.OPTIONAL, ColumnRule.OPTIONAL, columns);
    }

    /**
     * Adds attribute that requires at least one column.
     * @param columns
     * @return group
     */
    default G any(String... columns) {
        return ColumnValueBuilder.this.columns(ColumnRule.REQUIRED, ColumnRule.DEFAULT, columns);
    }

    /**
     * Adds attributes.
     * Setter will always be called, missing columns will have index -1.
     * @param columns
     * @return group
     */
    default G columns(String... columns) {
        return ColumnValueBuilder.this.columns(ColumnRule.DEFAULT, ColumnRule.DEFAULT, columns);
    }
    
    public abstract class Single<Entity, S extends Single<Entity, S>> {
        
        protected final String column;
        protected final ColumnRule rule;
        protected MultiValue multi = null;
        protected XFunction<Object, Object, MiException> toValue = null;
        protected Function<Object, Object> toColumn = null;

        public Single(String column, ColumnRule rule) {
            this.column = column;
            this.rule = rule;
        }
        
        public S mapToValue(XFunction<Object, Object, MiException> toValue) {
            this.toValue = toValue;
            multi = null;
            return (S) this;
        }
        
        public S mapToColumn(Function<Object, Object> toColumn) {
            this.toColumn = toColumn;
            multi = null;
            return (S) this;
        }
        
        public S map(MultiValue multi) {
            this.multi = multi;
            return (S) this;
        }
        
        protected ColumnValue buildColumnValue() {
            if (multi == null && (toValue != null || toColumn != null)) {
                if (toValue == null) toValue = IDENTITY;
                if (toColumn == null) toColumn = Function.identity();
                multi = new MultiValue() {
                    @Override
                    public Object join(Object[] values) throws MiException {
                        return toValue.apply(values[0]);
                    }
                    @Override
                    public Object[] split(Object value, Object[] result) {
                        result = withLength(result, 1);
                        result[0] = toColumn.apply(value);
                        return result;
                    }
                };
            }
            class SingleColumn implements ColumnValue {
                final List<String> columns = Arrays.asList(column);
                final ColumnRule rule = Single.this.rule;
                final MultiValue multi = Single.this.multi;
                @Override
                public List<String> getColumns() {
                    return columns;
                }
                @Override
                public Object[] toColumns(Object value, Object[] result) {
                    return multi.split(value, result);
                }
                @Override
                public EntityFactory<?> newValueReader(MiResultSet rs) throws MiException {
                    return ResultColumns.readColumn(rs, rule, columns.get(0), multi);
                }
                @Override
                public String toString() {
                    return columns.toString();
                }
            }
            return new SingleColumn();
        }
    }
    
    public abstract class Group<Entity, G extends Group<Entity, G>> {
        
        protected final String[] columns;
        protected final ColumnRule allRule, eachRule;
        protected ColumnValue columnValue = null;
        protected MultiValue multi = null;
        protected XFunction<Object[], ?, MiException> toValue = null;
        protected BiFunction<Object, Object[], Object[]> toColumn = null;
        protected XBiFunction<MiResultSet, int[], ?, MiException> reader = null;

        public Group(String[] columns, ColumnRule allRule, ColumnRule eachRule) {
            this.columns = columns;
            this.allRule = allRule;
            this.eachRule = eachRule;
        }
        
        public G mapToValue(XFunction<Object[], ?, MiException> toValue) {
            this.toValue = toValue;
            multi = null;
            return (G) this;
        }
        
        public G mapToColumns(BiFunction<?, Object[], Object[]> toColumn) {
            this.toColumn = (BiFunction) toColumn;
            multi = null;
            return (G) this;
        }
        
        public G readAs(XBiFunction<MiResultSet, int[], ?, MiException> reader) {
            this.reader = reader;
            return (G) this;
        }
        
        public G map(MultiValue multi) {
            this.multi = multi;
            return (G) this;
        }
        
        protected ColumnValue buildColumnValue() {
            if (columnValue != null) return columnValue;
            if (multi == null) {
                if (toValue == null && reader == null) {
                    toValue = a -> { throw new MiException("Dematerialization only"); };
                }
                if (toColumn == null) {
                    toColumn = (a, b) -> { throw new UnsupportedOperationException("Materialization only"); };
                }
                multi = new MultiValue() {
                    @Override
                    public Object join(Object[] values) throws MiException {
                        return toValue.apply(values);
                    }
                    @Override
                    public Object[] split(Object value, Object[] result) {
                        return toColumn.apply(value, result);
                    }
                };
            }
            return columnValue = new MultiColumn(columns, allRule, eachRule, multi, reader);
        }
    }
    
    static class MultiColumn implements ColumnValue {
        final String[] columns;
        final List<String> columnList;
        final ColumnRule allRule, eachRule;
        final MultiValue multi;
        final XBiFunction<MiResultSet, int[], ?, MiException> reader;

        public MultiColumn(String[] columns, ColumnRule allRule, ColumnRule eachRule, MultiValue multi, XBiFunction<MiResultSet, int[], ?, MiException> reader) {
            this.columns = columns;
            this.columnList = Arrays.asList(columns);
            this.allRule = allRule;
            this.eachRule = eachRule;
            this.multi = multi;
            class MultiReader implements XBiFunction<MiResultSet, int[], Object, MiException> {
                Object[] bag;
                @Override
                public Object apply(MiResultSet rs, int[] i) throws MiException {
                    bag = ResultColumns.readColumns(rs, i, bag);
                    return multi.join(bag);
                }
            }
            this.reader = reader != null ? reader : new MultiReader();
        }
        
        @Override
        public List<String> getColumns() {
            return columnList;
        }
        @Override
        public Object[] toColumns(Object value, Object[] result) {
            return multi.split(value, result);
        }
        @Override
        public EntityFactory<?> newValueReader(MiResultSet rs) throws MiException {
            int[] indices = ResultColumns.findColumns(allRule, eachRule, rs, columns);
            if (indices == null) return null;
            return () -> reader.apply(rs, indices);
        }
        @Override
        public String toString() {
            return columnList.toString();
        }
    }
    
    static interface MultiValue extends FunctionalHelper {
        
        Object join(Object[] values) throws MiException;
        
        Object[] split(Object value, Object[] result);
    }
    
    static final XFunction<Object, Object, MiException> IDENTITY = x -> x;
    
    public static final XFunction<Object, Object, MiException> TO_BOOL = o -> 
            Objects.equals(o, true) || 
            (o instanceof Number && ((Number) o).intValue() == 1);
    
    public static final XFunction<Object, Object, MiException> TO_BOOLEAN = o ->
            o == null ? null : TO_BOOL.apply(o);
}
