package org.cthul.miro.sql.map;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.at.model.TypeAnnotationReader;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.base.ResultColumns;
import org.cthul.miro.entity.base.ResultColumns.ColumnRule;
import org.cthul.miro.entity.map.ColumnMappingBuilder;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.impl.AbstractNodeType;
import org.cthul.miro.graph.impl.AbstractTypeBuilder;
import org.cthul.strings.JavaNames;

/**
 * Builds an entity type from annotations. HAS NOTHING TO DO WITH SQL
 * @param <E>
 */
public class SqlTableType<E> extends AbstractTypeBuilder<E, SqlTableType<E>> {

    public SqlTableType(Class<E> clazz) {
        super(clazz);
        init();
    }

    private void init() {
        new TypeAnnotationReader() {
            Map<String, Field> fields = new HashMap<>();
            
            @Override
            protected void table(String schema, String table, String tableAlias) { }
            
            @Override
            protected void property(boolean key, Field field) {
                fields.put(field.getName(), field);
            }
            
            @Override
            protected void column(String field, String tableAlias, String columnName) {
                Field f = fields.remove(field);
                if (columnName == null) columnName = JavaNames.under_score(field);
                Class fieldType = f.getType();
                if (fieldType.isPrimitive() || PRIMITIVES.contains(fieldType)) {
                    SqlTableType.this
                            .require(columnName)
                            .field(f);
                } else if (fieldType.isAssignableFrom(List.class)) {
                    Type gt = f.getGenericType();
                    if (gt instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) gt;
                        Type argType = pt.getActualTypeArguments()[0];
                        if (PRIMITIVES.contains(argType)) {
                            return; // TODO something
                        }
                        String prefix = columnName + "_";
                        SqlTableType.this
                                .as(columnName)
                                .column(ResultColumns.matchPrefixIgnoreCase(ColumnRule.OPTIONAL, prefix))
                                .readWith((rs, g) -> {
                                    // TODO use key attributes instead of "id"
                                    int idIndex = rs.findColumn("id");
                                    rs = rs.subResult(prefix);
                                    return ColumnMappingBuilder.listReader(
                                            idIndex, rs, g.getEntityType(argType, "*"));
                                })
                                .field(f);
                    }
                } else {
                    String prefix = columnName + "_";
                    SqlTableType.this
                            .as(columnName)
                            .column(ResultColumns.matchPrefixIgnoreCase(ColumnRule.OPTIONAL, prefix))
                            .readWith((rs, g) -> {
                                rs = rs.subResult(prefix);
                                return g.getEntityType(fieldType, "*").newFactory(rs);
                            })
                            .field(f);
                }
                
            }
        }.read(entityClass());
    }

    @Override
    protected AbstractNodeType.BatchLoader<E> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
        throw new UnsupportedOperationException();
    }
    
    private static final List<Type> PRIMITIVES = Arrays.asList(Long.class, Integer.class, String.class);
}
