package org.cthul.miro.query.template;

import org.cthul.miro.query.api.InternalQueryBuilder;

public class Attribute {

    private final String key;
    private final String select;
    private final String table;
    private final String keyLiteral;
    private final String columnLiteral;
    private final String require;

    public Attribute(String key, String select, String table, String keyLiteral, String columnLiteral) {
        this.key = key;
        this.select = select;
        this.table = table;
        this.keyLiteral = keyLiteral;
        this.columnLiteral = columnLiteral;
        if (table == null) {
            require = null;
        }
    }

    public void addRequired(InternalQueryBuilder queryBuilder) {
        if (table != null) {
            queryBuilder.require(table);
        }
    }

    /** e.g. firstName */
    public String getKey() {
        return key;
    }

    /** e.g. `data`.`first_name` */
    public String getSelect() {
        return select;
    }

    /** e.g. `firstName` */
    public String getKeyLiteral() {
        return keyLiteral;
    }

    /** e.g. `first_name` */
    public String getColumnLiteral() {
        return columnLiteral;
    }

//    /** e.g. `data` */
//    public String getTable() {
//        return table;
//    }
}
