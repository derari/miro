package org.cthul.miro.query.parts;

import java.util.Arrays;
import java.util.List;

public class UpdateTuplePart extends AbstractQueryPart implements ValuesQueryPart {
    
    private final Object[] values;
    private int keyCount = 0;

    public UpdateTuplePart(String key, Object... values) {
        super(key);
        this.values = values;
    }

    @Override
    public void appendSqlTo(StringBuilder sqlBuilder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendArgsTo(List<Object> args) {
        if (keyCount == 0) {
            args.add(Arrays.asList(values));
        } else {
            args.add(Arrays.asList(values).subList(keyCount, values.length));
        }
    }

    @Override
    public void selectAttribute(String attribute, String alias) {
    }

    @Override
    public void selectFilterValue(String key) {
        keyCount++;
    }

    @Override
    public void appendFilterValuesTo(List<Object> args) {
        if (keyCount == 0) return;
        args.add(Arrays.asList(values).subList(0, keyCount));
    }
}
