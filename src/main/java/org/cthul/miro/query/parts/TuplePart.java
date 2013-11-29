package org.cthul.miro.query.parts;

import java.util.Arrays;
import java.util.List;

public class TuplePart extends AbstractQueryPart implements ValuesQueryPart {
    
    private final Object[] values;

    public TuplePart(String key, Object... values) {
        super(key);
        this.values = values;
    }

    @Override
    public Selector selector() {
        return new Selector(getKey());
    }

    protected class Selector extends AbstractQueryPart implements ValuesQueryPart.Selector {
        
        private int keyCount = 0;

        public Selector(String key) {
            super(key);
        }
        
        @Override
        public void selectAttribute(String attribute, String alias) {
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
        public void selectFilterValue(String key) {
            keyCount++;
        }

        @Override
        public void appendFilterValuesTo(List<Object> args) {
            if (keyCount == 0) return;
            args.add(Arrays.asList(values).subList(0, keyCount));
        }
    }
}
