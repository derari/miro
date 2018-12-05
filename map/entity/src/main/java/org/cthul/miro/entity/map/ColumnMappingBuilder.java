package org.cthul.miro.entity.map;

import java.util.*;
import java.util.function.BiConsumer;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.util.XBiFunction;
import org.cthul.miro.util.XFunction;
import java.util.function.Function;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.entity.*;
import org.cthul.miro.entity.map.ResultColumns.ColumnMatcher;
import org.cthul.miro.entity.map.ResultColumns.ColumnRule;
import org.cthul.miro.entity.map.ResultColumns.ColumnsMatcher;
import org.cthul.miro.util.*;
import org.cthul.miro.util.Closeables.FunctionalHelper;

/**
 *
 * @param <Entity>
 * @param <S>
 * @param <G>
 */
public interface ColumnMappingBuilder<Entity, S extends ColumnMappingBuilder.Single<Entity, ?>, G extends ColumnMappingBuilder.Group<Entity, ?>> {
    
    S column(ColumnRule rule, String column);
    
    S column(ColumnMatcher column);
    
    G columns(ColumnRule allRule, ColumnRule eachRule, String... columns);
    
    G columns(ColumnsMatcher matcher, String[] columns);
    
    default G columns(ColumnsMatcher matcher, Collection<String> columns) {
        return columns(matcher, columns.toArray(new String[columns.size()]));
    }
    
    /**
     * Adds attribute with requiredColumns columnMatcher.
     * @param column
     * @return single
     */
    default S requiredColumn(String column) {
        return ColumnMappingBuilder.this.column(ColumnRule.REQUIRED, column);
    }

    /**
     * Adds attribute with optionalColumn columnMatcher.
     * @param column
     * @return single
     */
    default S optionalColumn(String column) {
        return ColumnMappingBuilder.this.column(ColumnRule.OPTIONAL, column);
    }

    /**
     * Adds attribute with no default behavior.
     * Setter will be called with index -1 if columnMatcher is not present.
     * @param column
     * @return single
     */
    default S column(String column) {
        return ColumnMappingBuilder.this.column(ColumnRule.DEFAULT, column);
    }

    /**
     * Adds attribute with requiredColumns columns.
     * @param columns
     * @return group
     */
    default G requiredColumns(String... columns) {
        return ColumnMappingBuilder.this.columns(ColumnRule.REQUIRED, ColumnRule.REQUIRED, columns);
    }

    /**
     * Adds attribute with optionalColumn columns.
     * If only some columns are missing, their indices are set to -1.
     * @param columns
     * @return group
     */
    default G optionalColumns(String... columns) {
        return ColumnMappingBuilder.this.columns(ColumnRule.OPTIONAL, ColumnRule.DEFAULT, columns);
    }

    /**
     * Adds attribute with optionalColumn columns.
     * If anyColumn columnMatcher is missing, setter is not called.
     * @param columns
     * @return group
     */
    default G allOrNoneColumns(String... columns) {
        return ColumnMappingBuilder.this.columns(ColumnRule.OPTIONAL, ColumnRule.OPTIONAL, columns);
    }

    /**
     * Adds attribute that requires at least one columnMatcher.
     * @param columns
     * @return group
     */
    default G anyColumn(String... columns) {
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
    
    public class Single<Entity, S extends Single<Entity, S>> {
        
        protected final ColumnMatcher columnMatcher;
        protected String column = null;
        protected Function<Object, Object> toColumn = IDENTITY;
        protected XFunction<Object, Object, MiException> toValue = XIDENTITY;
        protected XBiFunction<MiResultSet, Integer, ?, MiException> reader = null;
        protected XBiConsumer<MiResultSet, FactoryBuilder<Object>, MiException> newReader2 = null;
        protected XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> newReader3 = null;
        protected Function<Collection<?>, ColumnMapping> nestedBuilder = null;

        public Single(ColumnMatcher column) {
            this.columnMatcher = column;
        }
        
        public S setColumn(String column) {
            this.column = column;
            return (S) this;
        }
        
        public S mapToValue(XFunction<Object, Object, MiException> toValue) {
            this.toValue = toValue;
            return (S) this;
        }
        
        public S mapToColumn(Function<Object, Object> toColumn) {
            this.toColumn = toColumn;
            return (S) this;
        }
        
        public S read(XBiFunction<MiResultSet, Integer, ?, MiException> reader) {
            this.reader = reader;
            this.newReader2 = null;
            this.newReader3 = null;
            return (S) this;
        }
        
        public S readWith(XBiConsumer<MiResultSet, FactoryBuilder<Object>, MiException> newReader) {
            this.reader = null;
            this.newReader2 = newReader;
            this.newReader3 = null;
            return (S) this;
        }
        
        public S readWith(XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> newReader) {
            this.reader = null;
            this.newReader2 = null;
            this.newReader3 = newReader;
            return (S) this;
        }

        public S nested(Function<Collection<?>, ColumnMapping> nestedBuilder) {
            this.nestedBuilder = nestedBuilder;
            return (S) this;
        }
        
        public S nested(BiConsumer<Collection<?>, ColumnMappingBuilder<Entity, Single<Entity,?>, Group<Entity,?>>> nested) {
            return nested(a -> {
                SimpleBuilder<Entity> sb = new SimpleBuilder<>();
                nested.accept(a, sb);
                return sb.getColumnMapping();
            });
        }
        
        protected ColumnMapping buildColumnValue() {
            if (newReader3 != null || newReader2 != null || reader != null) {
                if (toValue != XIDENTITY) throw new IllegalArgumentException(
                        "Can't use mapToValue when reader is given");
            }
            XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> fNewReader = buildNewReader();
            XTriFunction<Object, Integer, Object[], Object[], RuntimeException> fToCol = buildToColumn();
            XFunction<MiResultSet, Boolean, MiException> matcher = buildMatcher();
            Function<Collection<?>, ColumnMapping> nested = buildNested();
            return new SimpleColumnValue(matcher, new ReadOnlyArrayList<>(column), fToCol, fNewReader, nested);
        }

        protected XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> buildNewReader() {
            if (newReader3 != null) {
                return (rep, rs, fb) -> {
                    if (columnMatcher.find(rs) == null) return;
                    newReader3.accept(rep, rs, fb);
                };
            } else if (newReader2 != null) {
                return (rep, rs, fb) -> {
                    if (columnMatcher.find(rs) == null) return;
                    newReader2.accept(rs, fb);
                };
            } else if (reader != null) {
                return (rep, rs, fb) -> {
                    Integer i = columnMatcher.find(rs);
                    if (i == null) return;
                    fb.setFactory(() -> reader.apply(rs, i))
                            .addName(columnMatcher + "(#" + i + ")");
                };
            } else {
                return (rep, rs, fb) -> {
                    Integer i = columnMatcher.find(rs);
                    if (i == null) return;
                    fb.setFactory(() -> toValue.apply(rs.get(i)))
                            .addName(columnMatcher + "(#" + i + ")");
                };
            }
        }

        protected XTriFunction<Object, Integer, Object[], Object[], RuntimeException> buildToColumn() {
            return (v,i,a) -> {
                if (a == null) a = new Object[i+1];
                if (a.length <= i) a = Arrays.copyOf(a, i+1);
                a[i] = toColumn.apply(v);
                return a;
            };
        }

        protected XFunction<MiResultSet, Boolean, MiException> buildMatcher() {
            return rs -> columnMatcher.test(rs) != null;
        }

        protected Function<Collection<?>, ColumnMapping> buildNested() {
            if (nestedBuilder != null) return nestedBuilder;
            return null;
        }
    }
    
    public class Group<Entity, G extends Group<Entity, G>> {
        
        protected final String[] columns;
        protected final ColumnsMatcher matcher;
        protected MultiValue multi = null;
        protected XFunction<Object[], ?, MiException> toValue = null;
        protected XTriFunction<Object, Integer, Object[], Object[], RuntimeException> toColumn = null;
        protected XBiFunction<MiResultSet, int[], ?, MiException> reader = null;
        protected XBiConsumer<MiResultSet, FactoryBuilder<Object>, MiException> newReader2 = null;
        protected XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> newReader3 = null;
        protected Function<Collection<?>, ColumnMapping> nestedBuilder = null;

        public Group(String[] columns, ColumnRule allRule, ColumnRule eachRule) {
            this(columns, ResultColumns.match(allRule, eachRule, columns));
//            this.columns = columns;
//            this.allRule = allRule;
//            this.eachRule = eachRule;
        }

        public Group(String[] columns, ColumnsMatcher matcher) {
            this.columns = columns;
            this.matcher = matcher;
        }
        
        public G mapToValue(XFunction<Object[], ?, MiException> toValue) {
            this.toValue = toValue;
            multi = null;
            return (G) this;
        }
//        
//        public G mapToColumns(BiFunction<?, Object[], Object[]> toColumn) {
//            this.toColumn = (BiFunction) toColumn;
//            multi = null;
//            return (G) this;
//        }
        
        public G mapToColumns(XTriFunction<Object, Integer, Object[], Object[], RuntimeException> toColumn) {
            this.toColumn = toColumn;
            multi = null;
            return (G) this;
        }
        
        public G map(MultiValue multi) {
            this.multi = multi;
            return (G) this;
        }
        
        public G read(XBiFunction<MiResultSet, int[], ?, MiException> reader) {
            this.reader = reader;
            this.newReader2 = null;
            this.newReader3 = null;
            return (G) this;
        }
        
        public G readWith(XBiConsumer<MiResultSet, FactoryBuilder<Object>, MiException> newReader) {
            this.reader = null;
            this.newReader2 = newReader;
            this.newReader3 = null;
            return (G) this;
        }
        
        public G readWith(XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> newReader) {
            this.reader = null;
            this.newReader2 = null;
            this.newReader3 = newReader;
            return (G) this;
        }
        
        public G nested(Function<Collection<?>, ColumnMapping> nestedBuilder) {
            this.nestedBuilder = nestedBuilder;
            return (G) this;
        }
        
        public G nested(BiConsumer<Collection<?>, ColumnMappingBuilder<Entity, Single<Entity,?>, Group<Entity,?>>> nested) {
            return nested(a -> {
                SimpleBuilder<Entity> sb = new SimpleBuilder<>();
                nested.accept(a, sb);
                return sb.getColumnMapping();
            });
        }
        
        protected ColumnMapping buildColumnValue() {
            if (newReader3 != null || newReader2 != null || reader != null) {
                if (toValue != null) throw new IllegalArgumentException(
                        "Can't use mapToValue when reader is given");
                if (multi != null) throw new IllegalArgumentException(
                        "Can't use map when reader is given");
            }
            XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> fNewReader = buildNewReader();
            XTriFunction<Object, Integer, Object[], Object[], RuntimeException> fToCol = buildToColumn();
            XFunction<MiResultSet, Boolean, MiException> accept = rs -> matcher.test(rs) != null;
            return new SimpleColumnValue(accept, new ReadOnlyArrayList<>(columns), fToCol, fNewReader, nestedBuilder);
        }

        protected XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> buildNewReader() {
            if (newReader3 != null) {
                return (rep,rs,fb) -> {
                    if (matcher.find(rs) == null) return;
                    newReader3.accept(rep, rs, fb);
                };
            } else if (newReader2 != null) {
                return (rep,rs,fb) -> {
                    if (matcher.find(rs) == null) return;
                    newReader2.accept(rs, fb);
                };
            } else if (reader != null) {
                return (rep,rs,fb) -> {
                    int[] i = matcher.find(rs);
                    if (i == null) return;
                    fb.setFactory(() -> reader.apply(rs, i))
                            .addName(Arrays.toString(i));
                };
            } else if (toValue != null || multi != null) {
                if (toValue == null) toValue = multi::join;
                return (rep,rs,fb) -> {
                    int[] i = matcher.find(rs);
                    if (i == null) return;
                    Object[] result = new Object[i.length];
                    fb.setFactory(() -> toValue.apply(ResultColumns.readColumns(rs, i, result)))
                            .addName(Arrays.toString(i));
                };
            } else {
                return (rep,rs,fb) -> { throw new UnsupportedOperationException("Dematerialization only"); };
            }
        }

        protected XTriFunction<Object, Integer, Object[], Object[], RuntimeException> buildToColumn() {
            if (toColumn != null) {
                return toColumn;
            } else if (multi != null) {
                return multi::split;
            } else {
                return (v,i,r) -> { throw new UnsupportedOperationException("Materialization only"); };
            }
        }
    }
    
    static class SimpleBuilder<Entity> implements ColumnMappingBuilder<Entity, Single<Entity, ?>, Group<Entity, ?>> {
        
        private Single<Entity, ?> single;
        private Group<Entity, ?> group;

        @Override
        public Single<Entity, ?> column(ColumnRule rule, String column) {
            return column(ResultColumns.match(rule, column)).setColumn(column);
        }

        @Override
        public Single<Entity, ?> column(ColumnMatcher column) {
            return single = new Single<>(column);
        }

        @Override
        public Group<Entity, ?> columns(ColumnRule allRule, ColumnRule eachRule, String... columns) {
            return group = new Group<>(columns, allRule, eachRule);
        }

        @Override
        public Group<Entity, ?> columns(ColumnsMatcher matcher, String[] columns) {
            return group = new Group<>(columns, matcher);
        }
        
        public ColumnMapping getColumnMapping() {
            if (single != null) {
                return single.buildColumnValue();
            } else if (group != null) {
                return group.buildColumnValue();
            } else {
                throw new IllegalStateException("No mapping built.");
            }
        }
    }
    
    static class SimpleColumnValue implements ColumnMapping {
        final XFunction<MiResultSet, Boolean, MiException> matcher;
        final List<String> columns;
        final XTriFunction<Object, Integer, Object[], Object[], RuntimeException> toColumn;
        final XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> newReader;
        final Function<Collection<?>, ColumnMapping> nestedBuilder;

        public SimpleColumnValue(XFunction<MiResultSet, Boolean, MiException> matcher, List<String> columns, XTriFunction<Object, Integer, Object[], Object[], RuntimeException> toColumn, XTriConsumer<Repository, MiResultSet, FactoryBuilder<Object>, MiException> newReader, Function<Collection<?>, ColumnMapping> nestedBuilder) {
            this.matcher = matcher;
            this.columns = columns;
            this.toColumn = toColumn;
            this.newReader = newReader;
            if (nestedBuilder != null) {
                this.nestedBuilder = nestedBuilder;
            } else {
                this.nestedBuilder =  a -> {
                    if (a.size() > 1 || (a.size() == 1 && !"*".equals(String.valueOf(a.iterator().next())))) {
                        throw new UnsupportedOperationException("Nested: " + a);
                    }
                    return this;
                };
            }
        }

        @Override
        public List<String> getColumns() {
            return columns;
        }

        @Override
        public Object[] writeColumns(Object value, int index, Object[] target) {
            return toColumn.apply(value, index, target);
        }

        @Override
        public boolean accept(MiResultSet resultSet) throws MiException {
            return matcher.apply(resultSet);
        }

        @Override
        public void newValueReader(Repository repository, MiResultSet resultSet, FactoryBuilder<Object> factoryBuilder) throws MiException {
            newReader.accept(repository, resultSet, factoryBuilder);
        }

        @Override
        public String toString() {
            return String.valueOf(columns);
        }

        @Override
        public ColumnMapping nested(Collection<?> attributes) {
            class Nested implements ColumnMapping {
                ColumnMapping actual = null;
                ColumnMapping actual() {
                    if (actual != null) return actual;
                    return actual = nestedBuilder.apply(attributes);
                }
                @Override
                public List<String> getColumns() {
                    return actual().getColumns();
                }
                @Override
                public Object[] writeColumns(Object value, int index, Object[] target) {
                    return actual().writeColumns(value, index, target);
                }
                @Override
                public boolean accept(MiResultSet resultSet) throws MiException {
                    return actual().accept(resultSet);
                }
                @Override
                public void newValueReader(Repository repository, MiResultSet resultSet, FactoryBuilder<Object> factoryBuilder) throws MiException {
                    actual().newValueReader(repository, resultSet, factoryBuilder);
                }
                @Override
                public ColumnMapping nested(Collection<?> attributes2) {
                    List<Object> all = new ArrayList<>(attributes.size() + attributes2.size());
                    all.addAll(attributes);
                    all.addAll(attributes2);
                    return SimpleColumnValue.this.nested(all);
                }
            }
            return new Nested();
        }
    }
    
    static interface MultiValue extends FunctionalHelper {
        
        Object join(Object[] values) throws MiException;
        
        Object[] split(Object value, int index, Object[] result);
    }
    
    static final Function<Object, Object> IDENTITY = x -> x;
    static final XFunction<Object, Object, MiException> XIDENTITY = x -> x;
    
    public static final XFunction<Object, Object, MiException> TO_BOOL = o -> 
            Objects.equals(o, true) || 
            (o instanceof Number && ((Number) o).intValue() == 1);
    
    public static final XFunction<Object, Object, MiException> TO_BOOLEAN = o ->
            o == null ? null : TO_BOOL.apply(o);
    
//    public static <E> XFunction<MiResultSet, EntityFactory<List<E>>, MiException> listReader(String parent, String subresult, EntityTemplate<E> type) {
//        return rs -> {
//            MiResultSet r = subresult != null ? rs.subResult(subresult) : rs;
//            int parentIndex = rs.findColumn(parent);
//            return listReader(parentIndex, r, type);
//        };
//    }
//    
//    public static <E> EntityFactory<List<E>> listReader(int parentIndex, MiResultSet resultSet, EntityTemplate<E> type) throws MiException {
//        return listReader(parentIndex, resultSet, type.newFactory(resultSet));
//    }
//    
//    public static <E> EntityFactory<List<E>> listReader(int parentIndex, MiResultSet resultSet, EntityFactory<E> factory) {
//        return Entities.buildFactory(b -> b
//                .setFactory(ArrayList::new)
//                .addInitializer(list -> {
//                    Object parentId = resultSet.get(parentIndex);
//                    while (resultSet.get(parentIndex).equals(parentId)) {
//                        list.add(factory.newEntity());
//                        if (!resultSet.next()) break;
//                    }
//                    resultSet.previous();
//                })
//                .addCompleteAndClose(factory)
//                .addName("List<"+factory+">"));
//    }
    public static <E> void listReader(EntityTemplate<?> parentLookUp, EntityTemplate<E> nestedLookUp, String nestedPrefix, MiResultSet resultSet, FactoryBuilder<? super List<E>> builder) throws MiException {
        EntityFactory<?> getParent = builder.nestedFactory(parentLookUp, resultSet);
        EntityFactory<E> getNested = builder.nestedFactory(nestedLookUp, resultSet.subResult(nestedPrefix));
        builder.setFactory(ArrayList::new)
            .addName("List<" + getNested + ">")
            .addInitializer(list -> {
                Object parent = getParent.newEntity();
                while (getParent.newEntity().equals(parent)) {
                    list.add(getNested.newEntity());
                    if (!resultSet.next()) break;
                }
                resultSet.previous();
            });
    }
}
