package org.cthul.miro.at.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.composer.sql.SqlAttribute;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.function.MiBiConsumer;
import org.cthul.miro.function.MiFunction;
import org.cthul.miro.map.sql.MappedSqlBuilder;

/**
 *
 */
public class TypeAnnotationReader {
    
    private final Map<String, String> schemaMap = new HashMap<>();

    public TypeAnnotationReader() {
    }

    public Map<String, String> getSchemaMap() {
        return schemaMap;
    }
    
    protected <Entity> void readSuperClass(Class<?> clazz, MappedSqlBuilder<Entity,?> mapping) {
        Class<?> sup = clazz.getSuperclass();
        if (sup == null) return;
        // TODO: check for annotations
        read(sup, mapping);
    }

    public <Entity> void read(Class<?> clazz, MappedSqlBuilder<Entity,?> mapping) {
        readSuperClass(clazz, mapping);
        Alias atAlias = clazz.getAnnotation(Alias.class);
        String alias = atAlias != null ? atAlias.value() : null;
        Table atTable = clazz.getAnnotation(Table.class);
        if (atTable != null) {
            String schema = atTable.schema();
            schema = schemaMap.getOrDefault(schema, schema);
            String table = atTable.name();
            if (table == null) table = clazz.getSimpleName();
            if (alias == null) alias = uniqueAlias(table);
//            QlCode code = QlCode.id(schema, table).ql(" ").ql(alias);
//            SqlTable sqlTable = new SqlTable(alias, code);
            mapping.from(schema, table, alias);
        }
        for (Field f: clazz.getDeclaredFields()) {
            if (isTransient(f)) continue;
            mapping.field(f.getName(), getter(f), setter(f));
            Column atColumn = f.getAnnotation(Column.class);
            if (atColumn != null) {
                if (alias == null) throw new IllegalArgumentException(clazz.getSimpleName() + ": table name required");
                boolean key = f.getAnnotation(Id.class) != null;
                String colName = atColumn.name();
                if (colName == null) colName = f.getName();
                SqlAttribute at = new SqlAttribute(f.getName(), false, 
                        QlCode.ql(alias).ql(".").id(colName), QlCode.ql(f.getName()));
                at.getDependencies().add(alias);
                mapping.attribute(ResultScope.DEFAULT, key, at);
            }
        }
    }
    
    private static <E> MiFunction<E, Object> getter(Field f) {
        f.setAccessible(true);
        return f::get;
    }
    
    private static <E> MiBiConsumer<E, Object> setter(Field f) {
        f.setAccessible(true);
        Class<?> c = f.getType();
        if (c == boolean.class) {
            return (e, o) -> {
                boolean b = o != null && (o.equals(true) ||
                        ((o instanceof Number) && ((Number) o).intValue() == 1));
                f.set(e, b);
            };
        }
        if (c == Boolean.class) {
            return (e, o) -> {
                if (o == null) {
                    f.set(e, null);
                } else {
                    boolean b = o.equals(true) ||
                            ((o instanceof Number) && ((Number) o).intValue() == 1);
                    f.set(e, b);
                }
            };
        }
        return f::set;
    }
    
    private static boolean isTransient(Field f) {
        return (f.getModifiers() & Modifier.TRANSIENT) != 0
                || f.getAnnotation(Transient.class) != null;
    }
    
    private static final AtomicLong COUNTER = new AtomicLong(1);
    
    private static String uniqueAlias(String name) {
        name = name.toLowerCase().replaceAll("^[^a-z]+|\\W", "");
        return name + COUNTER.getAndIncrement();
    }
}
