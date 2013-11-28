package org.cthul.miro.query.parts;

import java.util.Arrays;
import java.util.List;

public class InsertTuplePart extends AbstractQueryPart implements SelectableQueryPart {
    
    private final Object[] values;
    private final String sql;

    public InsertTuplePart(String key, Object... values) {
        super(key);
        this.values = values;
        sql = tuple(values.length);
    }

    @Override
    public void selectAttribute(String attribute, String alias) {
    }

    @Override
    public void appendSqlTo(StringBuilder sqlBuilder) {
        sqlBuilder.append(sql);
    }

    @Override
    public void appendArgsTo(List<Object> args) {
        args.addAll(Arrays.asList(values));
    }
    
    private static String tuple(int l) {
        StringBuilder sb = new StringBuilder(2+2*l);
        sb.append('(');
        for (int i = 0; i < l; i++) {
            if (i != 0) sb.append(',');
            sb.append('?');
        }
        return sb.append(')').toString();
    }
}
