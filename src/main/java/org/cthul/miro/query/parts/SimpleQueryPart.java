package org.cthul.miro.query.parts;

import java.util.Arrays;
import java.util.List;

public class SimpleQueryPart extends AbstractQueryPart implements SqlQueryPart {
    
    private final String sql;
    private Object[] values = null;

    public SimpleQueryPart(Object key, String sql) {
        super(key);
        this.sql = sql;
    }

    @Override
    public void put(Object key, Object... args) {
        if (key == null) {
            values = args;
            return;
        }
        super.put(key, args);
    }

    public String getSql() {
        return sql;
    }

    public Object[] getValues() {
        return values;
    }
    
    @Override
    public void appendSqlTo(StringBuilder sqlBuilder) {
        sqlBuilder.append(sql);
    }

    @Override
    public void appendArgsTo(List<Object> args) {
        if (values != null) {
            args.addAll(Arrays.asList(values));
        }
    }

    @Override
    public String toString() {
        Object[] v = getValues();
        return super.toString() + " " + getSql() + 
                (v == null ? "" : " " + Arrays.toString(v));
    }
}
