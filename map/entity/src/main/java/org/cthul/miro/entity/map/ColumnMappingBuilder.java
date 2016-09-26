package org.cthul.miro.entity.map;

import java.util.ArrayList;
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
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.entity.base.ResultColumns;
import org.cthul.miro.entity.base.ResultColumns.ColumnMatcher;
import org.cthul.miro.entity.base.ResultColumns.ColumnRule;
import org.cthul.miro.util.Closeables.FunctionalHelper;

/**
 *
 * @param <Entity>
 * @param <Cnn>
 * @param <S>
 * @param <G>
 */
public interface ColumnMappingBuilder<Entity, Cnn, S extends ColumnMappingBuilder.Single<Entity, Cnn, S>, G extends ColumnMappingBuilder.Group<Entity, Cnn, G>> {
    
    S column(ColumnRule rule, String column);
    
    S column(ColumnMatcher column);
    
    G columns(ColumnRule allRule, ColumnRule eachRule, String... columns);
    
    /**
     * Adds attribute with required column.
     * @param column
     * @return single
     */
    default S require(String column) {
        return ColumnMappingBuilder.this.column(ColumnRule.REQUIRED, column);
    }

    /**
     * Adds attribute with optional column.
     * @param column
     * @return single
     */
    default S optional(String column) {
        return ColumnMappingBuilder.this.column(ColumnRule.OPTIONAL, column);
    }

    /**
     * Adds attribute with no default behavior.
     * Setter will be called with index -1 if column is not present.
     * @param column
     * @return single
     */
    default S column(String column) {
        return ColumnMappingBuilder.this.column(ColumnRule.DEFAULT, column);
    }

    /**
     * Adds attribute with required columns.
     * @param columns
     * @return group
     */
    default G required(String... columns) {
        return ColumnMappingBuilder.this.columns(ColumnRule.REQUIRED, ColumnRule.REQUIRED, columns);
    }

    /**
     * Adds attribute with optional columns.
     * If only some columns are missing, their indices are set to -1.
     * @param columns
     * @return group
     */
    default G optional(String... columns) {
        return ColumnMappingBuilder.this.columns(ColumnRule.OPTIONAL, ColumnRule.DEFAULT, columns);
    }

    /**
     * Adds attribute with optional columns.
     * If any column is missing, setter is not called.
     * @param columns
     * @return group
     */
    default G allOrNone(String... columns) {
        return ColumnMappingBuilder.this.columns(ColumnRule.OPTIONAL, ColumnRule.OPTIONAL, columns);
    }

    /**
     * Adds attribute that requires at least one column.
     * @param columns
     * @return group
     */
    default G any(String... columns) {
        return ColumnMappingBuilder.this.columns(ColumnRule.REQUIRED, ColumnRule.DEFAULT, columns);
    }

    /**
     * Adds attributes.
     * Setter will always be called, missing columns will have index -1.
     * @param columns
     * @return group
     */
    default G columns(String... columns) {
        return ColumnMappingBuilder.this.columns(ColumnRule.DEFAULT, ColumnRule.DEFAULT, columns);
    }
    
    public abstract class Single<Entity, Cnn, S extends Single<Entity, Cnn, S>> {
        
        protected final ColumnMatcher column;
        protected final List<String> columns = new ArrayList<>();
        protected MultiValue multi = null;
        protected Function<Object, Object> toColumn = null;
        protected XFunction<Object, Object, MiException> toValue = null;
        protected XBiFunction<MiResultSet, Integer, ?, MiException> reader = null;
        protected XBiFunction<MiResultSet, Cnn, ? extends EntityFactory<?>, MiException> newReader = null;

        public Single(ColumnMatcher column) {
            this.column = column;
        }
        
        public S addColumns(String... columns) {
            return addColumns(Arrays.asList(columns));
        }
        
        public S addColumns(List<String> columns) {
            this.columns.addAll(columns);
            return (S) this;
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
        
        public S readAs(XBiFunction<MiResultSet, Integer, ?, MiException> reader) {
            this.reader = reader;
            this.newReader = null;
            return (S) this;
        }
        
        public S readWith(XFunction<MiResultSet, ? extends EntityFactory<?>, MiException> newReader) {
            return readWith((rs, cnn) -> newReader.apply(rs));
        }
        
        public S readWith(XBiFunction<MiResultSet, Cnn, ? extends EntityFactory<?>, MiException> newReader) {
            this.reader = null;
            this.newReader = newReader;
            return (S) this;
        }
        
        protected ColumnMapping buildColumnValue() {
            XBiFunction<MiResultSet, Cnn, ? extends EntityFactory<?>, MiException> fNewReader;
            final BiFunction<Object, Object[], Object[]> fToCol;
            if (newReader != null || reader != null) {
                if (toValue != null) throw new IllegalArgumentException(
                        "Can't use mapToValue when reader is given");
                if (multi != null) throw new IllegalArgumentException(
                        "Can't use map when reader is given");
            }
            if (newReader != null) {
                fNewReader = (rs, cnn) -> {
                    if (column.find(rs) == null) return null;
                    return newReader.apply(rs, cnn);
                };
            } else if (reader != null) {
                fNewReader = (rs, cnn) -> {
                    Integer i = column.find(rs);
                    if (i == null) return null;
                    return () -> reader.apply(rs, i);
                };
            } else if (toValue != null) {
                fNewReader = (rs, cnn) -> ResultColumns.readColumn(rs, column, multi).andThen(toValue);
            } else {
                fNewReader = (rs, cnn) -> ResultColumns.readColumn(rs, column, multi);
            }
            if (toColumn != null) {
                fToCol = (v,r) -> {
                    if (r == null || r.length != 1) r = new Object[1];
                    r[0] = toColumn.apply(v);
                    return r;
                };
            } else if (multi != null) {
                fToCol = multi::split;
            } else {
                fToCol = (v,r) -> { 
                    if (r == null || r.length != 1) r = new Object[1];
                    r[0] = v; 
                    return r;
                };
            }
            XFunction<MiResultSet, Boolean, MiException> matcher = rs -> column.test(rs) != null;
            return new SimpleColumnValue<>(matcher, columns, fToCol, fNewReader);
        }
    }
    
    public abstract class Group<Entity, Cnn, G extends Group<Entity, Cnn, G>> {
        
        protected final String[] columns;
        protected final ColumnRule allRule, eachRule;
        protected ColumnMapping columnValue = null;
        protected MultiValue multi = null;
        protected XFunction<Object[], ?, MiException> toValue = null;
        protected BiFunction<Object, Object[], Object[]> toColumn = null;
        protected XBiFunction<MiResultSet, int[], ?, MiException> reader = null;
        protected XBiFunction<MiResultSet, Cnn, ? extends EntityFactory<?>, MiException> newReader = null;

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
        
        public G map(MultiValue multi) {
            this.multi = multi;
            return (G) this;
        }
        
        public G readAs(XBiFunction<MiResultSet, int[], ?, MiException> reader) {
            this.reader = reader;
            this.newReader = null;
            return (G) this;
        }
        
        public G readWith(XFunction<MiResultSet, ? extends EntityFactory<?>, MiException> newReader) {
            return readWith((rs, c) -> newReader.apply(rs));
        }
        
        public G readWith(XBiFunction<MiResultSet, Cnn, ? extends EntityFactory<?>, MiException> newReader) {
            this.reader = null;
            this.newReader = newReader;
            return (G) this;
        }
        
        protected ColumnMapping buildColumnValue() {
            if (columnValue != null) return columnValue;
            XBiFunction<MiResultSet, Cnn, ? extends EntityFactory<?>, MiException> fNewReader;
            final BiFunction<Object, Object[], Object[]> fToCol;
            if (newReader != null || reader != null) {
                if (toValue != null) throw new IllegalArgumentException(
                        "Can't use mapToValue when reader is given");
                if (multi != null) throw new IllegalArgumentException(
                        "Can't use map when reader is given");
            }
            if (newReader != null) {
                fNewReader = (rs, cnn) -> {
                    if (ResultColumns.findColumns(allRule, eachRule, rs, columns) == null) return null;
                    return newReader.apply(rs, cnn);
                };
            } else if (reader != null) {
                fNewReader = (rs, cnn) -> {
                    int[] i = ResultColumns.findColumns(allRule, eachRule, rs, columns);
                    if (i == null) return null;
                    return () -> reader.apply(rs, i);
                };
            } else if (toValue != null || multi != null) {
                if (toValue == null) toValue = multi::join;
                fNewReader = (rs, cnn) -> {
                    int[] i = ResultColumns.findColumns(allRule, eachRule, rs, columns);
                    if (i == null) return null;
                    Object[] result = new Object[i.length];
                    return () -> toValue.apply(ResultColumns.readColumns(rs, i, result));
                };
            } else {
                fNewReader = (rs, cnn) -> { throw new UnsupportedOperationException("Dematerialization only"); };
            }
            if (toColumn != null) {
                fToCol = toColumn;
            } else if (multi != null) {
                fToCol = multi::split;
            } else {
                fToCol = (v,r) -> { throw new UnsupportedOperationException("Materialization only"); };
            }
            XFunction<MiResultSet, Boolean, MiException> matcher = rs -> {
                try {
                    return ResultColumns.findColumns(allRule, eachRule, rs, columns) != null;
                } catch (MiException e) {
                    return false;
                }
            };
            return new SimpleColumnValue(matcher, Arrays.asList(columns), fToCol, fNewReader);
        }
    }
    
    static class SimpleColumnValue<Cnn> implements ColumnMapping<Cnn> {
        final XFunction<MiResultSet, Boolean, MiException> matcher;
        final List<String> columns;
        final BiFunction<Object, Object[], Object[]> toColumn;
        final XBiFunction<MiResultSet, Cnn, ? extends EntityFactory<?>, MiException> newReader;

        public SimpleColumnValue(XFunction<MiResultSet, Boolean, MiException> matcher, List<String> columns, BiFunction<Object, Object[], Object[]> toColumn, XBiFunction<MiResultSet, Cnn, ? extends EntityFactory<?>, MiException> newReader) {
            this.matcher = matcher;
            this.columns = columns;
            this.toColumn = toColumn;
            this.newReader = newReader;
        }

        @Override
        public List<String> getColumns() {
            return columns;
        }

        @Override
        public Object[] toColumns(Object value, Object[] result) {
            return toColumn.apply(value, result);
        }

        @Override
        public boolean accept(MiResultSet rs, Cnn cnn) throws MiException {
            return matcher.apply(rs);
        }

        @Override
        public EntityFactory<?> newValueReader(MiResultSet rs, Cnn cnn) throws MiException {
            return newReader.apply(rs, cnn);
        }

        @Override
        public String toString() {
            return String.valueOf(columns);
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
    
    public static <E> XFunction<MiResultSet, EntityFactory<List<E>>, MiException> listReader(String parent, String subresult, EntityType<E> type) {
        return rs -> {
            MiResultSet r = subresult != null ? rs.subResult(subresult) : rs;
            int parentIndex = rs.findColumn(parent);
            return listReader(parentIndex, r, type);
        };
    }
    
    public static <E> EntityFactory<List<E>> listReader(int parentIndex, MiResultSet resultSet, EntityType<E> type) throws MiException {
        return listReader(parentIndex, resultSet, type.newFactory(resultSet));
    }
    
    public static <E> EntityFactory<List<E>> listReader(int parentIndex, MiResultSet resultSet, EntityFactory<E> factory) {
        return EntityTypes.buildFactory(b -> b
                .setFactory(ArrayList::new)
                .addInitializer(list -> {
                    Object parentId = resultSet.get(parentIndex);
                    while (resultSet.get(parentIndex).equals(parentId)) {
                        list.add(factory.newEntity());
                        if (!resultSet.next()) break;
                    }
                    resultSet.previous();
                })
                .addCompleteAndClose(factory)
                .addName("List<"+factory+">"));
    }
}
