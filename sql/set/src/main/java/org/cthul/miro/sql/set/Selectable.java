package org.cthul.miro.sql.set;

/**
 *
 */
public interface Selectable {

    void selectInto(MappedSqlBuilder<?,?> target, String key);
}
