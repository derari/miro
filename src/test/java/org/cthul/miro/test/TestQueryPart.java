package org.cthul.miro.test;

import java.util.Arrays;
import java.util.List;
import org.cthul.miro.query.parts.AttributeQueryPart;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.parts.ValuesQueryPart;

public class TestQueryPart implements QueryPart {
    
    protected final String sql;
    protected final Object[] args;

    public TestQueryPart(String sql, Object... args) {
        this.sql = sql;
        this.args = args;
    }

    @Override
    public String getKey() {
        return sql;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public void put(String key, Object... args) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void appendSqlTo(StringBuilder sqlBuilder) {
        sqlBuilder.append(sql);
    }

    @Override
    public void appendArgsTo(List<Object> args) {
        args.addAll(Arrays.asList(this.args));
    }

    @Override
    public String toString() {
        return sql + " " + Arrays.toString(args);
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

    public static class Values extends TestQueryPart implements ValuesQueryPart {

        private final Object[] filterValues;
        
        public Values(Object... args) {
            this(args, new Object[0]);
        }
        
        public Values(int argC, Object... args) {
            this(Arrays.copyOfRange(args, 0, argC), Arrays.copyOfRange(args, argC, args.length));
        }
        
        public Values(Object[] args, Object[] filterValues) {
            super(tuple(args.length), args);
            this.filterValues = filterValues;
        }

        @Override
        public void selectAttribute(String attribute, String alias) {
        }

        @Override
        public void selectFilterValue(String key) {
        }

        @Override
        public void appendFilterValuesTo(List<Object> args) {
            args.addAll(Arrays.asList(filterValues));
        }
    }
    
    public static class Attribute extends TestQueryPart implements AttributeQueryPart {

        public Attribute(String sql) {
            super(sql);
        }

        @Override
        public String getAttributeKey() {
            return getKey();
        }

        @Override
        public String getSqlName() {
            return getSql();
        }
    }
}
