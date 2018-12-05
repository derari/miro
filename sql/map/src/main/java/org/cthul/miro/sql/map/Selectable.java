package org.cthul.miro.sql.map;

/**
 *
 */
public interface Selectable {

    void selectInto(MappedSqlBuilder<?,?> target, String key);
}
