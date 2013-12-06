package org.cthul.miro.query.template;

import org.cthul.miro.query.InternalQueryBuilder;

public class Attribute {

    private final String key;
    private final String select;
    private final String keyLiteral;
    private final String columnLiteral;
    private final Object[] required;

    public Attribute(String key, String select, String keyLiteral, String columnLiteral, Object... required) {
        this.key = key;
        this.select = select;
        this.required = required;
        this.keyLiteral = keyLiteral;
        this.columnLiteral = columnLiteral;
    }

    public void addRequired(InternalQueryBuilder queryBuilder) {
        if (required != null) {
            for (Object r: required) {
                queryBuilder.put(r);
//                if (r instanceof String) {
//                    String s = (String) r;
//                    int dot = s.indexOf('.');
//                    if (dot < 0) {
//                        queryBuilder.put(s);
//                    } else {
//                        queryBuilder.put2(s.substring(0, dot), s.substring(dot+1));
//                    }
//                } else if (r != null) {
//                    
//                }
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
    @Override
    public String toString() {
        return getSelect() + " AS " + getKey();
    }
}
