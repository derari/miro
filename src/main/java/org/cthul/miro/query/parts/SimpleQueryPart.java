package org.cthul.miro.query.parts;

import java.util.Arrays;
import java.util.List;

public class SimpleQueryPart extends AbstractQueryPart {
    
    private final String sql;
    private Object[] values = null;

    public SimpleQueryPart(String key, String sql) {
        super(key);
        this.sql = sql;
    }

    @Override
    public void put(String key, Object... args) {
        switch (key) {
            case "":
                values = args;
                return;
            default:
                super.put(key, args);
        }
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
}
