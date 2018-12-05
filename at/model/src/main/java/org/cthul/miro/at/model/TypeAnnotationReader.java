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

/**
 *
 */
public abstract class TypeAnnotationReader {
    
    private final Map<String, String> schemaMap;

    public TypeAnnotationReader() {
        this(new HashMap<>());
    }

    public TypeAnnotationReader(Map<String, String> schemaMap) {
        this.schemaMap = schemaMap;
    }

    public Map<String, String> getSchemaMap() {
        return schemaMap;
    }
    
    protected <Entity> void readSuperClass(Class<?> clazz) {
        Class<?> sup = clazz.getSuperclass();
        if (sup == null) return;
        // TODO: check for annotations
        read(sup);
    }

    public <Entity> void read(Class<?> clazz) {
        readSuperClass(clazz);
        Alias atAlias = clazz.getAnnotation(Alias.class);
        String tableAlias = atAlias != null ? atAlias.value() : null;
        Table atTable = clazz.getAnnotation(Table.class);
        if (atTable != null) {
            String schema = atTable.schema();
            schema = schemaMap.getOrDefault(schema, schema);
            String table = atTable.name();
            if (table == null) table = clazz.getSimpleName();
            if (tableAlias == null) tableAlias = uniqueAlias(table);
            table(schema, table, tableAlias);
        }
        for (Field f: clazz.getDeclaredFields()) {
            if (isTransient(f)) continue;
            boolean key = f.getAnnotation(Id.class) != null;
            property(key, f);
            Column atColumn = f.getAnnotation(Column.class);
            if (atColumn != null) {
                String colName = atColumn.name();
                if (colName == null) colName = f.getName();
                column(f.getName(), tableAlias, colName);
            } else {
                column(f.getName(), tableAlias, null);
            }
        }
    }

    protected abstract void table(String schema, String table, String tableAlias);

    protected abstract void property(boolean key, Field field);
    
    protected abstract void column(String property, String tableAlias, String columnName);
    
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
