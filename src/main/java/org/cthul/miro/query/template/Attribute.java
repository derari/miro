package org.cthul.miro.query.template;

import org.cthul.miro.query.api.InternalQueryBuilder;

public class Attribute {

    private final String key;
    private final String select;
    private final String keyLiteral;
    private final String columnLiteral;
    private final String[] required;

    public Attribute(String key, String select, String keyLiteral, String columnLiteral, String... required) {
        this.key = key;
        this.select = select;
        this.required = required;
        this.keyLiteral = keyLiteral;
        this.columnLiteral = columnLiteral;
    }

    public void addRequired(InternalQueryBuilder queryBuilder) {
        if (required != null) {
            for (String r: required) {
                if (r != null) {
                    queryBuilder.put(r);
                }
            }
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
