package org.cthul.miro.at.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.futures.MiBiConsumer;
import org.cthul.miro.futures.MiFunction;
import org.cthul.miro.map.sql.MappedSqlTemplatesBuilder;

/**
 *
 */
public class TypeAnnotationReader {
    
    protected static <Entity> void readSuperClass(Class<?> clazz, MappedSqlTemplatesBuilder<Entity> mapping) {
        Class<?> sup = clazz.getSuperclass();
        if (sup == null) return;
        // TODO: check for annotations
        read(sup, mapping);
    }

    public static <Entity> void read(Class<?> clazz, MappedSqlTemplatesBuilder<Entity> mapping) {
        Table atTable = clazz.getAnnotation(Table.class);
        if (atTable != null) {
            Alias alias = clazz.getAnnotation(Alias.class);
            if (alias != null) {
                MiSqlParser.Table t = MiSqlParser.parseFromPart(atTable.name());
                mapping.mainTable(t, alias.value());
            } else {
                mapping.mainTable(atTable.name());
            }
        }
        for (Field f: clazz.getDeclaredFields()) {
            if (isTransient(f)) continue;
            Column atColumn = f.getAnnotation(Column.class);
            String colName = atColumn != null ? atColumn.name() : f.getName();
            boolean key = f.getAnnotation(Id.class) != null;
            mapping.attribute(ResultScope.DEFAULT, key, f.getName(), colName, getter(f), setter(f));
        }
    }
    
    private static <E> MiFunction<E, Object> getter(Field f) {
        f.setAccessible(true);
        return f::get;
    }
    
    private static <E> MiBiConsumer<E, Object> setter(Field f) {
        f.setAccessible(true);
        return f::set;
    }
    
    private static boolean isTransient(Field f) {
        return (f.getModifiers() & Modifier.TRANSIENT) != 0
                || f.getAnnotation(Transient.class) != null;
    }
}
